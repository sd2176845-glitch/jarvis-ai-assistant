package com.jarvis.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.telephony.SmsMessage;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import java.util.Locale;

/** Announces incoming calls and SMS only after the user grants the matching Android permissions. */
public class PhoneEventReceiver extends BroadcastReceiver {
    private static TextToSpeech speaker;
    private static void announce(Context context, String message) {
        if (speaker != null) { speaker.stop(); speaker.shutdown(); speaker = null; }
        speaker = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                speaker.setLanguage(new Locale("en", "IN"));
                speaker.setSpeechRate(0.92f);
                speaker.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_phone_event");
            }
        });
    }
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                String name = null;
                if (number != null && context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Cursor c = null;
                    try {
                        c = context.getContentResolver().query(ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(number).build(),
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                        if (c != null && c.moveToFirst()) name = c.getString(0);
                    } catch (Exception ignored) {} finally { if (c != null) c.close(); }
                }
                announce(context, name == null || name.trim().isEmpty() ? "Boss, incoming call aa raha hai." : "Boss, " + name + " ka call aa raha hai.");
            }
        } else if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) return;
            StringBuilder message = new StringBuilder("Boss, naya SMS aaya hai. ");
            for (Object pdu : pdus) {
                SmsMessage sms;
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    String format = bundle.getString("format");
                    sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else sms = SmsMessage.createFromPdu((byte[]) pdu);
                if (sms != null) {
                    String sender = sms.getDisplayOriginatingAddress();
                    String body = sms.getMessageBody();
                    if (sender != null) message.append("Sender ").append(sender).append(". ");
                    if (body != null) message.append(body);
                    break;
                }
            }
            announce(context, message.toString());
        }
    }
}
