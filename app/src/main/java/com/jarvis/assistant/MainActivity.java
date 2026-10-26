package com.jarvis.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.speech.SpeechRecognizer;
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

    private void handleCommand(String command) {
        transcript.setText("YOU: " + command);
        String lower = command.toLowerCase(Locale.ROOT);
        String reply;
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("jarvis")) {
            reply = "At your service. How may I assist you?";
        } else if (lower.contains("time")) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault());
            reply = "The time is " + fmt.format(new java.util.Date());
        } else if (lower.contains("open youtube")) {
            reply = "Opening YouTube.";
            speak(reply);
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                if (launch != null) startActivity(launch);
                else startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com")));
            } catch (Exception e) {
                transcript.append("\nUnable to open YouTube right now.");
            }
            return;
        } else if (lower.contains("open chrome")) {
            reply = "Opening Chrome.";
            speak(reply);
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
                if (launch != null) startActivity(launch);
                else startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com")));
            } catch (Exception e) {
                transcript.append("\nUnable to open Chrome right now.");
            }
            return;
        } else if (lower.contains("your name") || lower.contains("who are you")) {
            reply = "I am JARVIS, your personal assistant. Systems are standing by.";
        } else {
            reply = "I heard you say: " + command + ". My command library is still growing.";
        }
        transcript.setText("YOU: " + command + "\n\nJARVIS: " + reply);
        speak(reply);
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
