package com.jarvis.assistant;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView status, transcript;
    private TextToSpeech tts;
    private SpeechRecognizer recognizer;
    private static final int MIC_PERMISSION_REQUEST = 1001;

    private int cyan = Color.rgb(70, 220, 255);
    private int bg = Color.rgb(3, 10, 22);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        buildInterface();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("en", "IN"));
                tts.setSpeechRate(0.92f);
            }
        });
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                public void onReadyForSpeech(Bundle params) { status.setText("LISTENING…"); }
                public void onBeginningOfSpeech() { status.setText("HEARING YOU"); }
                public void onRmsChanged(float rmsdB) {}
                public void onBufferReceived(byte[] buffer) {}
                public void onEndOfSpeech() { status.setText("ANALYZING"); }
                public void onError(int error) { status.setText("STANDBY • TAP TO SPEAK"); }
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) handleCommand(matches.get(0));
                    status.setText("STANDBY • TAP TO SPEAK");
                }
                public void onPartialResults(Bundle partialResults) {}
                public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 36, 24, 24);
        root.setBackgroundColor(bg);

        TextView label = new TextView(this);
        label.setText("STARK INDUSTRIES  •  PERSONAL AI");
        label.setTextColor(cyan);
        label.setTextSize(12);
        label.setLetterSpacing(0.16f);
        label.setGravity(Gravity.CENTER);
        root.addView(label, new LinearLayout.LayoutParams(-1, -2));

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S.");
        title.setTextColor(Color.WHITE);
        title.setTextSize(36);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 26, 0, 4);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView orb = new TextView(this);
        orb.setText("◉");
        orb.setTextColor(cyan);
        orb.setTextSize(90);
        orb.setGravity(Gravity.CENTER);
        orb.setPadding(0, 14, 0, 14);
        root.addView(orb, new LinearLayout.LayoutParams(-1, 0, 1));

        status = new TextView(this);
        status.setText("SYSTEM ONLINE • STANDBY");
        status.setTextColor(cyan);
        status.setTextSize(12);
        status.setGravity(Gravity.CENTER);
        status.setLetterSpacing(0.12f);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        transcript = new TextView(this);
        transcript.setText("Good evening. JARVIS interface initialized.\nTap the microphone control to speak.");
        transcript.setTextColor(Color.rgb(190, 215, 230));
        transcript.setTextSize(15);
        transcript.setGravity(Gravity.CENTER);
        transcript.setPadding(8, 22, 8, 22);
        root.addView(transcript, new LinearLayout.LayoutParams(-1, -2));

        Button speak = new Button(this);
        speak.setText("◉  ACTIVATE VOICE");
        speak.setTextColor(bg);
        speak.setTextSize(14);
        speak.setAllCaps(false);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(cyan);
        shape.setCornerRadius(48);
        speak.setBackground(shape);
        speak.setOnClickListener(v -> startListening());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, 60);
        bp.setMargins(8, 12, 8, 18);
        root.addView(speak, bp);

        TextView footer = new TextView(this);
        footer.setText("VOICE CONTROL  /  PERSONAL ASSISTANT  /  V1.0");
        footer.setTextColor(Color.rgb(90, 130, 150));
        footer.setTextSize(9);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private void startListening() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST);
            status.setText("MICROPHONE PERMISSION NEEDED");
            return;
        }
        if (recognizer == null) {
            status.setText("VOICE RECOGNITION UNAVAILABLE");
            return;
        }
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS");
            recognizer.startListening(intent);
        } catch (Exception e) {
            status.setText("COULD NOT START MICROPHONE");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIC_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startListening();
            else status.setText("MICROPHONE PERMISSION DENIED");
        }
    }

    private void handleCommand(String command) {
        transcript.setText("YOU: " + command);
        String original = command == null ? "" : command.trim();
        String lower = original.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();

        // Recognize common ways the user may pronounce or address JARVIS.
        String cleaned = lower.replaceFirst("^(hey |hi |hello |oye |oy |arey |are |ok |okay |please )*", "");
        cleaned = cleaned.replaceFirst("^(jarvis|jarvesh|jarvish|jarvish|jarvis ji|j a r v i s)(\\s+)", "");
        String intent = cleaned.trim();
        if (intent.isEmpty() || intent.matches("^(jarvis|jarvesh|jarvish|jarvish ji|j a r v i s)$")) {
            String[] greetings = {
                "Yes, I'm listening. Tell me, what do you need?",
                "I'm here. Go ahead, I'm listening.",
                "At your service. What's up?",
                "Yes Boss—I'm listening. Tell me."
            };
            replyAndShow(original, greetings[(int)(System.currentTimeMillis() % greetings.length)]);
            return;
        }

        String reply;
        if (intent.matches(".*\\b(hello|hi|hey|namaste|namaskar)\\b.*")) {
            reply = "Hello! I'm here. What can I do for you?";
        } else if (intent.contains("kaise ho") || intent.contains("how are you")) {
            reply = "I'm running well and ready to help you. Tum batao, kya haal hai?";
        } else if (intent.contains("kya kar rahe") || intent.contains("what are you doing")) {
            reply = "Main tumhari help ke liye ready hoon. Batao, kya karna hai?";
        } else if (intent.contains("thank") || intent.contains("shukriya") || intent.contains("dhanyavad")) {
            reply = "You're welcome! Main yahin hoon, jab bhi help chahiye.";
        } else if (intent.contains("time") || intent.contains("samay") || intent.contains("kitne baje")) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault());
            reply = "Abhi time hai " + fmt.format(new java.util.Date());
        } else if (intent.contains("date") || intent.contains("tarikh") || intent.contains("aaj ki date")) {
            java.text.SimpleDateFormat dateFmt = new java.text.SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("en", "IN"));
            reply = "Aaj " + dateFmt.format(new java.util.Date());
        } else if (intent.contains("battery") || intent.contains("charge kitna")) {
            Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent != null) {
                int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int percent = (level >= 0 && scale > 0) ? (level * 100 / scale) : -1;
                reply = percent >= 0 ? "Battery " + percent + " percent hai." : "Battery level read nahi ho paya.";
            } else reply = "Battery level read nahi ho paya.";
        } else if (intent.contains("open settings") || intent.contains("settings kholo") || intent.contains("phone settings") || intent.equals("settings")) {
            speak("Opening Settings.");
            try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
            catch (Exception e) { transcript.append("\\nSettings abhi nahi khul pa raha."); }
            return;
        } else if (intent.contains("set alarm") || intent.contains("alarm lagao") || intent.contains("alarm kholo")) {
            speak("Alarm screen khol raha hoon. Time select karke confirm kar dena.");
            try { startActivity(new Intent(AlarmClock.ACTION_SET_ALARM)); }
            catch (Exception e) { transcript.append("\\nAlarm app available nahi hai."); }
            return;
        } else if (intent.contains("search for ") || intent.startsWith("google ") || intent.startsWith("search ")) {
            String query = intent.replaceFirst("^.*?search for ", "").replaceFirst("^google ", "").replaceFirst("^search ", "").trim();
            if (query.isEmpty()) query = intent;
            speak("Searching the web for " + query);
            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode(query)))); }
            catch (Exception e) { transcript.append("\\nWeb search nahi khul paaya."); }
            return;
        } else if (intent.contains("open youtube") || intent.contains("youtube kholo") || intent.contains("youtube chalao") || intent.equals("youtube")) {
            speak("YouTube khol raha hoon.");
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                if (launch != null) startActivity(launch);
                else startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com")));
            } catch (Exception e) { transcript.append("\\nYouTube nahi khul paaya."); }
            return;
        } else if (intent.contains("open chrome") || intent.contains("chrome kholo") || intent.equals("chrome")) {
            speak("Chrome khol raha hoon.");
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
                if (launch != null) startActivity(launch);
                else startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com")));
            } catch (Exception e) { transcript.append("\\nChrome nahi khul paaya."); }
            return;
        } else if (intent.contains("open whatsapp") || intent.contains("whatsapp kholo") || intent.equals("whatsapp")) {
            launchApp("com.whatsapp", "WhatsApp", "https://www.whatsapp.com"); return;
        } else if (intent.contains("open instagram") || intent.contains("instagram kholo") || intent.equals("instagram")) {
            launchApp("com.instagram.android", "Instagram", "https://www.instagram.com"); return;
        } else if (intent.contains("open facebook") || intent.contains("facebook kholo") || intent.equals("facebook")) {
            launchApp("com.facebook.katana", "Facebook", "https://www.facebook.com"); return;
        } else if (intent.contains("your name") || intent.contains("who are you") || intent.contains("tum kaun ho") || intent.contains("tera naam")) {
            reply = "Main JARVIS hoon, tumhara personal assistant. Batao, kya help karun?";
        } else if (intent.contains("listen") || intent.contains("suno") || intent.contains("meri baat sun")) {
            reply = "Haan, sun raha hoon. Bolo, kya baat hai?";
        } else {
            reply = "Maine suna: " + original + ". Is baat ko abhi poori tarah samajh nahi paaya, lekin tum doosre tareeke se bolo, main try karta hoon.";
        }
        replyAndShow(original, reply);
    }

    private void replyAndShow(String original, String reply) {
        transcript.setText("YOU: " + original + "\\n\\nJARVIS: " + reply);
        speak(reply);
    }

    private void launchApp(String packageName, String appName, String fallbackUrl) {
        String reply = "Opening " + appName + ".";
        speak(reply);
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launch != null) {
                startActivity(launch);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(fallbackUrl)));
            }
        } catch (Exception e) {
            transcript.append("\nUnable to open " + appName + " right now.");
        }
    }

    private void speak(String text) {
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply");
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
