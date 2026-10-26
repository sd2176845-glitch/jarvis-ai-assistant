package com.jarvis.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.telephony.SmsMessage;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.os.Bundle;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/** Announces incoming calls and SMS only after the user grants the matching Android permissions. */
public class PhoneEventReceiver extends BroadcastReceiver {
    private static void announce(Context context, String message, PendingResult pending) {
        AtomicBoolean finished = new AtomicBoolean(false);
        Runnable finish = () -> {
            if (finished.compareAndSet(false, true)) pending.finish();
        };
        final TextToSpeech[] engine = new TextToSpeech[1];
        try {
            engine[0] = new TextToSpeech(context.getApplicationContext(), status -> {
                TextToSpeech tts = engine[0];
                if (status != TextToSpeech.SUCCESS || tts == null) {
                    if (tts != null) tts.shutdown();
                    finish.run();
                    return;
                }
                tts.setLanguage(new Locale("en", "IN"));
                tts.setSpeechRate(0.92f);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) {}
                    @Override public void onDone(String id) {
                        tts.shutdown();
                        finish.run();
                    }
                    @Override public void onError(String id) {
                        tts.shutdown();
                        finish.run();
                    }
                });
                if (tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_phone_event") == TextToSpeech.ERROR) {
                    tts.shutdown();
                    finish.run();
                }
            });
        } catch (Exception e) {
            finish.run();
        }
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) return;
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            String name = null;
            if (number != null && context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Cursor c = null;
                try {
                    c = context.getContentResolver().query(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(number).build(),
                        new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                    if (c != null && c.moveToFirst()) name = c.getString(0);
                } catch (Exception ignored) {
                } finally {
                    if (c != null) c.close();
                }
            }
            String announcement = name == null || name.trim().isEmpty()
                    ? "Boss, incoming call aa raha hai."
                    : "Boss, " + name + " ka call aa raha hai.";
            announce(context, announcement, goAsync());
        } else if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null || pdus.length == 0) return;
            StringBuilder message = new StringBuilder("Boss, naya SMS aaya hai. ");
            String format = bundle.getString("format");
            for (Object pdu : pdus) {
                if (!(pdu instanceof byte[])) continue;
                SmsMessage sms;
                if (android.os.Build.VERSION.SDK_INT >= 23) sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                else sms = SmsMessage.createFromPdu((byte[]) pdu);
                if (sms != null) {
                    String sender = sms.getDisplayOriginatingAddress();
                    String body = sms.getMessageBody();
                    if (sender != null && message.indexOf("Sender ") < 0) message.append("Sender ").append(sender).append(". ");
                    if (body != null) message.append(body).append(" ");
                }
            }
            announce(context, message.toString(), goAsync());
        }
    }
}
