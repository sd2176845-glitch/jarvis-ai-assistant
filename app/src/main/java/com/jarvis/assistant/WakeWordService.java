package com.jarvis.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.AlarmClock;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

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
    private boolean waitingForAi = false;
    private boolean speaking = false;
    private int utteranceCounter = 0;
    private String activeUtteranceId = null;
    private PowerManager.WakeLock screenOffWakeLock;
    // After the wake phrase, accept the next utterance as the command.
    private boolean awaitingCommand = false;
    private String pendingCallNumber = null;
    private String pendingCallName = null;
    private boolean awaitingCallConfirmation = false;
    private final Runnable commandTimeout = () -> { awaitingCommand = false; awaitingCallConfirmation = false; pendingCallNumber = null; pendingCallName = null; };

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("en", "IN"));
                tts.setSpeechRate(0.92f);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String utteranceId) {
                        handler.post(() -> {
                            if (utteranceId.equals(activeUtteranceId)) speaking = true;
                        });
                    }
                    @Override public void onDone(String utteranceId) {
                        finishUtterance(utteranceId);
                    }
                    @Override public void onError(String utteranceId) {
                        finishUtterance(utteranceId);
                    }
                });
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
        acquireScreenOffWakeLock();
        beginRecognition();
        return START_STICKY;
    }

    private void acquireScreenOffWakeLock() {
        try {
            PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
            if (power != null && (screenOffWakeLock == null || !screenOffWakeLock.isHeld())) {
                screenOffWakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "JARVIS:VoiceListening");
                screenOffWakeLock.setReferenceCounted(false);
                screenOffWakeLock.acquire();
            }
        } catch (Exception ignored) {
            // Android may restrict background operation; the persistent notification remains visible.
        }
    }

    private void releaseScreenOffWakeLock() {
        try {
            if (screenOffWakeLock != null && screenOffWakeLock.isHeld()) screenOffWakeLock.release();
        } catch (Exception ignored) {
        }
        screenOffWakeLock = null;
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
        if (stopping || waitingForAi || speaking) return;
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
                @Override public void onError(int error) { scheduleRestart(500); }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) handleRecognized(matches.get(0));
                    if (!waitingForAi && !speaking) scheduleRestart(400);
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
            scheduleRestart(800);
        }
    }

    private void handleRecognized(String heard) {
        String text = heard == null ? "" : heard.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[^a-z0-9\\u0900-\\u097F\\s]", " ").replaceAll("\\s+", " ").trim();

        // A pending call always requires a clear spoken yes/no confirmation.
        if (awaitingCallConfirmation) {
            if (matches(text, "yes", "haan", "han", "ha", "confirm", "yes please", "haan call karo", "haan kar do", "हाँ", "हां", "हाँ जी", "हां जी", "जी हाँ", "जी हां", "हाँ कॉल करो", "हां कॉल करो", "हाँ कर दो", "हां कर दो")) {
                String number = pendingCallNumber;
                String name = pendingCallName;
                awaitingCallConfirmation = false;
                awaitingCommand = false;
                pendingCallNumber = null;
                pendingCallName = null;
                handler.removeCallbacks(commandTimeout);
                if (number == null || number.trim().isEmpty()) {
                    say("Number nahi mila. Dobara contact ka naam bolo.");
                } else if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    say("Direct call permission nahi mili. JARVIS app kholo aur permission allow karo.");
                } else {
                    say(name + " ko call laga raha hoon.");
                    handler.postDelayed(() -> {
                        try {
                            Intent call = new Intent(Intent.ACTION_CALL, android.net.Uri.parse("tel:" + android.net.Uri.encode(number)));
                            call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(call);
                        } catch (SecurityException e) {
                            say("Call permission nahi mili. Phone app se call kar lo.");
                        } catch (Exception e) {
                            say("Call nahi lag paayi. Phone app se try karo.");
                        }
                    }, 150);
                }
                return;
            }
            if (matches(text, "no", "nahi", "nahin", "cancel", "mat karo", "rehne do")) {
                awaitingCallConfirmation = false;
                pendingCallNumber = null;
                pendingCallName = null;
                handler.removeCallbacks(commandTimeout);
                say("Theek hai, call cancel kar di.");
                return;
            }
            say("Call confirm karne ke liye haan bolo, ya cancel karne ke liye nahi bolo.");
            return;
        }

        String wake = text.replace("hey jarvis ji", "jarvis")
                .replace("hey jarvesh", "jarvis").replace("hey jarvish", "jarvis")
                .replace("hey jarvis", "jarvis").replace("jarvesh", "jarvis")
                .replace("jarvish", "jarvis").replace("jarves", "jarvis");

        String command;
        if (wake.contains("jarvis")) {
            command = wake.replaceFirst(".*?\\bjarvis\\b", "").trim()
                    .replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
            if (command.isEmpty()) {
                awaitingCommand = true;
                handler.removeCallbacks(commandTimeout);
                handler.postDelayed(commandTimeout, 8000);
                say("Haan Boss, bolo.");
                return;
            }
        } else if (awaitingCommand) {
            command = text;
            awaitingCommand = false;
            handler.removeCallbacks(commandTimeout);
        } else {
            return;
        }

        if (matches(command, "call uthao", "phone uthao", "answer call", "pick up call", "call answer karo")) {
            sendCallControl(CallControlAccessibilityService.ACTION_ANSWER, "Call uthाने ki koshish kar raha hoon. Accessibility service enabled honi chahiye.");
        } else if (matches(command, "call kaat do", "phone kaat do", "end call", "hang up", "call disconnect karo")) {
            sendCallControl(CallControlAccessibilityService.ACTION_END, "Call kaatne ki koshish kar raha hoon.");
        } else if (matches(command, "speaker on", "speaker chalu", "speakerphone on", "loudspeaker on")) {
            sendCallControl(CallControlAccessibilityService.ACTION_SPEAKER, "Speaker button dabane ki koshish kar raha hoon.");
        } else if (command.isEmpty()) {
            say("Command samajh nahi aaya. Dobara boliye.");
        } else if (command.equals("call screen kholo") || command.equals("dialer kholo") ||
                command.equals("phone kholo") || command.equals("open phone") || command.equals("phone")) {
            launchIntent(new Intent(Intent.ACTION_DIAL), "Phone dialer khol raha hoon.");
        } else if (isCallCommand(command)) {
            String target = extractContactTarget(command);
            if (target.isEmpty()) say("Kis contact ko call karna hai? Contact ka naam boliye.");
            else openContactDialer(target);
        } else if (matches(command, "youtube", "open youtube", "youtube kholo", "youtube khol", "youtube chalao")) {
            launchApp("com.google.android.youtube", "YouTube", "https://www.youtube.com");
        } else if (matches(command, "whatsapp", "open whatsapp", "whatsapp kholo", "whatsapp khol", "whatsapp khol do")) {
            launchApp("com.whatsapp", "WhatsApp", "https://www.whatsapp.com");
        } else if (matches(command, "instagram", "open instagram", "instagram kholo", "instagram khol")) {
            launchApp("com.instagram.android", "Instagram", "https://www.instagram.com");
        } else if (matches(command, "facebook", "open facebook", "facebook kholo", "facebook khol")) {
            launchApp("com.facebook.katana", "Facebook", "https://www.facebook.com");
        } else if (matches(command, "chrome", "open chrome", "chrome kholo", "browser kholo", "internet kholo")) {
            launchApp("com.android.chrome", "Chrome", "https://www.google.com");
        } else if (matches(command, "camera", "open camera", "camera kholo", "camera khol")) {
            launchIntent(new Intent("android.media.action.IMAGE_CAPTURE"), "Camera khol raha hoon.");
        } else if (matches(command, "maps", "open maps", "maps kholo", "google maps", "map kholo")) {
            launchIntent(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=")), "Maps khol raha hoon.");
        } else if (matches(command, "calculator", "open calculator", "calculator kholo")) {
            launchIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR), "Calculator khol raha hoon.");
        } else if (matches(command, "calendar", "open calendar", "calendar kholo")) {
            launchIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR), "Calendar khol raha hoon.");
        } else if (matches(command, "clock", "open clock", "clock kholo", "ghadi kholo", "alarms kholo")) {
            launchIntent(new Intent(AlarmClock.ACTION_SHOW_ALARMS), "Clock khol raha hoon.");
        } else if (matches(command, "settings", "open settings", "settings kholo", "phone settings")) {
            launchIntent(new Intent(android.provider.Settings.ACTION_SETTINGS), "Settings khol raha hoon.");
        } else if (matches(command, "wifi settings", "wifi kholo", "wi fi settings")) {
            launchIntent(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), "Wi-Fi settings khol raha hoon.");
        } else if (matches(command, "bluetooth settings", "bluetooth kholo")) {
            launchIntent(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS), "Bluetooth settings khol raha hoon.");
        } else if (matches(command, "location settings", "location kholo", "gps settings")) {
            launchIntent(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), "Location settings khol raha hoon.");
        } else if (matches(command, "phone", "open phone", "dialer kholo", "phone kholo", "call screen kholo")) {
            launchIntent(new Intent(Intent.ACTION_DIAL), "Phone dialer khol raha hoon.");
        } else if (matches(command, "contacts", "open contacts", "contacts kholo", "contact kholo")) {
            launchIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS), "Contacts khol raha hoon.");
        } else if (matches(command, "time", "what time is it", "kitne baje", "samay batao")) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault());
            say("Abhi time hai " + fmt.format(new java.util.Date()));
        } else if (matches(command, "date", "today date", "aaj ki date", "tarikh batao")) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("en", "IN"));
            say("Aaj " + fmt.format(new java.util.Date()));
        } else if (command.contains("battery") || command.contains("charge kitna") || command.contains("battery kitna")) {
            Intent battery = registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (battery != null) {
                int level = battery.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = battery.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                say(level >= 0 && scale > 0 ? "Battery " + (level * 100 / scale) + " percent hai." : "Battery level nahi mil paaya.");
            } else say("Battery level nahi mil paaya.");
        } else if (matches(command, "alarm lagao", "set alarm", "alarm kholo", "alarm set karo")) {
            launchIntent(new Intent(AlarmClock.ACTION_SET_ALARM), "Alarm screen khol raha hoon. Time set karke confirm kar dena.");
        } else if (command.startsWith("search ") || command.startsWith("google ") || command.startsWith("search for ")) {
            String query = command.replaceFirst("^search for ", "").replaceFirst("^search ", "").replaceFirst("^google ", "").trim();
            if (query.isEmpty()) say("Kya search karna hai, Boss?");
            else launchIntent(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode(query))), "Web par search kar raha hoon.");
        } else if (command.startsWith("message ") || command.startsWith("sms ") || command.startsWith("text ")) {
            String body = command.replaceFirst("^(message|sms|text)\\s+", "").trim();
            Intent sms = new Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:"));
            sms.putExtra("sms_body", body);
            launchIntent(sms, "Message draft khol raha hoon. Bhejne se pehle check kar lena.");
        } else if (command.contains("phone lock") || command.contains("lock phone") || command.contains("phone lock karo") || command.contains("screen lock karo")) {
            say("Phone lock karne ke liye Android mein Device Admin ya Accessibility permission setup chahiye. Abhi ye command enabled nahi hai.");
        } else {
            waitingForAi = true;
            JarvisAiClient.ask(this, command, answer -> {
                if (stopping) {
                    waitingForAi = false;
                    return;
                }
                if (tts == null) {
                    waitingForAi = false;
                    say(answer);
                    scheduleRestart(350);
                } else {
                    speakAiReply(answer);
                }
            });
        }
    }

    private void sendCallControl(String action, String response) {
        say(response);
        Intent control = new Intent(action);
        control.setPackage(getPackageName());
        sendBroadcast(control);
    }

    private boolean matches(String command, String... phrases) {
        for (String phrase : phrases) {
            if (command.equals(phrase) || command.contains(phrase)) return true;
        }
        return false;
    }

    private void launchApp(String packageName, String appName, String fallbackUrl) {
        say(appName + " khol raha hoon.");
        handler.postDelayed(() -> {
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launch != null) launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                else launch = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(fallbackUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
            } catch (Exception ignored) {
                say(appName + " abhi nahi khul paaya.");
            }
        }, 150);
    }

    private void launchIntent(Intent intent, String response) {
        say(response);
        handler.postDelayed(() -> {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception ignored) {
                say("Ye app ya screen is phone par nahi khul paayi.");
            }
        }, 150);
    }

    private boolean isCallCommand(String command) {
        return command.contains("call") || command.contains("phone lagao")
                || command.contains("call karo") || command.contains("ko phone karo")
                || command.contains("ko call karo") || command.contains("dial ");
    }

    private String extractContactTarget(String command) {
        String target = command.replace("please", "").replace("mere ", "")
                .replace("meri ", "").replace("ko call lagao", "").replace("call lagao", "")
                .replace("ko call karo", "")
                .replace("call karo", "").replace("call", "")
                .replace("phone lagao", "").replace("ko phone karo", "")
                .replace("phone karo", "").replace("dial", "")
                .replace("lagao", "").replaceAll("\\s+ko$", "").trim();
        return target.replaceAll("\\s+", " ").trim();
    }

    private void openContactDialer(String requestedName) {
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            say("Contact permission nahi mili. JARVIS app kholo aur contact permission allow karo.");
            return;
        }
        String number = null;
        String matchedName = requestedName;
        Cursor cursor = null;
        try {
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
            String[] args = new String[]{"%" + requestedName + "%"};
            cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    selection, args, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                number = cursor.getString(0);
                matchedName = cursor.getString(1);
            }
        } catch (SecurityException ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        if (number == null || number.trim().isEmpty()) {
            say("Phone contacts mein " + requestedName + " nahi mila. Contact ka naam check karke dobara bolo.");
            return;
        }
        pendingCallNumber = number;
        pendingCallName = matchedName == null ? requestedName : matchedName;
        awaitingCallConfirmation = true;
        awaitingCommand = false;
        handler.removeCallbacks(commandTimeout);
        handler.postDelayed(commandTimeout, 10000);
        say(pendingCallName + " ko call karun? Haan bolo, ya nahi bolkar cancel karo.");
    }

    private void say(String text) {
        if (tts == null) {
            speaking = false;
            activeUtteranceId = null;
            scheduleRestart(350);
            return;
        }
        String utteranceId = "jarvis_background_reply_" + (++utteranceCounter);
        activeUtteranceId = utteranceId;
        speaking = true;
        int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        if (result == TextToSpeech.ERROR && utteranceId.equals(activeUtteranceId)) {
            activeUtteranceId = null;
            speaking = false;
            scheduleRestart(350);
        }
    }

    private void speakAiReply(String text) {
        if (tts == null) {
            waitingForAi = false;
            say(text);
            return;
        }
        String utteranceId = "jarvis_ai_reply_" + (++utteranceCounter);
        activeUtteranceId = utteranceId;
        speaking = true;
        int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        if (result == TextToSpeech.ERROR && utteranceId.equals(activeUtteranceId)) {
            activeUtteranceId = null;
            speaking = false;
            waitingForAi = false;
            say(text);
        }
    }

    private void finishUtterance(String utteranceId) {
        handler.post(() -> {
            if (!utteranceId.equals(activeUtteranceId)) return;
            activeUtteranceId = null;
            speaking = false;
            if (utteranceId.startsWith("jarvis_ai_reply_")) waitingForAi = false;
            scheduleRestart(350);
        });
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
        releaseScreenOffWakeLock();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopping = true;
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        releaseScreenOffWakeLock();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
