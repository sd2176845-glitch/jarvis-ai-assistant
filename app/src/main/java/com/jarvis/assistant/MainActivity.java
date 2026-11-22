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
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView status, transcript;
    private TextToSpeech tts;
    private SpeechRecognizer recognizer;
    private static final int MIC_PERMISSION_REQUEST = 1001;
    private static final int CONTACT_PERMISSION_REQUEST = 1002;
    private static final int CALL_PERMISSION_REQUEST = 1003;
    private static final int PHONE_EVENTS_PERMISSION_REQUEST = 1004;
    private boolean startWakeAfterPermission = false;
    private String pendingDirectCallNumber = null;
    private String pendingDirectCallName = null;
    private SharedPreferences preferences;
    private ImageView avatar;
    private android.os.CountDownTimer activeFocusTimer;
    private boolean aiOffline = false;

    private int cyan = Color.rgb(255,  70, 82);
    private int bg = Color.rgb(8, 5, 9);

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
                applySavedVoiceSettings();
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String utteranceId) {
                        runOnUiThread(() -> setVoiceStatus("SPEAKING…"));
                    }
                    @Override public void onDone(String utteranceId) {
                        runOnUiThread(() -> setVoiceStatus(aiOffline
                                ? "AI OFFLINE • LOCAL COMMANDS READY"
                                : "STANDBY • TAP TO SPEAK"));
                    }
                    @Override public void onError(String utteranceId) {
                        runOnUiThread(() -> setVoiceStatus("VOICE OUTPUT ERROR • TAP TO SPEAK"));
                    }
                });
            } else {
                setVoiceStatus("VOICE OUTPUT UNAVAILABLE");
            }
        });
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                public void onReadyForSpeech(Bundle params) { setVoiceStatus("LISTENING…"); animateOrb(1.07f, 550L); }
                public void onBeginningOfSpeech() { setVoiceStatus("HEARING YOU"); animateOrb(1.10f, 350L); }
                public void onRmsChanged(float rmsdB) {}
                public void onBufferReceived(byte[] buffer) {}
                public void onEndOfSpeech() { setVoiceStatus("PROCESSING SPEECH…"); animateOrb(1.035f, 700L); }
                public void onError(int error) {
                    setVoiceStatus("VOICE INPUT ENDED • TAP TO SPEAK");
                    animateOrb(1.0f, 800L);
                }
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        setVoiceStatus("PROCESSING COMMAND…");
                        understandVoiceCommand(matches.get(0));
                    } else {
                        setVoiceStatus("NO SPEECH RECOGNIZED • TAP TO SPEAK");
                    }
                    animateOrb(1.0f, 800L);
                }
                public void onPartialResults(Bundle partialResults) {}
                public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 28, 24, 28);
        root.setBackgroundColor(bg);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -1));

        TextView label = new TextView(this);
        label.setText("STARK INDUSTRIES");
        label.setTextColor(cyan);
        label.setTextSize(11);
        label.setLetterSpacing(0.20f);
        label.setGravity(Gravity.CENTER);
        root.addView(label, new LinearLayout.LayoutParams(-1, -2));

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S.");
        title.setTextColor(Color.WHITE);
        title.setTextSize(38);
        title.setLetterSpacing(0.14f);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 12, 0, 4);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("PERSONAL AI");
        subtitle.setTextColor(Color.rgb(190, 125, 135));
        subtitle.setTextSize(10);
        subtitle.setLetterSpacing(0.24f);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 12);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));

        FrameLayout avatarFrame = new FrameLayout(this);
        int orbArea = (int) (286 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams avatarFrameParams = new LinearLayout.LayoutParams(-1, orbArea);
        avatarFrameParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        root.addView(avatarFrame, avatarFrameParams);

        LivingInterfaceView livingInterface = new LivingInterfaceView(this);
        FrameLayout.LayoutParams livingParams = new FrameLayout.LayoutParams(
                (int) (270 * getResources().getDisplayMetrics().density),
                (int) (270 * getResources().getDisplayMetrics().density), Gravity.CENTER);
        avatarFrame.addView(livingInterface, livingParams);

        avatar = new ImageView(this);
        avatar.setImageResource(com.jarvis.assistant.R.drawable.jarvis_orb);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatar.setAdjustViewBounds(true);
        int avatarSize = (int) (232 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(
                avatarSize, avatarSize, Gravity.CENTER);
        avatarFrame.addView(avatar, avatarParams);

        android.view.animation.ScaleAnimation pulse = new android.view.animation.ScaleAnimation(
                0.985f, 1.015f, 0.985f, 1.015f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(2200);
        pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
        pulse.setRepeatCount(android.view.animation.Animation.INFINITE);
        avatar.startAnimation(pulse);

        FrameLayout dividerTrack = new FrameLayout(this);
        GradientDrawable track = new GradientDrawable();
        track.setColor(Color.rgb(58, 18, 25));
        track.setCornerRadius(4 * getResources().getDisplayMetrics().density);
        dividerTrack.setBackground(track);
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(
                (int) (210 * getResources().getDisplayMetrics().density),
                (int) (2 * getResources().getDisplayMetrics().density));
        trackParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        trackParams.bottomMargin = (int) (18 * getResources().getDisplayMetrics().density);
        root.addView(dividerTrack, trackParams);
        View dividerGlow = new View(this);
        GradientDrawable glow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.TRANSPARENT, cyan, Color.TRANSPARENT});
        glow.setCornerRadius(4 * getResources().getDisplayMetrics().density);
        dividerGlow.setBackground(glow);
        dividerTrack.addView(dividerGlow, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));

        status = new TextView(this);
        status.setText("ONLINE  •  STANDBY");
        status.setTextColor(cyan);
        status.setTextSize(12);
        status.setGravity(Gravity.CENTER);
        status.setLetterSpacing(0.16f);
        status.setPadding(0, 4, 0, 6);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        transcript = new TextView(this);
        transcript.setText("Good evening, Boss.");
        transcript.setTextColor(Color.rgb(190, 215, 230));
        transcript.setTextSize(15);
        transcript.setGravity(Gravity.CENTER);
        transcript.setPadding(8, 10, 8, 22);
        root.addView(transcript, new LinearLayout.LayoutParams(-1, -2));

        Button speak = new Button(this);
        speak.setText("◉   ACTIVATE VOICE");
        speak.setTextColor(Color.WHITE);
        speak.setTextSize(14);
        speak.setAllCaps(false);
        GradientDrawable voiceShape = new GradientDrawable();
        voiceShape.setColor(Color.rgb(95, 24, 36));
        voiceShape.setCornerRadius(48);
        voiceShape.setStroke((int) (1.2f * getResources().getDisplayMetrics().density), cyan);
        speak.setBackground(voiceShape);
        speak.setOnClickListener(v -> startListening());
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(-1,
                (int) (54 * getResources().getDisplayMetrics().density));
        voiceParams.setMargins(8, 4, 8, 12);
        root.addView(speak, voiceParams);

        Button stopVoice = new Button(this);
        stopVoice.setText("STOP FOREGROUND VOICE");
        stopVoice.setTextColor(Color.rgb(255, 190, 195));
        stopVoice.setTextSize(12);
        stopVoice.setAllCaps(false);
        GradientDrawable stopShape = new GradientDrawable();
        stopShape.setColor(Color.rgb(30, 10, 15));
        stopShape.setCornerRadius(42);
        stopShape.setStroke((int) (1.0f * getResources().getDisplayMetrics().density),
                Color.rgb(155, 45, 58));
        stopVoice.setBackground(stopShape);
        stopVoice.setOnClickListener(v -> stopVoiceSession());
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(-1,
                (int) (46 * getResources().getDisplayMetrics().density));
        stopParams.setMargins(8, 0, 8, 12);
        root.addView(stopVoice, stopParams);

        Button commandCenter = new Button(this);
        commandCenter.setText("COMMAND CENTER   ⌄");
        commandCenter.setTextColor(cyan);
        commandCenter.setTextSize(13);
        commandCenter.setAllCaps(false);
        GradientDrawable centerShape = new GradientDrawable();
        centerShape.setColor(Color.rgb(24, 9, 14));
        centerShape.setCornerRadius(48);
        centerShape.setStroke((int) (1.2f * getResources().getDisplayMetrics().density),
                Color.rgb(155, 45, 58));
        commandCenter.setBackground(centerShape);
        commandCenter.setOnClickListener(v -> showCommandCenter());
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(-1,
                (int) (50 * getResources().getDisplayMetrics().density));
        centerParams.setMargins(8, 0, 8, 10);
        root.addView(commandCenter, centerParams);

        TextView footer = new TextView(this);
        footer.setText("VOICE INTERFACE  •  SAFE MODE");
        footer.setTextColor(Color.rgb(130, 85, 95));
        footer.setTextSize(9);
        footer.setLetterSpacing(0.12f);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 12, 0, 4);
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));

        setContentView(scroll);
    }

    /**
     * Lightweight movie-style HUD around the existing orb: rotating cyan arcs,
     * a subtle pulse and mirrored sound-wave bars. The animation is decorative;
     * microphone activity is still reported by the status label.
     */
    private class LivingInterfaceView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float density = getResources().getDisplayMetrics().density;
        private final ValueAnimator animator;
        private float phase = 0f;

        LivingInterfaceView(android.content.Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            animator = ValueAnimator.ofFloat(0f, 360f);
            animator.setDuration(9000L);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(value -> {
                phase = (Float) value.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float unit = Math.min(getWidth(), getHeight()) / 2f;
            canvas.save();
            canvas.rotate(phase, cx, cy);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.2f * density);
            paint.setColor(Color.argb(115, 190, 45, 65));
            canvas.drawCircle(cx, cy, unit * 0.94f, paint);
            paint.setColor(Color.argb(165, 255, 75, 90));
            canvas.drawCircle(cx, cy, unit * 0.82f, paint);
            paint.setColor(Color.argb(95, 150, 25, 55));
            canvas.drawCircle(cx, cy, unit * 0.70f, paint);

            RectF arc = new RectF(cx - unit * 0.88f, cy - unit * 0.88f,
                    cx + unit * 0.88f, cy + unit * 0.88f);
            paint.setStrokeWidth(2.1f * density);
            paint.setColor(Color.argb(220, 255, 65, 80));
            canvas.drawArc(arc, 20f, 64f, false, paint);
            canvas.drawArc(arc, 195f, 82f, false, paint);
            arc.inset(12f * density, 12f * density);
            paint.setColor(Color.argb(160, 200, 35, 65));
            canvas.drawArc(arc, 110f, 44f, false, paint);
            canvas.drawArc(arc, 285f, 40f, false, paint);
            canvas.restore();

            // Mirrored waveform visible on either side of the central orb.
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(1.7f * density);
            for (int i = 0; i < 13; i++) {
                float xOffset = (25f + i * 5.5f) * density;
                float wave = (float) Math.abs(Math.sin(Math.toRadians(phase * 2.0f + i * 29f)));
                float h = (5f + wave * (7f + (i % 4) * 2.5f)) * density;
                int alpha = 95 + (int) (100 * wave);
                paint.setColor(Color.argb(alpha, 255, 75, 90));
                float leftX = cx - xOffset;
                float rightX = cx + xOffset;
                canvas.drawLine(leftX, cy - h, leftX, cy + h, paint);
                canvas.drawLine(rightX, cy - h, rightX, cy + h, paint);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override protected void onDetachedFromWindow() {
            animator.cancel();
            super.onDetachedFromWindow();
        }
    }

    private void openVisionTools() {
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Vision tools")
                .setMessage("Open the camera to capture a photo, or open Google Lens in your browser to identify objects and read text from an image. Lens requires internet and may ask you to upload/select a photo.")
                .setPositiveButton("Open Camera", (d, w) -> {
                    try { startActivity(new Intent("android.media.action.IMAGE_CAPTURE")); }
                    catch (Exception e) { status.setText("CAMERA NOT AVAILABLE"); }
                })
                .setNeutralButton("Open Google Lens", (d, w) -> {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://lens.google.com/"))); }
                    catch (Exception e) { status.setText("BROWSER NOT AVAILABLE"); }
                })
                .setNegativeButton("Close", null).show();
    }

    private void showTaskAndReminderHub() {
        String structured = getStructuredTaskSummary();
        String notes = getSavedNotes();
        String summary = structured;
        if (!notes.isEmpty()) summary += "\n\nLEGACY NOTES (unchanged)\n" + notes;
        new AlertDialog.Builder(this).setTitle("JARVIS • Tasks & reminders")
                .setMessage(summary + "\n\nTasks are stored locally in a separate structured list. Existing notes are preserved. Alarm time must be confirmed in Android Clock.")
                .setPositiveButton("Manage tasks", (d, w) -> showStructuredTaskManager())
                .setNeutralButton("Set alarm", (d, w) -> {
                    try {
                        Intent alarm = new Intent(AlarmClock.ACTION_SET_ALARM);
                        alarm.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
                        startActivity(alarm);
                    } catch (Exception e) { status.setText("ALARM APP NOT AVAILABLE"); }
                })
                .setNegativeButton("New event", (d, w) -> showCalendarEventDraftDialog()).show();
    }

    private org.json.JSONArray readStructuredTasks() {
        String raw = preferences.getString("structured_tasks_v1", "[]");
        try {
            return new org.json.JSONArray(raw);
        } catch (org.json.JSONException e) {
            return null;
        }
    }

    private boolean saveStructuredTasks(org.json.JSONArray tasks) {
        if (tasks == null) return false;
        preferences.edit().putString("structured_tasks_v1", tasks.toString()).apply();
        return true;
    }

    private String getStructuredTaskSummary() {
        org.json.JSONArray tasks = readStructuredTasks();
        if (tasks == null) return "Structured task data needs attention; saved data has been preserved.";
        if (tasks.length() == 0) return "TASK LIST\nNo structured tasks yet.";
        int pending = 0;
        int completed = 0;
        for (int i = 0; i < tasks.length(); i++) {
            org.json.JSONObject item = tasks.optJSONObject(i);
            if (item == null) continue;
            if (item.optBoolean("done", false)) completed++;
            else pending++;
        }
        return "TASK LIST\nPending: " + pending + "  •  Completed: " + completed;
    }

    private void showStructuredTaskManager() {
        org.json.JSONArray tasks = readStructuredTasks();
        if (tasks == null) {
            new AlertDialog.Builder(this).setTitle("JARVIS • Tasks")
                    .setMessage("Task data could not be read. Nothing has been changed.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < tasks.length(); i++) {
            org.json.JSONObject item = tasks.optJSONObject(i);
            labels.add(item == null ? "Untitled task"
                    : (item.optBoolean("done", false) ? "✓  " : "○  ")
                    + item.optString("title", "Untitled task"));
        }
        String[] entries = labels.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("JARVIS • Structured tasks")
                .setItems(entries, (dialog, which) -> {
                    org.json.JSONObject item = tasks.optJSONObject(which);
                    if (item != null) {
                        try {
                            item.put("done", !item.optBoolean("done", false));
                            saveStructuredTasks(tasks);
                            status.setText(item.optBoolean("done", false) ? "TASK COMPLETED" : "TASK REOPENED");
                        } catch (org.json.JSONException e) {
                            status.setText("TASK STATUS COULD NOT BE UPDATED");
                        }
                    }
                    showStructuredTaskManager();
                })
                .setPositiveButton("Add task", (dialog, which) -> showAddStructuredTaskDialog())
                .setNeutralButton("Delete task", (dialog, which) -> showDeleteStructuredTaskDialog())
                .setNegativeButton("Close", null);
        if (entries.length == 0) {
            builder.setMessage("No tasks yet. Tap Add task to create your first one.");
        } else {
            builder.setMessage("Tap a task to switch its completion status.");
        }
        builder.show();
    }

    private void showAddStructuredTaskDialog() {
        EditText input = new EditText(this);
        input.setHint("What needs to be done?");
        input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("JARVIS • Add task")
                .setMessage("This task stays in JARVIS private app storage on this phone.")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save task", (dialog, which) -> {
                    String title = input.getText().toString().trim();
                    if (title.isEmpty()) {
                        status.setText("EMPTY TASK NOT SAVED");
                        return;
                    }
                    org.json.JSONArray tasks = readStructuredTasks();
                    if (tasks == null) {
                        status.setText("TASK DATA COULD NOT BE READ");
                        return;
                    }
                    org.json.JSONObject item = new org.json.JSONObject();
                    try {
                        item.put("title", title);
                        item.put("done", false);
                        item.put("created_at", System.currentTimeMillis());
                        tasks.put(item);
                        saveStructuredTasks(tasks);
                        status.setText("TASK SAVED ON DEVICE");
                        showStructuredTaskManager();
                    } catch (org.json.JSONException e) {
                        status.setText("TASK COULD NOT BE SAVED");
                    }
                }).show();
    }

    private void showDeleteStructuredTaskDialog() {
        org.json.JSONArray tasks = readStructuredTasks();
        if (tasks == null || tasks.length() == 0) {
            status.setText(tasks == null ? "TASK DATA COULD NOT BE READ" : "NO TASKS TO DELETE");
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < tasks.length(); i++) {
            org.json.JSONObject item = tasks.optJSONObject(i);
            labels.add(item == null ? "Untitled task" : item.optString("title", "Untitled task"));
        }
        new AlertDialog.Builder(this).setTitle("JARVIS • Delete task")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                    tasks.remove(which);
                    saveStructuredTasks(tasks);
                    status.setText("TASK DELETED");
                    showStructuredTaskManager();
                })
                .setNegativeButton("Cancel", null).show();
    }


    // M08 safe foundation: owner-created, manually-run checklists only.
    // No background triggers, cross-app actions, calls, messages, or device changes.
    private org.json.JSONArray readManualWorkflows() {
        try {
            return new org.json.JSONArray(preferences.getString("manual_workflows_v1", "[]"));
        } catch (org.json.JSONException e) {
            return null;
        }
    }

    private boolean saveManualWorkflows(org.json.JSONArray workflows) {
        if (workflows == null) return false;
        preferences.edit().putString("manual_workflows_v1", workflows.toString()).apply();
        return true;
    }

    // M09 extends the existing notes and recent-conversation stores; it creates no parallel memory database.
    // The timeline is owner-created and stored as one structured key in the existing JARVIS preferences.
    private void showPersonalMemoryVault() {
        String savedNotes = getSavedNotes();
        SharedPreferences conversationStore = getSharedPreferences("jarvis_ai_memory", MODE_PRIVATE);
        String recentConversation = conversationStore.getString("conversation", "");
        String summary = "PERSONAL KNOWLEDGE\n"
                + (savedNotes.isEmpty() ? "No saved notes yet." : savedNotes)
                + "\n\nRECENT CONVERSATION CONTEXT\n"
                + (recentConversation.isEmpty() ? "No recent conversation context." : recentConversation)
                + "\n\nTIMELINE\n"
                + getPersonalTimelineCount() + " owner-created entries"
                + "\n\nAll information stays in existing on-device JARVIS storage. Review before exporting.";
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        TextView content = new TextView(this);
        content.setText(summary);
        content.setTextColor(Color.WHITE);
        content.setTextSize(14);
        content.setPadding(28, 20, 28, 16);
        panel.addView(content, new LinearLayout.LayoutParams(-1, -2));
        Button editNotes = new Button(this);
        editNotes.setText("EDIT SAVED NOTES");
        editNotes.setOnClickListener(v -> showEditVaultNotes());
        panel.addView(editNotes, new LinearLayout.LayoutParams(-1, -2));
        Button timeline = new Button(this);
        timeline.setText("OPEN PERSONAL TIMELINE");
        timeline.setOnClickListener(v -> showPersonalTimeline());
        panel.addView(timeline, new LinearLayout.LayoutParams(-1, -2));
        Button memoryExpansion = new Button(this);
        memoryExpansion.setText("MEMORY EXPANSION STUDIO");
        memoryExpansion.setOnClickListener(v ->
                MemoryExpansionStudio.show(this, preferences, getFilesDir(), null));
        panel.addView(memoryExpansion, new LinearLayout.LayoutParams(-1, -2));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(panel);
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Personal Memory Vault")
                .setView(scroll)
                .setPositiveButton("Export", (d, w) -> exportPersonalMemory())
                .setNegativeButton("Clear recent context", (d, w) -> confirmClearRecentContext())
                .setNeutralButton("Close", null)
                .show();
    }

    private org.json.JSONArray readPersonalTimeline() {
        try {
            return new org.json.JSONArray(preferences.getString("memory_timeline_v1", "[]"));
        } catch (org.json.JSONException e) {
            return null;
        }
    }

    private int getPersonalTimelineCount() {
        org.json.JSONArray entries = readPersonalTimeline();
        return entries == null ? 0 : entries.length();
    }

    private void savePersonalTimeline(org.json.JSONArray entries) {
        if (entries != null) {
            preferences.edit().putString("memory_timeline_v1", entries.toString()).apply();
        }
    }

    // Timeline entries are added only when the owner explicitly creates them; no background capture.
    private void showPersonalTimeline() {
        org.json.JSONArray entries = readPersonalTimeline();
        if (entries == null) {
            new AlertDialog.Builder(this).setTitle("JARVIS • Personal Timeline")
                    .setMessage("Timeline data could not be read. Existing information has not been changed.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < entries.length(); i++) {
            org.json.JSONObject item = entries.optJSONObject(i);
            if (item == null) {
                labels.add("Unavailable entry");
                continue;
            }
            long createdAt = item.optLong("createdAt", 0L);
            String date = createdAt > 0
                    ? new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(new java.util.Date(createdAt))
                    : "Date unavailable";
            labels.add(item.optString("title", "Untitled entry") + " • " + date);
        }
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Personal Timeline")
                .setItems(labels.toArray(new String[0]), (d, which) -> {
                    org.json.JSONObject item = entries.optJSONObject(which);
                    if (item == null) return;
                    long createdAt = item.optLong("createdAt", 0L);
                    String date = createdAt > 0
                            ? new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    .format(new java.util.Date(createdAt))
                            : "Date unavailable";
                    new AlertDialog.Builder(this)
                            .setTitle(item.optString("title", "Timeline entry"))
                            .setMessage(date + "\n\n" + item.optString("detail", ""))
                            .setPositiveButton("Close", null).show();
                })
                .setPositiveButton("Add entry", (d, w) -> showAddPersonalTimelineEntry())
                .setNeutralButton("Delete entry", (d, w) -> showDeletePersonalTimelineEntry())
                .setNegativeButton("Back", (d, w) -> showPersonalMemoryVault())
                .show();
    }

    private void showAddPersonalTimelineEntry() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 8);
        EditText titleInput = new EditText(this);
        titleInput.setSingleLine(true);
        titleInput.setHint("Milestone or event title");
        form.addView(titleInput, new LinearLayout.LayoutParams(-1, -2));
        EditText detailInput = new EditText(this);
        detailInput.setHint("Details (optional)");
        detailInput.setMinLines(3);
        detailInput.setGravity(Gravity.TOP | Gravity.START);
        detailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        form.addView(detailInput, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Add timeline entry")
                .setMessage("Only information you enter here is saved. JARVIS does not collect events in the background.")
                .setView(form)
                .setPositiveButton("Save entry", (d, w) -> {
                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        status.setText("TIMELINE ENTRY NEEDS A TITLE");
                        return;
                    }
                    org.json.JSONArray entries = readPersonalTimeline();
                    if (entries == null) {
                        status.setText("TIMELINE DATA UNAVAILABLE • NOTHING CHANGED");
                        return;
                    }
                    org.json.JSONObject item = new org.json.JSONObject();
                    try {
                        item.put("title", title);
                        item.put("detail", detailInput.getText().toString().trim());
                        item.put("createdAt", System.currentTimeMillis());
                        entries.put(item);
                        savePersonalTimeline(entries);
                        status.setText("TIMELINE ENTRY SAVED ON DEVICE");
                        showPersonalTimeline();
                    } catch (org.json.JSONException e) {
                        status.setText("TIMELINE ENTRY COULD NOT BE SAVED");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeletePersonalTimelineEntry() {
        org.json.JSONArray entries = readPersonalTimeline();
        if (entries == null || entries.length() == 0) {
            new AlertDialog.Builder(this).setTitle("JARVIS • Delete timeline entry")
                    .setMessage("There are no timeline entries to delete.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < entries.length(); i++) {
            org.json.JSONObject item = entries.optJSONObject(i);
            labels.add(item == null ? "Unavailable entry" : item.optString("title", "Untitled entry"));
        }
        new AlertDialog.Builder(this).setTitle("Choose an entry to delete")
                .setItems(labels.toArray(new String[0]), (d, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete this timeline entry?")
                            .setMessage("Only the selected owner-created entry will be deleted.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (confirm, selected) -> {
                                org.json.JSONArray latest = readPersonalTimeline();
                                if (latest != null && which < latest.length()) {
                                    latest.remove(which);
                                    savePersonalTimeline(latest);
                                    status.setText("TIMELINE ENTRY DELETED");
                                    showPersonalTimeline();
                                }
                            }).show();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showEditVaultNotes() {
        EditText editor = new EditText(this);
        editor.setHint("Your saved notes and owner-approved knowledge");
        editor.setMinLines(5);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editor.setText(getSavedNotes());
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Review / correct knowledge")
                .setMessage("Edit the existing notes store. Saving replaces only the notes text; recent AI conversation is separate.")
                .setView(editor)
                .setPositiveButton("Save changes", (d, w) -> {
                    String updated = editor.getText().toString().trim();
                    preferences.edit().putString("notes", updated).putString("jarvis_notes", updated).apply();
                    status.setText("PERSONAL KNOWLEDGE UPDATED ON DEVICE");
                    showPersonalMemoryVault();
                })
                .setNeutralButton("Delete notes", (d, w) -> confirmClearVaultNotes())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmClearVaultNotes() {
        new AlertDialog.Builder(this).setTitle("Delete saved notes?")
                .setMessage("This deletes the existing notes only. Recent AI conversation and tasks are kept.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete notes", (d, w) -> {
                    preferences.edit().remove("notes").remove("jarvis_notes").apply();
                    status.setText("SAVED NOTES DELETED");
                    showPersonalMemoryVault();
                }).show();
    }

    private void confirmClearRecentContext() {
        new AlertDialog.Builder(this).setTitle("Clear recent conversation context?")
                .setMessage("This clears only the recent AI conversation context. Saved notes and tasks are kept.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear context", (d, w) -> {
                    getSharedPreferences("jarvis_ai_memory", MODE_PRIVATE).edit().remove("conversation").apply();
                    status.setText("RECENT CONVERSATION CONTEXT CLEARED");
                    showPersonalMemoryVault();
                }).show();
    }

    private void exportPersonalMemory() {
        String notes = getSavedNotes();
        String conversation = getSharedPreferences("jarvis_ai_memory", MODE_PRIVATE)
                .getString("conversation", "");
        org.json.JSONArray timeline = readPersonalTimeline();
        StringBuilder timelineText = new StringBuilder();
        if (timeline != null) {
            for (int i = 0; i < timeline.length(); i++) {
                org.json.JSONObject item = timeline.optJSONObject(i);
                if (item == null) continue;
                timelineText.append(item.optString("title", "Untitled entry"))
                        .append(" • ")
                        .append(item.optLong("createdAt", 0L))
                        .append("\n")
                        .append(item.optString("detail", ""))
                        .append("\n\n");
            }
        }
        String export = "JARVIS PERSONAL MEMORY EXPORT\n\nSAVED NOTES\n"
                + (notes.isEmpty() ? "(none)" : notes)
                + "\n\nRECENT CONVERSATION CONTEXT\n"
                + (conversation.isEmpty() ? "(none)" : conversation)
                + "\n\nOWNER-CREATED TIMELINE\n"
                + (timelineText.length() == 0 ? "(none)" : timelineText.toString())
                + "\nExported by owner from this device.";
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, "JARVIS Personal Memory Export");
            share.putExtra(Intent.EXTRA_TEXT, export);
            startActivity(Intent.createChooser(share, "Review and choose where to export"));
            status.setText("EXPORT SHARE SHEET OPENED");
        } catch (Exception e) {
            status.setText("EXPORT NOT AVAILABLE");
        }
    }

    private void showWorkflowStudio() {
        org.json.JSONArray workflows = readManualWorkflows();
        if (workflows == null) {
            new AlertDialog.Builder(this).setTitle("JARVIS • Workflow Studio")
                    .setMessage("Workflow data could not be read. Existing data has not been changed.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < workflows.length(); i++) {
            org.json.JSONObject item = workflows.optJSONObject(i);
            if (item == null) {
                labels.add("Unavailable workflow");
                continue;
            }
            org.json.JSONArray steps = item.optJSONArray("steps");
            org.json.JSONArray done = item.optJSONArray("done");
            int total = steps == null ? 0 : steps.length();
            int completed = 0;
            if (done != null) {
                for (int j = 0; j < done.length(); j++) if (done.optBoolean(j, false)) completed++;
            }
            labels.add(item.optString("title", "Untitled workflow") + "  •  " + completed + "/" + total);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("JARVIS • Workflow Studio")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> showManualWorkflow(which))
                .setPositiveButton("Create workflow", (dialog, which) -> showCreateManualWorkflow())
                .setNeutralButton("Delete", (dialog, which) -> showDeleteManualWorkflow())
                .setNegativeButton("Close", null);
        if (labels.isEmpty()) builder.setMessage("Create a personal checklist, then run it manually. Workflows never run in the background.");
        else builder.setMessage("Choose a checklist to review or continue. All steps are manual and owner-controlled.");
        builder.show();
    }

    private void showCreateManualWorkflow() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(32, 12, 32, 4);
        EditText titleInput = new EditText(this);
        titleInput.setSingleLine(true);
        titleInput.setHint("Workflow name");
        EditText stepsInput = new EditText(this);
        stepsInput.setHint("One checklist step per line (max 12)");
        stepsInput.setMinLines(4);
        stepsInput.setGravity(Gravity.TOP);
        stepsInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        panel.addView(titleInput);
        panel.addView(stepsInput);
        new AlertDialog.Builder(this).setTitle("JARVIS • Create checklist")
                .setMessage("Manual checklist only. No automatic app/device actions or background triggers.")
                .setView(panel)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String rawSteps = stepsInput.getText().toString();
                    String[] split = rawSteps.split("\\r?\\n");
                    org.json.JSONArray steps = new org.json.JSONArray();
                    for (String line : split) {
                        String step = line.trim();
                        if (!step.isEmpty() && steps.length() < 12 && step.length() <= 120) steps.put(step);
                    }
                    if (title.isEmpty() || title.length() > 60 || steps.length() == 0) {
                        status.setText("ADD A NAME AND AT LEAST ONE VALID STEP");
                        return;
                    }
                    org.json.JSONArray workflows = readManualWorkflows();
                    if (workflows == null) {
                        status.setText("WORKFLOW DATA COULD NOT BE READ");
                        return;
                    }
                    org.json.JSONObject item = new org.json.JSONObject();
                    try {
                        item.put("title", title);
                        item.put("steps", steps);
                        item.put("done", new org.json.JSONArray());
                        item.put("created_at", System.currentTimeMillis());
                        workflows.put(item);
                        saveManualWorkflows(workflows);
                        status.setText("MANUAL WORKFLOW SAVED");
                        showWorkflowStudio();
                    } catch (org.json.JSONException e) {
                        status.setText("WORKFLOW COULD NOT BE SAVED");
                    }
                }).show();
    }

    private void showManualWorkflow(int index) {
        org.json.JSONArray workflows = readManualWorkflows();
        if (workflows == null) return;
        org.json.JSONObject item = workflows.optJSONObject(index);
        if (item == null) return;
        org.json.JSONArray steps = item.optJSONArray("steps");
        if (steps == null || steps.length() == 0) {
            status.setText("WORKFLOW HAS NO STEPS");
            return;
        }
        org.json.JSONArray done = item.optJSONArray("done");
        boolean[] checked = new boolean[steps.length()];
        String[] labels = new String[steps.length()];
        for (int i = 0; i < steps.length(); i++) {
            checked[i] = done != null && done.optBoolean(i, false);
            labels[i] = steps.optString(i, "Unavailable step");
        }
        new AlertDialog.Builder(this).setTitle(item.optString("title", "Workflow"))
                .setMessage("Tick steps as you complete them. JARVIS does not execute the steps for you.")
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Save progress", (dialog, which) -> {
                    org.json.JSONArray updated = new org.json.JSONArray();
                    for (boolean value : checked) updated.put(value);
                    try {
                        item.put("done", updated);
                        saveManualWorkflows(workflows);
                        status.setText("WORKFLOW CHECKLIST PROGRESS SAVED");
                    } catch (org.json.JSONException e) {
                        status.setText("WORKFLOW PROGRESS COULD NOT BE SAVED");
                    }
                    showWorkflowStudio();
                })
                .setNeutralButton("Reset checklist", (dialog, which) -> {
                    try {
                        item.put("done", new org.json.JSONArray());
                        saveManualWorkflows(workflows);
                        status.setText("WORKFLOW CHECKLIST RESET");
                    } catch (org.json.JSONException e) {
                        status.setText("WORKFLOW COULD NOT BE RESET");
                    }
                    showWorkflowStudio();
                })
                .setNegativeButton("Close", null).show();
    }

    private void showDeleteManualWorkflow() {
        org.json.JSONArray workflows = readManualWorkflows();
        if (workflows == null || workflows.length() == 0) {
            status.setText(workflows == null ? "WORKFLOW DATA COULD NOT BE READ" : "NO WORKFLOWS TO DELETE");
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < workflows.length(); i++) {
            org.json.JSONObject item = workflows.optJSONObject(i);
            labels.add(item == null ? "Unavailable workflow" : item.optString("title", "Untitled workflow"));
        }
        new AlertDialog.Builder(this).setTitle("JARVIS • Delete workflow")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                    new AlertDialog.Builder(this).setTitle("Delete this checklist?")
                            .setMessage("This removes only the selected workflow from this phone.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                workflows.remove(which);
                                saveManualWorkflows(workflows);
                                status.setText("WORKFLOW DELETED");
                                showWorkflowStudio();
                            }).show();
                }).setNegativeButton("Cancel", null).show();
    }

    private void showConnectedSystems() {
        String[] items = {"Wi-Fi settings", "Bluetooth settings", "Location settings", "Open Maps", "Open Calendar", "Open Phone dialer"};
        new AlertDialog.Builder(this).setTitle("JARVIS • Connected systems")
                .setItems(items, (d, which) -> {
                    Intent intent;
                    if (which == 0) intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                    else if (which == 1) intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    else if (which == 2) intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    else if (which == 3) intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q="));
                    else if (which == 4) intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR);
                    else intent = new Intent(Intent.ACTION_DIAL);
                    try { startActivity(intent); }
                    catch (Exception e) { status.setText("THIS CONTROL IS NOT AVAILABLE ON THIS PHONE"); }
                }).setNegativeButton("Close", null).show();
    }

    private void animateOrb(float scale, long duration) {
        if (avatar == null) return;
        avatar.animate().scaleX(scale).scaleY(scale).setDuration(duration).start();
    }


    /**
     * Owner-started focus timer. It does not block apps, notifications or calls.
     * The countdown remains active only while the JARVIS process is alive.
     */
    private void showFocusSession() {
        EditText taskInput = new EditText(this);
        taskInput.setHint("What are you focusing on?");
        taskInput.setSingleLine(true);
        taskInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(28, 8, 28, 4);
        TextView help = new TextView(this);
        help.setText("Choose a short focus block. JARVIS shows a countdown but does not restrict apps, calls or notifications. Keep JARVIS open while the session runs.");
        help.setTextColor(Color.rgb(190, 215, 230));
        panel.addView(help);
        panel.addView(taskInput);
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Focus session")
                .setView(panel)
                .setPositiveButton("Choose duration", (dialog, which) -> {
                    String task = taskInput.getText().toString().trim();
                    if (task.isEmpty()) task = "Focus session";
                    showFocusDurationDialog(task);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFocusDurationDialog(String task) {
        new AlertDialog.Builder(this)
                .setTitle("Choose focus duration")
                .setItems(new String[]{"15 minutes", "25 minutes", "45 minutes"}, (dialog, which) -> {
                    int minutes = which == 0 ? 15 : (which == 1 ? 25 : 45);
                    startFocusSession(task, minutes);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startFocusSession(String task, int minutes) {
        if (activeFocusTimer != null) {
            activeFocusTimer.cancel();
            activeFocusTimer = null;
        }
        final String focusTask = task;
        final TextView taskLabel = new TextView(this);
        taskLabel.setText(focusTask);
        taskLabel.setTextColor(Color.rgb(190, 215, 230));
        taskLabel.setTextSize(16);
        taskLabel.setGravity(Gravity.CENTER);
        taskLabel.setPadding(16, 20, 16, 8);
        final TextView timerText = new TextView(this);
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(30);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(20, 24, 20, 28);
        LinearLayout timerPanel = new LinearLayout(this);
        timerPanel.setOrientation(LinearLayout.VERTICAL);
        timerPanel.addView(taskLabel);
        timerPanel.addView(timerText);
        final AlertDialog sessionDialog = new AlertDialog.Builder(this)
                .setTitle("FOCUS IN PROGRESS")
                .setView(timerPanel)
                .setPositiveButton("Stop session", null)
                .create();
        sessionDialog.setCancelable(false);
        sessionDialog.show();
        sessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (activeFocusTimer != null) {
                activeFocusTimer.cancel();
                activeFocusTimer = null;
            }
            status.setText("FOCUS SESSION STOPPED");
            transcript.setText("JARVIS • Focus session stopped\\n\\n" + focusTask);
            sessionDialog.dismiss();
        });
        activeFocusTimer = new android.os.CountDownTimer(minutes * 60L * 1000L, 1000L) {
            @Override public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000L;
                long remainingMinutes = totalSeconds / 60L;
                long remainingSeconds = totalSeconds % 60L;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d",
                        remainingMinutes, remainingSeconds));
                status.setText("FOCUS SESSION • " + remainingMinutes + " MIN LEFT");
            }

            @Override public void onFinish() {
                activeFocusTimer = null;
                timerText.setText("SESSION COMPLETE");
                status.setText("FOCUS SESSION COMPLETE");
                transcript.setText("JARVIS • Focus session complete\\n\\n" + focusTask);
                sessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Done");
                sessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> sessionDialog.dismiss());
            }
        }.start();
    }


    /**
     * Read-only device resource overview. Uses Android public APIs and adds no permissions.
     */
    private void showDeviceResourceDashboard() {
        StringBuilder report = new StringBuilder("JARVIS • DEVICE RESOURCE INTELLIGENCE\\n\\n");
        report.append("DEVICE\\n");
        report.append(android.os.Build.MANUFACTURER).append(" ")
                .append(android.os.Build.MODEL).append("\\n");
        report.append("Android ").append(android.os.Build.VERSION.RELEASE)
                .append(" (API ").append(android.os.Build.VERSION.SDK_INT).append(")\\n\\n");

        report.append("BATTERY\\n");
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            if (level >= 0 && scale > 0) {
                report.append("Charge: ").append(level * 100 / scale).append("%\\n");
            } else {
                report.append("Charge: unavailable\\n");
            }
            report.append("Power: ").append(plugged == 0 ? "Not plugged in" : "Charging / connected to power").append("\\n");
        } else {
            report.append("Battery details unavailable\\n");
        }

        report.append("\\nSTORAGE (APP DATA VOLUME)\\n");
        try {
            android.os.StatFs storage = new android.os.StatFs(android.os.Environment.getDataDirectory().getPath());
            long totalBytes = storage.getTotalBytes();
            long freeBytes = storage.getAvailableBytes();
            report.append("Total: ").append(formatResourceBytes(totalBytes)).append("\\n");
            report.append("Available: ").append(formatResourceBytes(freeBytes)).append("\\n");
            if (totalBytes > 0L) {
                report.append("Used: ").append((totalBytes - freeBytes) * 100L / totalBytes).append("%\\n");
            }
        } catch (Exception e) {
            report.append("Storage details unavailable\\n");
        }

        report.append("\\nMEMORY (RAM)\\n");
        try {
            android.app.ActivityManager manager =
                    (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                android.app.ActivityManager.MemoryInfo memory = new android.app.ActivityManager.MemoryInfo();
                manager.getMemoryInfo(memory);
                report.append("Total: ").append(formatResourceBytes(memory.totalMem)).append("\\n");
                report.append("Available: ").append(formatResourceBytes(memory.availMem)).append("\\n");
                report.append("Low-memory condition: ").append(memory.lowMemory ? "Yes" : "No").append("\\n");
            } else {
                report.append("RAM details unavailable\\n");
            }
        } catch (Exception e) {
            report.append("RAM details unavailable\\n");
        }

        report.append("\\nREAD-ONLY OVERVIEW\\n");
        report.append("JARVIS only reads the system summaries shown here. It does not clear memory, close apps, change settings, or upload this report.");
        transcript.setText(report.toString());
        status.setText("DEVICE RESOURCE OVERVIEW READY");
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Device resources")
                .setMessage(report.toString())
                .setPositiveButton("Refresh", (d, w) -> showDeviceResourceDashboard())
                .setNegativeButton("Close", null)
                .show();
    }

    private String formatResourceBytes(long bytes) {
        if (bytes < 0L) return "Unavailable";
        final double kib = 1024.0;
        final double mib = kib * 1024.0;
        final double gib = mib * 1024.0;
        if (bytes >= gib) return String.format(Locale.getDefault(), "%.2f GB", bytes / gib);
        if (bytes >= mib) return String.format(Locale.getDefault(), "%.1f MB", bytes / mib);
        if (bytes >= kib) return String.format(Locale.getDefault(), "%.1f KB", bytes / kib);
        return bytes + " B";
    }

    private void showCommandCenter() {
        String[] items = {
                "Hey JARVIS • Background listening",
                "Stop background listening",
                "Call & SMS announcements",
                "Background app opening",
                "Voice call controls",
                "System safety check",
                "Set emergency contact",
                "SOS • Emergency options",
                "Clear AI memory",
                "Assistant modes",
                "Smart notes",
                "Daily briefing",
                "Live web • Weather",
                "Vision tools",
                "Tasks • Reminders • Alarms",
                "Connected systems",
                "Privacy & permissions",
                "Screen-time overview",
                "Cybersecurity Command Center",
                "Digital Threat Inspector • Link / Code",
                "Background listening voice: Silent / On",
                "Voice Studio • Voice / Speed / Pitch",
                "Focus & Productivity Session",
                "Device Resource Intelligence",
                "Workflow Studio • Manual checklists",
                "Personal Memory & Knowledge Vault",
                "Language & Learning Studio • Translate / Text / Vocabulary",
                "Universal App Intelligence • Installed Apps / Details / Permissions",
                "Public Knowledge Studio • Countries / India PIN / Dictionary",
                "Live Information Studio • Weather / Currency / Holidays",
                "AI Learning Academy • Coding / Science / Technology Roadmaps",
                "Software Development Studio • Architecture / Debug / Code Review",
                "Data Intelligence Center • CSV Profile / Quality / Insights",
                "Personal Productivity Studio • Projects / Priority / Weekly Review",
                "Creative Media Studio • Video Script / Storyboard / Presentation"
        };
        new AlertDialog.Builder(this).setTitle("J.A.R.V.I.S.  •  COMMAND CENTER")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) startWakeWordListening();
                    else if (which == 1) {
                        stopService(new Intent(MainActivity.this, WakeWordService.class));
                        status.setText("BACKGROUND LISTENING STOPPED");
                    } else if (which == 2) enablePhoneEventPermissions();
                    else if (which == 3) enableBackgroundAppOpening();
                    else if (which == 4) {
                        try { startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
                        catch (Exception e) { status.setText("OPEN SETTINGS > ACCESSIBILITY"); }
                    } else if (which == 5) runSafetyCheck();
                    else if (which == 6) showEmergencyContactDialog();
                    else if (which == 7) showSosOptions();
                    else if (which == 8) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Clear JARVIS AI memory?")
                                .setMessage("This clears recent conversation memory stored on this phone. Your notes are kept.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Clear", (d, w) -> {
                                    getSharedPreferences("jarvis_ai_memory", MODE_PRIVATE).edit().clear().apply();
                                    status.setText("AI MEMORY CLEARED");
                                    transcript.setText("JARVIS memory cleared.");
                                }).show();
                    } else if (which == 9) showModeSelector();
                    else if (which == 10) showNotesDialog();
                    else if (which == 11) showDailyBriefing();
                    else if (which == 12) showLiveInfoOptions();
                    else if (which == 13) openVisionTools();
                    else if (which == 14) showTaskAndReminderHub();
                    else if (which == 15) showConnectedSystems();
                    else if (which == 16) showPrivacyOptions();
                    else if (which == 17) showScreenTimeOverview();
                    else if (which == 18) showCybersecurityCommandCenter();
                    else if (which == 19) showDigitalThreatInspector();
                    else if (which == 20) showBackgroundVoiceModeOptions();
                    else if (which == 21) showVoiceSettings();
                    else if (which == 22) showFocusSession();
                    else if (which == 23) showDeviceResourceDashboard();
                    else if (which == 24) showWorkflowStudio();
                    else if (which == 25) showPersonalMemoryVault();
                    else if (which == 26) LanguageStudio.show(MainActivity.this, status, transcript);
                    else if (which == 27) AppIntelligenceStudio.show(MainActivity.this);
                    else if (which == 28) PublicKnowledgeStudio.show(MainActivity.this);
                    else if (which == 29) LiveInfoStudio.show(MainActivity.this);
                    else if (which == 30) LearningAcademyStudio.show(MainActivity.this, status, transcript);
                    else if (which == 31) SoftwareDevelopmentStudio.show(MainActivity.this, status, transcript);
                    else if (which == 32) DataIntelligenceStudio.show(MainActivity.this, status, transcript);
                    else if (which == 33) ProductivityStudio.show(MainActivity.this);
                    else if (which == 34) CreativeMediaStudio.show(MainActivity.this, status, transcript);
                }).setNegativeButton("Close", null).show();
    }

    private void showBackgroundVoiceModeOptions() {
        boolean silent = preferences.getBoolean("silent_background_ack", true);
        String[] options = {
                "Silent — no repeated wake acknowledgement",
                "Voice acknowledgement on"
        };
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Background voice")
                .setSingleChoiceItems(options, silent ? 0 : 1, (dialog, which) -> {
                    boolean silentMode = which == 0;
                    preferences.edit().putBoolean("silent_background_ack", silentMode).apply();
                    status.setText(silentMode
                            ? "BACKGROUND LISTENING • SILENT"
                            : "BACKGROUND VOICE ACKNOWLEDGEMENT ON");
                    dialog.dismiss();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showCybersecurityCommandCenter() {
        boolean micGranted = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean contactsGranted = checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean smsGranted = checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean callGranted = checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        boolean overlayAllowed = android.os.Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
        boolean debugBuild = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        String accessibility = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean accessibilityEnabled = accessibility != null && accessibility.contains(getPackageName());

        StringBuilder report = new StringBuilder("JARVIS • CYBERSECURITY COMMAND CENTER\n\n");
        report.append("LOCAL APP SECURITY REVIEW (READ-ONLY)\n");
        report.append(micGranted ? "• Microphone: permission granted — review whether you need it enabled.\n"
                : "• Microphone: permission not granted.\n");
        report.append(contactsGranted ? "• Contacts: permission granted.\n" : "• Contacts: permission not granted.\n");
        report.append(smsGranted ? "• SMS: permission granted. Keep this enabled only if you use call/SMS announcements.\n"
                : "• SMS: permission not granted.\n");
        report.append(callGranted ? "• Direct calling: permission granted.\n" : "• Direct calling: permission not granted.\n");
        report.append(overlayAllowed ? "• Display-over-other-apps: allowed. Disable it in Android settings if not needed.\n"
                : "• Display-over-other-apps: not allowed.\n");
        report.append(accessibilityEnabled ? "• JARVIS accessibility service: enabled. Keep enabled only while using its call-control feature.\n"
                : "• JARVIS accessibility service: not enabled.\n");
        report.append(debugBuild ? "• Build type: debuggable — use a release-signed build for normal use.\n"
                : "• Build type: not marked debuggable.\n");
        report.append("\nIMPORTANT\nThis is a basic local permission review, not a malware scan, penetration test, network scan or certification that the phone is secure. No files, accounts or networks were scanned.\n");
        report.append("\nRECOMMENDED\n1. Install Android and app updates from trusted sources.\n");
        report.append("2. Review app permissions and remove access you no longer need.\n");
        report.append("3. Keep screen lock and Google Play Protect enabled.\n");
        report.append("4. Never share OTPs, passwords or API keys.\n");
        report.append("5. Only test systems and networks you own or are explicitly authorized to assess.");

        status.setText("CYBERSECURITY REVIEW READY");
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Cybersecurity")
                .setMessage(report.toString())
                .setPositiveButton("Android security settings", (d, w) -> {
                    try { startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)); }
                    catch (Exception e) { status.setText("OPEN ANDROID SETTINGS > SECURITY"); }
                })
                .setNeutralButton("App permissions", (d, w) -> {
                    try {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.parse("package:" + getPackageName())));
                    } catch (Exception e) { status.setText("OPEN JARVIS APP INFO IN SETTINGS"); }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /** Opens a user-invoked, static-only inspector. It never visits links or executes supplied code. */
    private void showDigitalThreatInspector() {
        EditText input = new EditText(this);
        input.setHint("Paste a link or code snippet here");
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(5);
        input.setMaxLines(10);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Digital Threat Inspector")
                .setMessage("Paste only the link or code you want checked. JARVIS analyses the text locally; it will not open links, download files, install APKs or run code.")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Inspect", (dialog, which) -> {
                    String sample = input.getText().toString().trim();
                    if (sample.isEmpty()) {
                        status.setText("NOTHING TO INSPECT");
                        return;
                    }
                    String report = inspectUntrustedText(sample);
                    status.setText("STATIC INSPECTION COMPLETE");
                    new AlertDialog.Builder(this)
                            .setTitle("JARVIS • Inspection report")
                            .setMessage(report)
                            .setPositiveButton("Close", null)
                            .show();
                }).show();
    }

    private String inspectUntrustedText(String sample) {
        String lower = sample.toLowerCase(Locale.ROOT);
        StringBuilder report = new StringBuilder("LOCAL STATIC TEXT REVIEW\n\n");
        boolean looksLikeUrl = lower.matches("(?s).*(https?://|www\\.|[a-z0-9-]+\\.[a-z]{2,}).*")
                && !sample.contains("\n");
        if (looksLikeUrl) {
            String candidate = sample.startsWith("http://") || sample.startsWith("https://")
                    ? sample : "https://" + sample.replaceFirst("^www\\.", "");
            android.net.Uri uri = android.net.Uri.parse(candidate);
            String host = uri.getHost();
            report.append("TYPE: Link / URL\n");
            if (host == null || host.trim().isEmpty()) {
                report.append("• URL format could not be reliably parsed.\n");
            } else {
                report.append("• Host: ").append(host).append("\n");
                if (!"https".equalsIgnoreCase(uri.getScheme())) report.append("• Warning: link does not use HTTPS.\n");
                if (host.matches("[0-9.]+") || host.contains(":")) report.append("• Warning: host looks like a raw IP address.\n");
                if (host.toLowerCase(Locale.ROOT).contains("xn--")) report.append("• Warning: internationalized/punycode hostname; check spelling carefully.\n");
                if (host.split("\\.").length >= 4) report.append("• Caution: unusually deep subdomain structure.\n");
                if (lower.contains("@")) report.append("• Warning: URL contains an @ symbol; inspect the real host carefully.\n");
                if (lower.matches(".*(bit\\.ly|tinyurl\\.com|t\\.co|is\\.gd|cutt\\.ly).*")) report.append("• Caution: URL may use a link-shortening service; destination is not checked.\n");
                if (lower.matches(".*(login|verify|wallet|bank|otp|password|urgent|free-prize|claim).*")) report.append("• Caution: wording commonly used in deceptive messages.\n");
                if ("https".equalsIgnoreCase(uri.getScheme()) && !lower.contains("@")
                        && !host.matches("[0-9.]+") && !host.toLowerCase(Locale.ROOT).contains("xn--")) {
                    report.append("• No obvious URL red flags were detected by these basic checks.\n");
                }
            }
            report.append("\n");
        } else {
            report.append("TYPE: Code / text snippet\n");
            String[] indicators = {
                "runtime.getruntime", "processbuilder", "exec(", "eval(", "powershell",
                "cmd.exe", "chmod ", "curl ", "wget ", "base64", "su -c",
                "accessibilityservice", "requestinstallpackages"
            };
            boolean found = false;
            for (String indicator : indicators) {
                if (lower.contains(indicator)) {
                    report.append("• Review manually: contains '").append(indicator).append("'.\n");
                    found = true;
                }
            }
            if (!found) report.append("• No configured high-risk text patterns were found.\n");
            report.append("• This is pattern matching only; context matters and benign code can contain these terms.\n\n");
        }
        report.append("LIMITATIONS\n");
        report.append("This is a basic offline text check, not a malware scan or proof that content is safe. Links are not opened and redirects/reputation are not checked. Files and APKs are not analysed in this first version. Never run unknown code or install an unexpected APK.");
        return report.toString();
    }

    private void showCalendarEventDraftDialog() {
        EditText titleInput = new EditText(this);
        titleInput.setHint("Event title");
        titleInput.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("JARVIS • Calendar event")
                .setMessage("JARVIS will prepare an event draft and open your calendar app. Review it there and save it yourself.")
                .setView(titleInput)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Choose date & time", (dialog, which) -> {
                    String eventTitle = titleInput.getText().toString().trim();
                    if (eventTitle.isEmpty()) {
                        status.setText("EVENT TITLE REQUIRED");
                        return;
                    }
                    java.util.Calendar chosen = java.util.Calendar.getInstance();
                    chosen.add(java.util.Calendar.HOUR_OF_DAY, 1);
                    new android.app.DatePickerDialog(this, (dateView, year, month, day) -> {
                        chosen.set(java.util.Calendar.YEAR, year);
                        chosen.set(java.util.Calendar.MONTH, month);
                        chosen.set(java.util.Calendar.DAY_OF_MONTH, day);
                        new android.app.TimePickerDialog(this, (timeView, hour, minute) -> {
                            chosen.set(java.util.Calendar.HOUR_OF_DAY, hour);
                            chosen.set(java.util.Calendar.MINUTE, minute);
                            chosen.set(java.util.Calendar.SECOND, 0);
                            chosen.set(java.util.Calendar.MILLISECOND, 0);
                            long begin = chosen.getTimeInMillis();
                            long finish = begin + 60L * 60L * 1000L;
                            Intent insert = new Intent(Intent.ACTION_INSERT,
                                    android.provider.CalendarContract.Events.CONTENT_URI);
                            insert.putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin);
                            insert.putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, finish);
                            insert.putExtra(android.provider.CalendarContract.Events.TITLE, eventTitle);
                            insert.putExtra(android.provider.CalendarContract.Events.DESCRIPTION,
                                    "Prepared by JARVIS. Please review before saving.");
                            try {
                                startActivity(insert);
                                status.setText("CALENDAR DRAFT OPENED • REVIEW BEFORE SAVE");
                            } catch (Exception e) {
                                status.setText("NO COMPATIBLE CALENDAR APP FOUND");
                            }
                        }, chosen.get(java.util.Calendar.HOUR_OF_DAY),
                                chosen.get(java.util.Calendar.MINUTE), false).show();
                    }, chosen.get(java.util.Calendar.YEAR),
                            chosen.get(java.util.Calendar.MONTH),
                            chosen.get(java.util.Calendar.DAY_OF_MONTH)).show();
                }).show();
    }

    private void showScreenTimeOverview() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            new AlertDialog.Builder(this)
                    .setTitle("JARVIS • Screen-time access")
                    .setMessage("To show an estimated screen-time overview, Android requires you to grant Usage Access manually. JARVIS only reads usage summaries on this device. You can revoke access any time in Settings.")
                    .setNegativeButton("Not now", null)
                    .setPositiveButton("Open Usage Access", (d, w) -> {
                        try { startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)); }
                        catch (Exception e) { status.setText("OPEN SETTINGS > SPECIAL APP ACCESS > USAGE ACCESS"); }
                    }).show();
            return;
        }

        UsageStatsManager manager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) {
            status.setText("SCREEN-TIME DATA UNAVAILABLE");
            return;
        }
        long end = System.currentTimeMillis();
        long start = end - 24L * 60L * 60L * 1000L;
        java.util.List<UsageStats> stats = manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, start, end);
        if (stats == null || stats.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("JARVIS • Screen-time")
                    .setMessage("Android has not provided usage data yet. Use the phone normally and check again later.")
                    .setPositiveButton("OK", null).show();
            return;
        }

        long total = 0L;
        java.util.Map<String, Long> byApp = new java.util.HashMap<>();
        for (UsageStats item : stats) {
            long duration = item.getTotalTimeInForeground();
            if (duration <= 0L) continue;
            total += duration;
            String packageName = item.getPackageName();
            Long previous = byApp.get(packageName);
            byApp.put(packageName, (previous == null ? 0L : previous) + duration);
        }
        java.util.List<java.util.Map.Entry<String, Long>> ranked =
                new java.util.ArrayList<>(byApp.entrySet());
        java.util.Collections.sort(ranked, (a, b) -> Long.compare(b.getValue(), a.getValue()));

        StringBuilder report = new StringBuilder("Estimated foreground app usage • last 24 hours\n");
        report.append("Total recorded app time: ").append(formatUsageDuration(total)).append("\n\n");
        int shown = 0;
        for (java.util.Map.Entry<String, Long> entry : ranked) {
            if (shown++ == 5) break;
            String appLabel = entry.getKey();
            try {
                appLabel = getPackageManager().getApplicationLabel(
                        getPackageManager().getApplicationInfo(entry.getKey(), 0)).toString();
            } catch (Exception ignored) { }
            report.append(appLabel).append(" — ")
                    .append(formatUsageDuration(entry.getValue())).append("\n");
        }
        report.append("\nThese are Android usage estimates, not an exact measure of active attention. JARVIS does not upload this report.");
        transcript.setText("JARVIS • SCREEN-TIME\n\n" + report);
        status.setText("SCREEN-TIME OVERVIEW READY");
        new AlertDialog.Builder(this).setTitle("JARVIS • Screen-time")
                .setMessage(report.toString())
                .setPositiveButton("Refresh", (d, w) -> showScreenTimeOverview())
                .setNegativeButton("Close", null).show();
    }

    private String formatUsageDuration(long millis) {
        long minutes = millis / 60000L;
        long hours = minutes / 60L;
        long remainder = minutes % 60L;
        return hours > 0 ? hours + " hr " + remainder + " min" : remainder + " min";
    }

    private String getSavedNotes() {
        String voiceNotes = preferences.getString("notes", "");
        String panelNotes = preferences.getString("jarvis_notes", "");
        if (voiceNotes.isEmpty()) return panelNotes;
        if (panelNotes.isEmpty() || panelNotes.equals(voiceNotes)) return voiceNotes;
        return voiceNotes + "\n" + panelNotes;
    }

    private void showNotesDialog() {
        EditText input = new EditText(this);
        input.setHint("Write a note or task");
        input.setMinLines(2);
        input.setGravity(Gravity.TOP | Gravity.START);
        String existing = getSavedNotes();
        TextView saved = new TextView(this);
        saved.setText(existing.isEmpty() ? "No saved notes yet." : "SAVED NOTES\n" + existing);
        saved.setTextColor(Color.rgb(190, 215, 230));
        saved.setPadding(8, 8, 8, 12);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(12, 4, 12, 0);
        panel.addView(saved);
        panel.addView(input);
        new AlertDialog.Builder(this).setTitle("JARVIS • Local notes")
                .setMessage("Notes stay in this app's private storage on this phone.")
                .setView(panel)
                .setNegativeButton("Close", null)
                .setNeutralButton("Clear notes", (d, w) -> {
                    preferences.edit().remove("jarvis_notes").remove("notes").apply();
                    status.setText("LOCAL NOTES CLEARED");
                })
                .setPositiveButton("Save note", (d, w) -> {
                    String note = input.getText().toString().trim();
                    if (note.isEmpty()) { status.setText("EMPTY NOTE NOT SAVED"); return; }
                    String old = getSavedNotes();
                    String combined = old.isEmpty() ? note : old + "\n• " + note;
                    preferences.edit().putString("jarvis_notes", combined).putString("notes", combined).apply();
                    transcript.setText("JARVIS • SAVED NOTES\n\n" + combined);
                    status.setText("NOTE SAVED ON DEVICE");
                }).show();
    }

    private void showDailyBriefing() {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEEE, d MMMM yyyy • h:mm a", Locale.getDefault());
        String date = format.format(new java.util.Date());
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery = registerReceiver(null, filter);
        String batteryText = "Battery: unavailable";
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) batteryText = "Battery: " + (level * 100 / scale) + "%";
        }
        String notes = preferences.getString("jarvis_notes", "");
        String report = "JARVIS • DAILY BRIEFING\n\n" + date + "\n" + batteryText
                + "\nAssistant mode: " + preferences.getString("assistant_mode", "Normal")
                + "\nSaved notes: " + (notes.isEmpty() ? "none" : notes.split("\\n").length)
                + "\n\nLive weather and current news require an internet search.";
        transcript.setText(report);
        status.setText("DAILY BRIEFING READY");
        new AlertDialog.Builder(this).setTitle("Daily briefing").setMessage(report)
                .setPositiveButton("Weather", (d, w) -> openWeatherSearch())
                .setNeutralButton("Notes", (d, w) -> showNotesDialog())
                .setNegativeButton("Close", null).show();
    }

    private void showLiveInfoOptions() {
        new AlertDialog.Builder(this).setTitle("JARVIS • Live information")
                .setItems(new String[]{"Search the web", "Weather search"}, (d, which) -> {
                    if (which == 0) promptWebSearch(); else openWeatherSearch();
                }).setNegativeButton("Cancel", null).show();
    }

    private void promptWebSearch() {
        EditText input = new EditText(this);
        input.setHint("What should I search?");
        new AlertDialog.Builder(this).setTitle("Live web search").setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Search", (d, w) -> {
                    String query = input.getText().toString().trim();
                    if (!query.isEmpty()) {
                        try { startActivity(new Intent(Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode(query)))); }
                        catch (Exception e) { status.setText("BROWSER NOT AVAILABLE"); }
                    }
                }).show();
    }

    private void openWeatherSearch() {
        try { startActivity(new Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode("weather today")))); }
        catch (Exception e) { status.setText("BROWSER NOT AVAILABLE"); }
    }

    private void showPrivacyOptions() {
        String mic = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ? "Granted" : "Not granted";
        String contacts = checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED ? "Granted" : "Not granted";
        String notes = preferences.getString("jarvis_notes", "");
        String report = "PRIVACY & PERMISSIONS\n\nMicrophone: " + mic
                + "\nContacts: " + contacts
                + "\nLocal notes: " + (notes.isEmpty() ? "empty" : "saved")
                + "\n\nBackground listening is controlled separately. Use STOP BACKGROUND LISTENING to stop it. Notes and recent AI conversation are stored locally; clearing AI memory does not clear notes.";
        new AlertDialog.Builder(this).setTitle("Privacy controls").setMessage(report)
                .setPositiveButton("App settings", (d, w) -> {
                    try { startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.parse("package:" + getPackageName()))); }
                    catch (Exception e) { status.setText("OPEN ANDROID APP SETTINGS"); }
                })
                .setNeutralButton("Clear notes", (d, w) -> {
                    preferences.edit().remove("jarvis_notes").apply();
                    status.setText("LOCAL NOTES CLEARED");
                }).setNegativeButton("Close", null).show();
    }

    private void showModeSelector() {
        final String[] modes = {"Normal", "Caring", "Funny", "Serious", "Study", "Gaming", "Travel", "Night"};
        String current = preferences.getString("assistant_mode", "Normal");
        int checked = 0;
        for (int i = 0; i < modes.length; i++) if (modes[i].equalsIgnoreCase(current)) checked = i;
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Assistant mode")
                .setSingleChoiceItems(modes, checked, (dialog, which) -> {
                    applyAssistantMode(modes[which]);
                    speak(modes[which] + " mode on.");
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .show();
    }

    private void applyAssistantMode(String mode) {
        preferences.edit().putString("assistant_mode", mode).apply();
        if (avatar != null) {
            long duration = "Gaming".equals(mode) ? 900L
                    : ("Night".equals(mode) ? 2800L : ("Study".equals(mode) ? 1700L : 2000L));
            avatar.clearAnimation();
            android.view.animation.ScaleAnimation pulse = new android.view.animation.ScaleAnimation(
                    0.985f, 1.015f, 0.985f, 1.015f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            pulse.setDuration(duration);
            pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
            pulse.setRepeatCount(android.view.animation.Animation.INFINITE);
            avatar.startAnimation(pulse);
        }
        status.setText("MODE ACTIVE • " + mode.toUpperCase(Locale.ROOT));
    }

    private void enableBackgroundAppOpening() {
        if (android.os.Build.VERSION.SDK_INT < 23) {
            status.setText("BACKGROUND APP OPENING READY");
            return;
        }
        if (android.provider.Settings.canDrawOverlays(this)) {
            status.setText("BACKGROUND APP OPENING ENABLED");
            speak("Background app opening permission is already enabled.");
            return;
        }
        try {
            Intent overlaySettings = new Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()));
            startActivity(overlaySettings);
            status.setText("ALLOW DISPLAY OVER OTHER APPS, THEN RETURN TO JARVIS");
        } catch (Exception e) {
            status.setText("OPEN SETTINGS AND ALLOW DISPLAY OVER OTHER APPS");
        }
    }

    // M14 upgrades the existing System safety check entry instead of adding a duplicate menu module.
    private void runSafetyCheck() {
        DiagnosticsStudio.show(this, preferences, tts != null);
    }

    private void enablePhoneEventPermissions() {
        java.util.ArrayList<String> needed = new java.util.ArrayList<>();
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.READ_PHONE_STATE);
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.READ_CONTACTS);
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.RECEIVE_SMS);
        if (!needed.isEmpty()) requestPermissions(needed.toArray(new String[0]), PHONE_EVENTS_PERMISSION_REQUEST);
        else {
            status.setText("CALL & SMS ANNOUNCEMENTS ENABLED");
            speak("Call aur SMS announcements enabled.");
        }
    }

    private void startWakeWordListening() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            startWakeAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST);
            status.setText("MICROPHONE PERMISSION NEEDED");
            return;
        }
        // Background listening needs microphone permission only. Contacts and calling permissions are requested only when those features are used.
        try {
            Intent service = new Intent(this, WakeWordService.class);
            if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
            status.setText("HEY JARVIS LISTENING • CHECK NOTIFICATION");
        } catch (Exception e) {
            status.setText("COULD NOT START BACKGROUND LISTENING");
        }
    }

    private void stopVoiceSession() {
        try {
            if (recognizer != null) recognizer.cancel();
        } catch (Exception ignored) { }
        try {
            if (tts != null) tts.stop();
        } catch (Exception ignored) { }
        setVoiceStatus("VOICE SESSION STOPPED • STANDBY");
        animateOrb(1.0f, 300L);
    }

    private void setVoiceStatus(String message) {
        if (status == null) return;
        runOnUiThread(() -> status.setText(message));
    }

    private void startListening() {
        aiOffline = false;
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
            } else status.setText("MICROPHONE PERMISSION DENIED");
        } else if (requestCode == CONTACT_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startWakeWordListening();
            } else {
                status.setText("CONTACTS DENIED • VOICE CALLS NEED CONTACT ACCESS");
            }
        } else if (requestCode == PHONE_EVENTS_PERMISSION_REQUEST) {
            boolean allGranted = true;
            for (int result : grantResults) if (result != PackageManager.PERMISSION_GRANTED) allGranted = false;
            status.setText(allGranted ? "CALL & SMS ANNOUNCEMENTS ENABLED" : "CALL/SMS PERMISSION DENIED • ANNOUNCEMENTS LIMITED");
            if (allGranted) speak("Call aur SMS announcements enabled.");
        } else if (requestCode == CALL_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String number = pendingDirectCallNumber;
                String name = pendingDirectCallName;
                pendingDirectCallNumber = null;
                pendingDirectCallName = null;
                if (number != null) placeDirectCall(number, name);
            } else {
                pendingDirectCallNumber = null;
                pendingDirectCallName = null;
                status.setText("PHONE PERMISSION DENIED • CALL NOT PLACED");
                speak("Phone permission nahi mili. Call nahi ki gayi.");
            }
        }
    }

    // AI-assisted transcription cleanup only: local command routing still decides what can execute.
    private void understandVoiceCommand(String rawTranscript) {
        final String raw = rawTranscript == null ? "" : rawTranscript.trim();
        if (raw.isEmpty()) return;
        status.setText("UNDERSTANDING YOUR VOICE…");
        transcript.setText("YOU: " + raw + "\n\nJARVIS: Understanding…");
        JarvisAiClient.understandCommand(this, raw, understood -> {
            status.setText("SYSTEM ONLINE • STANDBY");
            String safeText = understood == null || understood.trim().isEmpty()
                    ? raw : understood.trim();
            handleCommand(safeText);
        });
    }

    private void handleCommand(String command) {
        transcript.setText("YOU: " + command);
        String original = command == null ? "" : command.trim();
        String lower = original.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim();

        // Recognize common ways the user may pronounce or address JARVIS.
        String cleaned = lower.replaceFirst("^(hey |hi |hello |oye |oy |arey |are |ok |okay |please )*", "");
        cleaned = cleaned.replaceFirst("^(jarvis ji|jarvis|jarvesh|jarvish|j a r v i s)(\\s+)", "");
        String intent = cleaned.trim();
        if (intent.contains("digital threat inspector") || intent.contains("inspect link")
                || intent.contains("check this link") || intent.contains("check code")
                || intent.contains("scan link") || intent.contains("link checker")) {
            showDigitalThreatInspector();
            return;
        }
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
                String updated = old.isEmpty() ? note : old + "\n" + note;
                String[] notes = updated.split("\n");
                StringBuilder kept = new StringBuilder();
                int start = Math.max(0, notes.length - 10);
                for (int i = start; i < notes.length; i++) {
                    if (kept.length() > 0) kept.append("\n");
                    kept.append(notes[i]);
                }
                preferences.edit().putString("notes", kept.toString()).apply();
                replyAndShow(original, "Theek hai, maine ye baat is phone par local memory mein save kar li.");
            }
            return;
        }
        if (intent.contains("what do you remember") || intent.contains("meri memory") || intent.contains("kya yaad hai")) {
            String notes = preferences.getString("notes", "");
            replyAndShow(original, notes.isEmpty() ? "Abhi koi saved note nahi hai. Kaho: remember that, phir apni baat." : "Tumhari saved notes: " + notes.replace("\n", ". "));
            return;
        }
        if (intent.contains("forget my memory") || intent.contains("clear memory") || intent.contains("memory delete") || intent.contains("sab yaad bhool")) {
            new AlertDialog.Builder(this).setTitle("Clear JARVIS memory")
                    .setMessage("Saved notes on this phone will be deleted. Emergency contact and voice style settings will stay.")
                    .setNegativeButton("Cancel", (d, w) -> {})
                    .setPositiveButton("Clear notes", (d, w) -> {
                        preferences.edit().remove("notes").remove("jarvis_notes").apply();
                        replyAndShow(original, "Saved notes clear kar diye.");
                    }).show();
            return;
        }
        if (intent.contains("silent background listening") || intent.contains("background voice silent")) {
            preferences.edit().putBoolean("silent_background_ack", true).apply();
            replyAndShow(original, "Silent background listening on. Main wake phrase par baar-baar acknowledgement nahi bolunga.");
            return;
        }
        if (intent.contains("background voice on") || intent.contains("wake acknowledgement on")) {
            preferences.edit().putBoolean("silent_background_ack", false).apply();
            replyAndShow(original, "Background voice acknowledgement on.");
            return;
        }
        if (intent.contains("study mode") || intent.contains("padhai mode")) {
            applyAssistantMode("Study");
            replyAndShow(original, "Study mode on. Main focused, clear aur step-by-step help dunga.");
            return;
        }
        if (intent.contains("gaming mode") || intent.contains("game mode")) {
            applyAssistantMode("Gaming");
            replyAndShow(original, "Gaming mode on. Main short aur quick responses dunga.");
            return;
        }
        if (intent.contains("travel mode") || intent.contains("safar mode")) {
            applyAssistantMode("Travel");
            replyAndShow(original, "Travel mode on. Main directions aur travel planning mein help karunga; live details ke liye internet zaroori hai.");
            return;
        }
        if (intent.contains("night mode") || intent.contains("raat mode")) {
            applyAssistantMode("Night");
            replyAndShow(original, "Night mode on. Main shaant aur concise rahunga.");
            return;
        }
        if (intent.contains("caring mode")) {
            applyAssistantMode("Caring");
            replyAndShow(original, "Caring mode on. Main dhyan se aur supportively jawab dunga.");
            return;
        }
        if (intent.contains("funny mode")) {
            applyAssistantMode("Funny");
            replyAndShow(original, "Funny mode on. Mauka mila toh halka-phulka humour bhi hoga.");
            return;
        }
        if (intent.contains("serious mode")) {
            applyAssistantMode("Serious");
            replyAndShow(original, "Serious mode on. Main seedhe aur focused jawab dunga.");
            return;
        }
        if (intent.contains("normal assistant mode") || intent.contains("normal mode")) {
            applyAssistantMode("Normal");
            replyAndShow(original, "Normal mode on.");
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
        if (intent.contains("normal voice")) {
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
        if (intent.contains("cybersecurity") || intent.contains("cyber security")
                || intent.contains("security command center") || intent.contains("phone security check")) {
            showCybersecurityCommandCenter();
            return;
        }

        if (intent.contains("help commands") || intent.equals("what can you do") || intent.contains("commands list")) {
            replyAndShow(original, "I can open apps and settings, tell time and battery, set alarms, search the web, save local notes with remember that, read notes with what do you remember, change calm or energetic voice mode, and open SOS options.");
            return;
        }

        if (intent.equals("sos") || intent.contains("sos options") || intent.contains("emergency options")) {
            showSosOptions();
            return;
        }

        // Emergency phrases open a clear confirmation before the emergency dialer.
        if (intent.contains("accident") || intent.contains("emergency")
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
        } else if (intent.contains("open vision") || intent.contains("vision kholo") || intent.contains("google lens") || intent.contains("scan image")) {
            openVisionTools();
            return;
        } else if (intent.contains("show tasks") || intent.contains("task list") || intent.contains("reminders") || intent.contains("reminder kholo")) {
            showTaskAndReminderHub();
            return;
        } else if (intent.contains("connected systems") || intent.contains("quick controls") || intent.contains("system controls")) {
            showConnectedSystems();
            return;
        } else if (intent.contains("open camera") || intent.contains("camera kholo") || intent.equals("camera")) {
            speak("Camera khol raha hoon.");
            try { startActivity(new Intent("android.media.action.IMAGE_CAPTURE")); }
            catch (Exception e) { transcript.append("\nCamera abhi nahi khul paaya."); }
            return;
        } else if (intent.contains("open maps") || intent.contains("maps kholo") || intent.contains("google maps") || intent.contains("map kholo")) {
            speak("Maps khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q="))); }
            catch (Exception e) { transcript.append("\nMaps abhi nahi khul paaya."); }
            return;
        } else if (intent.contains("open calculator") || intent.contains("calculator kholo") || intent.equals("calculator")) {
            speak("Calculator khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)); }
            catch (Exception e) { transcript.append("\nCalculator app nahi mili."); }
            return;
        } else if (intent.contains("open calendar") || intent.contains("calendar kholo") || intent.equals("calendar")) {
            speak("Calendar khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)); }
            catch (Exception e) { transcript.append("\nCalendar app nahi mili."); }
            return;
        } else if (intent.contains("open clock") || intent.contains("clock kholo") || intent.contains("ghadi kholo")) {
            speak("Clock khol raha hoon.");
            try { startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS)); }
            catch (Exception e) { transcript.append("\nClock app nahi mili."); }
            return;
        } else if (intent.contains("wifi settings") || intent.contains("wifi kholo")) {
            speak("Wi-Fi settings khol raha hoon.");
            try { startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)); }
            catch (Exception e) { transcript.append("\nWi-Fi settings nahi khul paayi."); }
            return;
        } else if (intent.contains("bluetooth settings") || intent.contains("bluetooth kholo")) {
            speak("Bluetooth settings khol raha hoon.");
            try { startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)); }
            catch (Exception e) { transcript.append("\nBluetooth settings nahi khul paayi."); }
            return;
        } else if (intent.contains("location settings") || intent.contains("location kholo")) {
            speak("Location settings khol raha hoon.");
            try { startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)); }
            catch (Exception e) { transcript.append("\nLocation settings nahi khul paayi."); }
            return;
        } else if (intent.contains("call") || intent.contains("phone karo") || intent.contains("phone lagao") || intent.contains("ko phone karo") || intent.contains("ko call karo")) {
            String target = intent.replace("mere ", "").replace("meri ", "")
                    .replace("ko call karo", "").replace("call karo", "").replace("call", "")
                    .replace("ko phone karo", "").replace("phone karo", "").replace("phone lagao", "").trim();
            if (target.replaceAll("[^0-9+]", "").length() >= 5) {
                requestOrPlaceCall(target.replaceAll("[^0-9+]", ""), target);
            } else if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION_REQUEST);
                replyAndShow(original, "Contact permission allow karo, phir dobara bolo: " + target + " ko call karo.");
            } else {
                String number = findContactNumber(target);
                if (number == null) replyAndShow(original, "Contacts mein '" + target + "' nahi mila. Contact ka naam phonebook mein check karo.");
                else requestOrPlaceCall(number, target);
            }
            return;
        } else if (intent.startsWith("message ") || intent.startsWith("sms ") || intent.startsWith("text ")) {
            String body = intent.replaceFirst("^(message|sms|text)\\s+", "").trim();
            speak("Message draft khol raha hoon. Bhejne se pehle check kar lena.");
            try {
                Intent sms = new Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:"));
                sms.putExtra("sms_body", body);
                startActivity(sms);
            } catch (Exception e) { transcript.append("\nMessage app nahi khul paayi."); }
            return;
        } else if (intent.contains("open phone") || intent.contains("dialer kholo") || intent.contains("call screen kholo") || intent.equals("dialer")) {
            speak("Phone dialer khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_DIAL)); }
            catch (Exception e) { transcript.append("\nPhone app nahi khul paayi."); }
            return;
        } else if (intent.contains("open contacts") || intent.contains("contacts kholo") || intent.contains("contact kholo")) {
            speak("Contacts khol raha hoon.");
            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("content://contacts/people/"))); }
            catch (Exception e) {
                try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS)); }
                catch (Exception ignored) { transcript.append("\nContacts app nahi khul paayi."); }
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
            catch (Exception e) { transcript.append("\nSettings abhi nahi khul pa raha."); }
            return;
        } else if (intent.contains("set alarm") || intent.contains("alarm lagao") || intent.contains("alarm kholo")) {
            speak("Alarm screen khol raha hoon. Time select karke confirm kar dena.");
            try { startActivity(new Intent(AlarmClock.ACTION_SET_ALARM)); }
            catch (Exception e) { transcript.append("\nAlarm app available nahi hai."); }
            return;
        } else if (intent.contains("search for ") || intent.startsWith("google ") || intent.startsWith("search ")) {
            String query = intent.replaceFirst("^.*?search for ", "").replaceFirst("^google ", "").replaceFirst("^search ", "").trim();
            if (query.isEmpty()) query = intent;
            speak("Searching the web for " + query);
            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode(query)))); }
            catch (Exception e) { transcript.append("\nWeb search nahi khul paaya."); }
            return;
        } else if (intent.contains("open youtube") || intent.contains("youtube kholo") || intent.contains("youtube chalao") || intent.equals("youtube")) {
            speak("YouTube khol raha hoon.");
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                if (launch != null) startActivity(launch);
                else startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com")));
            } catch (Exception e) { transcript.append("\nYouTube nahi khul paaya."); }
            return;
        } else if (intent.contains("open chrome") || intent.contains("chrome kholo") || intent.equals("chrome")) {
            speak("Chrome khol raha hoon.");
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
                if (launch != null) startActivity(launch);
                else startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com")));
            } catch (Exception e) { transcript.append("\nChrome nahi khul paaya."); }
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
            requestAiReply(original);
            return;
        }
        replyAndShow(original, reply);
    }

    private void requestAiReply(String original) {
        aiOffline = false;
        setVoiceStatus("THINKING • CONNECTING TO JARVIS AI…");
        transcript.setText("YOU: " + original + "\n\nJARVIS: Thinking…");
        JarvisAiClient.ask(this, original, reply -> {
            aiOffline = reply != null && (reply.startsWith("JARVIS AI server mein error")
                    || reply.startsWith("JARVIS AI se connect nahi ho paaya"));
            if (aiOffline) setVoiceStatus("AI OFFLINE • LOCAL COMMANDS READY");
            replyAndShow(original, reply);
        });
    }

    private String findContactNumber(String requestedName) {
        String queryName = requestedName == null ? "" : requestedName.trim();
        if (queryName.isEmpty()) return null;
        Cursor cursor = null;
        try {
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
            String[] args = new String[]{"%" + queryName + "%"};
            cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    selection, args, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        } catch (SecurityException ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
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

    private void requestOrPlaceCall(String number, String name) {
        if (number == null || number.trim().isEmpty()) {
            status.setText("CONTACT NUMBER NOT AVAILABLE");
            speak("Contact ka number nahi mila. Call nahi ki gayi.");
            return;
        }
        String displayName = (name == null || name.trim().isEmpty()) ? "this contact" : name.trim();
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Confirm call")
                .setMessage(displayName + " ko call karna hai?")
                .setNegativeButton("No, cancel", (dialog, which) -> {
                    pendingDirectCallNumber = null;
                    pendingDirectCallName = null;
                    status.setText("CALL CANCELLED");
                    speak("Theek hai. Call cancel kar di.");
                })
                .setPositiveButton("Yes, call", (dialog, which) -> {
                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        pendingDirectCallNumber = number;
                        pendingDirectCallName = name;
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST);
                        status.setText("PHONE PERMISSION NEEDED");
                        speak("Phone permission allow karo. Confirmation mil chuki hai.");
                    } else {
                        placeDirectCall(number, name);
                    }
                }).show();
    }

    private void placeDirectCall(String number, String name) {
        try {
            speak((name == null || name.trim().isEmpty() ? "Contact" : name) + " ko call laga raha hoon.");
            startActivity(new Intent(Intent.ACTION_CALL,
                    android.net.Uri.parse("tel:" + android.net.Uri.encode(number))));
        } catch (SecurityException e) {
            status.setText("PHONE PERMISSION NOT AVAILABLE");
            speak("Phone permission nahi mili. Call nahi lag paayi.");
        } catch (Exception e) {
            status.setText("CALL COULD NOT BE STARTED");
            speak("Call nahi lag paayi. Phone app se try karo.");
        }
    }

    private void openDialer(String number) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + android.net.Uri.encode(number))));
        } catch (Exception e) {
            status.setText("PLEASE OPEN PHONE AND DIAL " + number);
        }
    }

    private void replyAndShow(String original, String reply) {
        transcript.setText("YOU: " + original + "\n\nJARVIS: " + reply);
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

    private void applySavedVoiceSettings() {
        if (tts == null) return;
        String style = preferences.getString("voice_style", "normal");
        float rate = "calm".equals(style) ? 0.82f : ("energetic".equals(style) ? 1.05f : 0.92f);
        float pitch = preferences.getFloat("voice_pitch", 1.0f);
        tts.setSpeechRate(rate);
        tts.setPitch(pitch);
        String savedVoice = preferences.getString("voice_name", "");
        if (!savedVoice.isEmpty() && android.os.Build.VERSION.SDK_INT >= 21) {
            try {
                java.util.Set<android.speech.tts.Voice> voices = tts.getVoices();
                if (voices != null) {
                    for (android.speech.tts.Voice voice : voices) {
                        if (savedVoice.equals(voice.getName())) {
                            tts.setVoice(voice);
                            break;
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
    }

    private void showVoiceSettings() {
        if (tts == null) {
            status.setText("VOICE ENGINE IS NOT READY");
            return;
        }

        java.util.ArrayList<android.speech.tts.Voice> available = new java.util.ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            try {
                java.util.Set<android.speech.tts.Voice> voices = tts.getVoices();
                if (voices != null) {
                    for (android.speech.tts.Voice voice : voices) {
                        if (voice != null && !voice.isNetworkConnectionRequired()) available.add(voice);
                    }
                }
            } catch (Exception ignored) { }
        }

        java.util.Collections.sort(available, (a, b) ->
                a.getLocale().toLanguageTag().compareToIgnoreCase(b.getLocale().toLanguageTag()));
        String[] labels = new String[available.size()];
        int selected = -1;
        String saved = preferences.getString("voice_name", "");
        for (int i = 0; i < available.size(); i++) {
            android.speech.tts.Voice voice = available.get(i);
            labels[i] = voice.getName() + " • " + voice.getLocale().toLanguageTag();
            if (voice.getName().equals(saved)) selected = i;
        }

        String voiceSummary = labels.length == 0
                ? "No additional offline voices are currently listed by the phone's speech engine. You can still change speech speed and pitch."
                : "Choose an installed offline voice. Names and language tags are shown as supplied by Android; male/female availability depends on the installed TTS engine.";
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • Voice Studio")
                .setMessage(voiceSummary)
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    android.speech.tts.Voice chosen = available.get(which);
                    int result = tts.setVoice(chosen);
                    if (result == TextToSpeech.SUCCESS) {
                        preferences.edit().putString("voice_name", chosen.getName()).apply();
                        status.setText("VOICE SELECTED • " + chosen.getLocale().toLanguageTag());
                        speak("Voice settings updated. This is my selected voice.");
                    } else {
                        status.setText("VOICE COULD NOT BE SELECTED");
                    }
                    dialog.dismiss();
                })
                .setPositiveButton("Speech style", (dialog, which) -> showSpeechStyleOptions())
                .setNeutralButton("Pitch", (dialog, which) -> showVoicePitchOptions())
                .setNegativeButton("Close", null)
                .show();
    }

    private void showSpeechStyleOptions() {
        String[] styles = {"Calm", "Normal", "Energetic"};
        String current = preferences.getString("voice_style", "normal");
        int selected = "calm".equals(current) ? 0 : ("energetic".equals(current) ? 2 : 1);
        new AlertDialog.Builder(this).setTitle("JARVIS • Speech speed")
                .setSingleChoiceItems(styles, selected, (dialog, which) -> {
                    String value = which == 0 ? "calm" : (which == 2 ? "energetic" : "normal");
                    preferences.edit().putString("voice_style", value).apply();
                    applySavedVoiceSettings();
                    status.setText("SPEECH STYLE • " + styles[which].toUpperCase(Locale.ROOT));
                    dialog.dismiss();
                    speak("Speech style updated.");
                }).setNegativeButton("Cancel", null).show();
    }

    private void showVoicePitchOptions() {
        String[] pitches = {"Lower", "Normal", "Higher"};
        float current = preferences.getFloat("voice_pitch", 1.0f);
        int selected = current < 0.95f ? 0 : (current > 1.05f ? 2 : 1);
        new AlertDialog.Builder(this).setTitle("JARVIS • Voice pitch")
                .setSingleChoiceItems(pitches, selected, (dialog, which) -> {
                    float value = which == 0 ? 0.88f : (which == 2 ? 1.12f : 1.0f);
                    preferences.edit().putFloat("voice_pitch", value).apply();
                    applySavedVoiceSettings();
                    status.setText("VOICE PITCH • " + pitches[which].toUpperCase(Locale.ROOT));
                    dialog.dismiss();
                    speak("Voice pitch updated.");
                }).setNegativeButton("Cancel", null).show();
    }

    private void speak(String text) {
        if (tts != null) {
            applySavedVoiceSettings();
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply");
            if (result == TextToSpeech.ERROR) setVoiceStatus("VOICE OUTPUT ERROR • TAP TO SPEAK");
        } else {
            setVoiceStatus("VOICE OUTPUT UNAVAILABLE");
        }
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) { }
            recognizer.destroy();
        }
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
