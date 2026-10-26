package com.jarvis.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

/**
 * User-started, visible wake-word listener. The persistent notification makes microphone use clear.
 * Android speech recognition is not a guaranteed low-power offline hotword engine.
 */
public class WakeWordService extends Service {
    private static final String CHANNEL_ID = "jarvis_voice_channel";
    private static final int NOTIFICATION_ID = 4201;
    private static final String ACTION_STOP = "com.jarvis.assistant.STOP_WAKE_LISTENING";
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean stopping = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("en", "IN"));
                tts.setSpeechRate(0.92f);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopListening();
            return START_NOT_STICKY;
        }
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return START_NOT_STICKY;
        }
        stopping = false;
        startForeground(NOTIFICATION_ID, buildNotification());
        beginRecognition();
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "JARVIS voice listening", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Visible notification while JARVIS listens for its wake phrase.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent openApp = new Intent(this, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(this, 1, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Intent stopIntent = new Intent(this, WakeWordService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 2, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("JARVIS is listening")
                .setContentText("Say “Hey JARVIS”. Tap Stop to turn off the microphone.")
                .setContentIntent(openPending)
                .setOngoing(true)
                .addAction(new Notification.Action.Builder(null, "Stop listening", stopPending).build());
        return builder.build();
    }

    private void beginRecognition() {
        if (stopping) return;
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            handler.postDelayed(this::beginRecognition, 5000);
            return;
        }
        try {
            if (recognizer != null) {
                recognizer.destroy();
                recognizer = null;
            }
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onError(int error) { scheduleRestart(1200); }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) handleRecognized(matches.get(0));
                    scheduleRestart(1800);
                }
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
            Intent speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
            speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN");
            speech.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            recognizer.startListening(speech);
        } catch (Exception e) {
            scheduleRestart(2500);
        }
    }

    private void handleRecognized(String heard) {
        String text = heard == null ? "" : heard.toLowerCase(Locale.ROOT).trim();
        String wake = text.replace("hey jarvis ji", "hey jarvis")
                .replace("hey jarvesh", "hey jarvis").replace("hey jarvish", "hey jarvis");
        if (!(wake.contains("hey jarvis") || wake.contains("hey jarvesh") || wake.contains("hey jarvish"))) return;
        String command = wake.replace("hey jarvis", "").trim()
                .replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        if (command.isEmpty()) {
            say("Haan Boss, JARVIS sun raha hai. Boliye.");
        } else if (isCallCommand(command)) {
            String target = extractContactTarget(command);
            if (target.isEmpty()) say("Kis contact ko call karna hai? Contact ka naam boliye.");
            else openContactDialer(target);
        } else if (command.contains("open youtube") || command.contains("youtube kholo") || command.contains("youtube chalao")) {
            say("YouTube khol raha hoon.");
            handler.postDelayed(() -> {
                try {
                    Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                    if (launch == null) launch = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com"));
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launch);
                } catch (Exception ignored) { say("YouTube abhi nahi khul paaya."); }
            }, 400);
        } else if (command.contains("open whatsapp") || command.contains("whatsapp kholo") || command.contains("whatsapp khol")) {
            say("WhatsApp khol raha hoon.");
            handler.postDelayed(() -> {
                try {
                    Intent launch = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                    if (launch != null) { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch); }
                    else say("WhatsApp is phone par nahi mila.");
                } catch (Exception ignored) { say("WhatsApp abhi nahi khul paaya."); }
            }, 400);
        } else {
            say("Haan Boss, command suni. Background mode mein abhi call, YouTube aur WhatsApp commands supported hain.");
        }
    }

    private boolean isCallCommand(String command) {
        return command.contains("call") || command.contains("phone lagao")
                || command.contains("call karo") || command.contains("ko phone karo")
                || command.contains("ko call karo") || command.contains("dial ");
    }

    private String extractContactTarget(String command) {
        String target = command.replace("please", "").replace("mere ", "")
                .replace("meri ", "").replace("ko call karo", "")
                .replace("call karo", "").replace("call", "")
                .replace("phone lagao", "").replace("ko phone karo", "")
                .replace("phone karo", "").replace("dial", "")
                .replace("lagao", "").trim();
        return target.replaceAll("\\s+", " ").trim();
    }

    private void openContactDialer(String requestedName) {
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            say("Contact permission nahi mili. JARVIS app kholo aur background listening dobara enable karo.");
            return;
        }
        String number = null;
        Cursor cursor = null;
        try {
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
            String[] args = new String[]{"%" + requestedName + "%"};
            cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    selection, args, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) number = cursor.getString(0);
        } catch (SecurityException ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        if (number == null || number.trim().isEmpty()) {
            say("Phone contacts mein " + requestedName + " nahi mila. Contact ko isi naam se save karke dobara koshish karo.");
            return;
        }
        final String phoneNumber = number;
        say("Dialer mein " + requestedName + " ka number khol raha hoon. Call confirm kar dena.");
        handler.postDelayed(() -> {
            try {
                Intent dial = new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + android.net.Uri.encode(phoneNumber)));
                dial.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dial);
            } catch (Exception ignored) {
                say("Dialer nahi khul paaya. Phone app se call kar lo.");
            }
        }, 450);
    }

    private void say(String text) {
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_background_reply");
    }

    private void scheduleRestart(long delay) {
        if (!stopping) handler.postDelayed(this::beginRecognition, delay);
    }

    private void stopListening() {
        stopping = true;
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) {
            try { recognizer.cancel(); recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopping = true;
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
