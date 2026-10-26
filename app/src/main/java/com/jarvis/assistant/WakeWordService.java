package com.jarvis.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
        return START_NOT_STICKY;
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
        String text = heard == null ? "" : heard.toLowerCase(Locale.ROOT);
        if (text.contains("hey jarvis") || text.contains("hey jarvesh") ||
                text.contains("hey jarvish") || text.contains("hey jarvis ji")) {
            String command = text.replace("hey jarvis", "").replace("hey jarvesh", "")
                    .replace("hey jarvish", "").replace("hey jarvis ji", "").trim();
            if (command.isEmpty()) {
                say("Haan Boss, JARVIS sun raha hai. Boliye.");
            } else if (command.contains("open youtube") || command.contains("youtube kholo")) {
                say("YouTube khol raha hoon.");
                handler.postDelayed(() -> {
                    try {
                        Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                        if (launch == null) launch = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com"));
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(launch);
                    } catch (Exception ignored) {}
                }, 400);
            } else if (command.contains("open whatsapp") || command.contains("whatsapp kholo")) {
                say("WhatsApp khol raha hoon.");
                handler.postDelayed(() -> {
                    try {
                        Intent launch = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                        if (launch != null) { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch); }
                    } catch (Exception ignored) {}
                }, 400);
            } else {
                say("Haan Boss, aapki baat suni. Is command ko abhi background mode mein support nahi karta.");
            }
        }
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
