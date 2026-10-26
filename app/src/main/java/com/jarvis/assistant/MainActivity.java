package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.InputType;
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
    private boolean startWakeAfterPermission = false;
    private SharedPreferences preferences;

    private int cyan = Color.rgb(70, 220, 255);
    private int bg = Color.rgb(3, 10, 22);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        preferences = getSharedPreferences("jarvis_preferences", MODE_PRIVATE);
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
        orb.setGravity(Gravity.CENTER);
        orb.setPadding(0, 14, 0, 14);
        orb.setBackgroundResource(com.jarvis.assistant.R.drawable.jarvis_orb);
        root.addView(orb, new LinearLayout.LayoutParams(-1, 0, 1));
        android.view.animation.ScaleAnimation pulse = new android.view.animation.ScaleAnimation(
                0.94f, 1.06f, 0.94f, 1.06f, android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(1300);
        pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
        pulse.setRepeatCount(android.view.animation.Animation.INFINITE);
        orb.startAnimation(pulse);

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

        Button wakeOn = new Button(this);
        wakeOn.setText("ENABLE HEY JARVIS (BACKGROUND)");
        wakeOn.setAllCaps(false);
        wakeOn.setTextColor(Color.WHITE);
        wakeOn.setOnClickListener(v -> startWakeWordListening());
        root.addView(wakeOn, new LinearLayout.LayoutParams(-1, 52));

        Button wakeOff = new Button(this);
        wakeOff.setText("STOP BACKGROUND LISTENING");
        wakeOff.setAllCaps(false);
        wakeOff.setTextColor(Color.WHITE);
        wakeOff.setOnClickListener(v -> {
            stopService(new Intent(MainActivity.this, WakeWordService.class));
            status.setText("BACKGROUND LISTENING STOPPED");
        });
        root.addView(wakeOff, new LinearLayout.LayoutParams(-1, 48));

        Button health = new Button(this);
        health.setText("RUN SYSTEM SAFETY CHECK");
        health.setAllCaps(false);
        health.setTextColor(cyan);
        health.setOnClickListener(v -> runSafetyCheck());
        root.addView(health, new LinearLayout.LayoutParams(-1, 48));

        Button emergencyContact = new Button(this);
        emergencyContact.setText("SET EMERGENCY CONTACT");
        emergencyContact.setAllCaps(false);
        emergencyContact.setTextColor(Color.WHITE);
        emergencyContact.setOnClickListener(v -> showEmergencyContactDialog());
        root.addView(emergencyContact, new LinearLayout.LayoutParams(-1, 48));

        Button sos = new Button(this);
        sos.setText("SOS • EMERGENCY OPTIONS");
        sos.setAllCaps(false);
        sos.setTextColor(Color.WHITE);
        sos.setOnClickListener(v -> showSosOptions());
        root.addView(sos, new LinearLayout.LayoutParams(-1, 48));

        TextView footer = new TextView(this);
        footer.setText("VOICE CONTROL  /  SAFE MODE  /  V1.1");
        footer.setTextColor(Color.rgb(90, 130, 150));
        footer.setTextSize(9);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private void runSafetyCheck() {
        StringBuilder report = new StringBuilder("JARVIS SAFETY CHECK\\n\\n");
        boolean speechAvailable = SpeechRecognizer.isRecognitionAvailable(this);
        report.append(speechAvailable ? "✓ Speech recognition available\\n" : "! Speech recognition unavailable\\n");
        report.append(tts != null ? "✓ Voice engine initialized\\n" : "! Voice engine not initialized\\n");
        report.append(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                ? "✓ Microphone permission granted\\n" : "! Microphone permission not granted\\n");
        report.append("✓ App data remains in Android's private app storage\\n");
        report.append("\\nSafe update policy: keep the previous APK until the new build is verified. Install updates only from a trusted source and confirm Android's installer prompt. If a build fails, use the last known working APK.");
        transcript.setText(report.toString());
        status.setText("SAFETY CHECK COMPLETE");
        speak("Safety check complete. Please review the report on screen.");
    }

    private void startWakeWordListening() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            startWakeAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST);
            status.setText("MICROPHONE PERMISSION NEEDED");
            return;
        }
        try {
            Intent service = new Intent(this, WakeWordService.class);
            if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
            status.setText("HEY JARVIS LISTENING • CHECK NOTIFICATION");
        } catch (Exception e) {
            status.setText("COULD NOT START BACKGROUND LISTENING");
        }
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (startWakeAfterPermission) { startWakeAfterPermission = false; startWakeWordListening(); }
                else startListening();
            }
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

        // Local, user-controlled preferences and memory. These never modify application code.
        if (intent.startsWith("remember that ") || intent.startsWith("remember ")) {
            String note = intent.replaceFirst("^remember( that)?\\s+", "").trim();
            if (note.isEmpty()) {
                replyAndShow(original, "Batao kya yaad rakhna hai.");
            } else {
                String old = preferences.getString("notes", "");
                String updated = old.isEmpty() ? note : old + "\\n" + note;
                String[] notes = updated.split("\\n");
                StringBuilder kept = new StringBuilder();
                int start = Math.max(0, notes.length - 10);
                for (int i = start; i < notes.length; i++) {
                    if (kept.length() > 0) kept.append("\\n");
                    kept.append(notes[i]);
                }
                preferences.edit().putString("notes", kept.toString()).apply();
                replyAndShow(original, "Theek hai, maine ye baat is phone par local memory mein save kar li.");
            }
            return;
        }
        if (intent.contains("what do you remember") || intent.contains("meri memory") || intent.contains("kya yaad hai")) {
            String notes = preferences.getString("notes", "");
            replyAndShow(original, notes.isEmpty() ? "Abhi koi saved note nahi hai. Kaho: remember that, phir apni baat." : "Tumhari saved notes: " + notes.replace("\\n", ". "));
            return;
        }
        if (intent.contains("forget my memory") || intent.contains("clear memory") || intent.contains("memory delete") || intent.contains("sab yaad bhool")) {
            new AlertDialog.Builder(this).setTitle("Clear JARVIS memory")
                    .setMessage("Saved notes on this phone will be deleted. Emergency contact and voice style settings will stay.")
                    .setNegativeButton("Cancel", (d, w) -> {})
                    .setPositiveButton("Clear notes", (d, w) -> {
                        preferences.edit().remove("notes").apply();
                        replyAndShow(original, "Saved notes clear kar diye.");
                    }).show();
            return;
        }
        if (intent.contains("calm mode") || intent.contains("shaant mode")) {
            preferences.edit().putString("voice_style", "calm").apply();
            replyAndShow(original, "Calm mode on. Main dheere aur shaant awaaz mein jawab dene ki koshish karunga.");
            return;
        }
        if (intent.contains("energetic mode") || intent.contains("fast voice")) {
            preferences.edit().putString("voice_style", "energetic").apply();
            replyAndShow(original, "Energetic mode on. Main thoda zyada energetic bolunga.");
            return;
        }
        if (intent.contains("normal voice") || intent.contains("normal mode")) {
            preferences.edit().putString("voice_style", "normal").apply();
            replyAndShow(original, "Normal voice mode on.");
            return;
        }
        if (intent.contains("set emergency contact") || intent.contains("emergency contact set") || intent.contains("emergency contact")) {
            showEmergencyContactDialog();
            return;
        }
        if (intent.equals("sos") || intent.contains("emergency options") || intent.contains("sos options")) {
            showSosOptions();
            return;
        }
        if (intent.contains("help commands") || intent.equals("what can you do") || intent.contains("commands list")) {
            replyAndShow(original, "I can open apps and settings, tell time and battery, set alarms, search the web, save local notes with remember that, read notes with what do you remember, change calm or energetic voice mode, and open SOS options.");
            return;
        }

        // Emergency phrases open a clear confirmation before the emergency dialer.
        if (intent.contains("accident") || intent.contains("emergency") || intent.contains("sos")
                || intent.contains("help me urgently") || intent.contains("bachao") || intent.contains("madad karo abhi")) {
            speak("Emergency help may be needed. Please confirm on screen.");
            new AlertDialog.Builder(this)
                    .setTitle("JARVIS • Emergency help")
                    .setMessage("If you are in immediate danger or injured, call India's emergency number 112. JARVIS cannot reliably detect an accident automatically. Open the dialer now?")
                    .setNegativeButton("Not now", (dialog, which) -> {
                        status.setText("EMERGENCY DIAL CANCELLED");
                    })
                    .setPositiveButton("OPEN 112 DIALER", (dialog, which) -> {
                        try {
                            startActivity(new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:112")));
                        } catch (Exception e) {
                            status.setText("PLEASE CALL 112 FROM YOUR PHONE");
                        }
                    })
                    .show();
            return;
        }

        // Mood-aware supportive replies: respond gently, without pretending to know more than was said.
        if (intent.contains("udaas") || intent.contains("sad") || intent.contains("dil kharab")
                || intent.contains("mann nahi lag") || intent.contains("man nahi lag")
                || intent.contains("rona aa raha") || intent.contains("pareshan hoon")) {
            replyAndShow(original, "Bhai, lag raha hai abhi waqt thoda mushkil hai. Main sun raha hoon. Aaram se batao kya hua—main judge nahi karunga. Agar tum chaho, hum ek-ek karke sochenge.");
            return;
        }
        if (intent.contains("darr lag") || intent.contains("dar lag") || intent.contains("ghabra")
                || intent.contains("anxiety") || intent.contains("tension ho rahi")) {
            replyAndShow(original, "Theek hai, pehle dheere se saans lo aur apne aap ko safe jagah par rakho. Main sun raha hoon. Kis baat se darr ya tension ho rahi hai?");
            return;
        }
        if (intent.contains("khush hoon") || intent.contains("bahut khush") || intent.contains("happy hoon")
                || intent.contains("good news")) {
            replyAndShow(original, "Arey wah, Boss! Ye toh achhi baat hai. Batao kya hua—saath mein celebrate karte hain!");
            return;
        }
        if (intent.contains("akela feel") || intent.contains("lonely") || intent.contains("koi nahi hai")
                || intent.contains("koi sunta nahi")) {
            replyAndShow(original, "Bhai, tum jo keh rahe ho main dhyan se sununga. Tumhe sab kuch ek saath explain karne ki zaroorat nahi—jitna comfortable ho utna batao. Agar mumkin ho toh kisi bharosemand insaan se bhi baat karna.");
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
        } else if (intent.contains("open camera") || intent.contains("camera kholo") || intent.equals("camera")) {
            speak("Camera khol raha hoon.");
            try { startActivity(new Intent("android.media.action.IMAGE_CAPTURE")); }
            catch (Exception e) { transcript.append("\\nCamera abhi nahi khul paaya."); }
            return;
        } else if (intent.contains("open maps") || intent.contains("maps kholo") || intent.contains("google maps") || intent.contains("map kholo")) {
            speak("Maps khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q="))); }
            catch (Exception e) { transcript.append("\\nMaps abhi nahi khul paaya."); }
            return;
        } else if (intent.contains("open calculator") || intent.contains("calculator kholo") || intent.equals("calculator")) {
            speak("Calculator khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)); }
            catch (Exception e) { transcript.append("\\nCalculator app nahi mili."); }
            return;
        } else if (intent.contains("open calendar") || intent.contains("calendar kholo") || intent.equals("calendar")) {
            speak("Calendar khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)); }
            catch (Exception e) { transcript.append("\\nCalendar app nahi mili."); }
            return;
        } else if (intent.contains("open clock") || intent.contains("clock kholo") || intent.contains("ghadi kholo")) {
            speak("Clock khol raha hoon.");
            try { startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS)); }
            catch (Exception e) { transcript.append("\\nClock app nahi mili."); }
            return;
        } else if (intent.contains("wifi settings") || intent.contains("wifi kholo")) {
            speak("Wi-Fi settings khol raha hoon.");
            try { startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)); }
            catch (Exception e) { transcript.append("\\nWi-Fi settings nahi khul paayi."); }
            return;
        } else if (intent.contains("bluetooth settings") || intent.contains("bluetooth kholo")) {
            speak("Bluetooth settings khol raha hoon.");
            try { startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)); }
            catch (Exception e) { transcript.append("\\nBluetooth settings nahi khul paayi."); }
            return;
        } else if (intent.contains("location settings") || intent.contains("location kholo")) {
            speak("Location settings khol raha hoon.");
            try { startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)); }
            catch (Exception e) { transcript.append("\\nLocation settings nahi khul paayi."); }
            return;
        } else if (intent.matches("^(call|phone karo|call karo)\\s+.+")) {
            String target = intent.replaceFirst("^(call|phone karo|call karo)\\s+", "").trim();
            speak("Calling screen khol raha hoon. Number check karke call confirm kar dena.");
            try {
                String digits = target.replaceAll("[^0-9+]", "");
                if (digits.length() >= 5) startActivity(new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + digits)));
                else { Intent dial = new Intent(Intent.ACTION_DIAL); startActivity(dial); }
            } catch (Exception e) { transcript.append("\\nPhone dialer nahi khul paaya."); }
            return;
        } else if (intent.startsWith("message ") || intent.startsWith("sms ") || intent.startsWith("text ")) {
            String body = intent.replaceFirst("^(message|sms|text)\\s+", "").trim();
            speak("Message draft khol raha hoon. Bhejne se pehle check kar lena.");
            try {
                Intent sms = new Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:"));
                sms.putExtra("sms_body", body);
                startActivity(sms);
            } catch (Exception e) { transcript.append("\\nMessage app nahi khul paayi."); }
            return;
        } else if (intent.contains("open phone") || intent.contains("dialer kholo") || intent.contains("call screen kholo") || intent.equals("dialer")) {
            speak("Phone dialer khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_DIAL)); }
            catch (Exception e) { transcript.append("\\nPhone app nahi khul paayi."); }
            return;
        } else if (intent.contains("open contacts") || intent.contains("contacts kholo") || intent.contains("contact kholo")) {
            speak("Contacts khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("content://contacts/people/"))); }
            catch (Exception e) {
                try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS)); }
                catch (Exception ignored) { transcript.append("\\nContacts app nahi khul paayi."); }
            }
            return;
        } else if (intent.contains("joke") || intent.contains("jokes") || intent.contains("mazaak")) {
            String[] jokes = {
                "Bhai, computer ko thand kyun lagti hai? Kyunki uske paas Windows hain!",
                "Ek phone ne doosre se kaha: tu itna smart kaise? Doosra bola: mere paas achha network hai!",
                "Main JARVIS hoon, bhai. Mere jokes bhi system update ke saath aate hain!"
            };
            reply = jokes[(int)(System.currentTimeMillis() % jokes.length)];
        } else if (intent.contains("motivate") || intent.contains("motivation") || intent.contains("himmat de")) {
            reply = "Bhai, ek-ek kadam se hi manzil milti hai. Rukna mat—aaram kar, phir dobara koshish kar. Main help ke liye yahin hoon.";
        } else if (intent.contains("who made you") || intent.contains("kisne banaya") || intent.contains("tumhe kisne banaya")) {
            reply = "Mujhe tumhare JARVIS project ke roop mein banaya ja raha hai. Abhi main basic voice commands aur phone actions handle karta hoon.";
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

    private void showEmergencyContactDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint("Phone number, e.g. +91...");
        input.setText(preferences.getString("emergency_contact", ""));
        new AlertDialog.Builder(this)
                .setTitle("Set emergency contact")
                .setMessage("Save a trusted person's number on this phone. JARVIS will open the dialer; it will not call automatically.")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> {})
                .setPositiveButton("Save", (d, w) -> {
                    String number = input.getText().toString().trim();
                    if (number.replaceAll("[^0-9]", "").length() < 5) {
                        status.setText("ENTER A VALID PHONE NUMBER");
                        return;
                    }
                    preferences.edit().putString("emergency_contact", number).apply();
                    replyAndShow("emergency contact", "Emergency contact saved on this phone.");
                }).show();
    }

    private void showSosOptions() {
        String contact = preferences.getString("emergency_contact", "");
        String[] options = contact.isEmpty()
                ? new String[]{"Open 112 dialer", "Set emergency contact"}
                : new String[]{"Open 112 dialer", "Call saved emergency contact", "Change emergency contact"};
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • SOS options")
                .setMessage("For immediate danger in India, call 112. Choose an option; JARVIS will open the dialer and you confirm the call.")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openDialer("112");
                    else if (options[which].equals("Set emergency contact") || options[which].equals("Change emergency contact")) showEmergencyContactDialog();
                    else openDialer(contact);
                }).show();
    }

    private void openDialer(String number) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + android.net.Uri.encode(number))));
        } catch (Exception e) {
            status.setText("PLEASE OPEN PHONE AND DIAL " + number);
        }
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
        if (tts != null) {
            String style = preferences == null ? "normal" : preferences.getString("voice_style", "normal");
            float rate = "calm".equals(style) ? 0.82f : ("energetic".equals(style) ? 1.05f : 0.92f);
            tts.setSpeechRate(rate);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply");
        }
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
