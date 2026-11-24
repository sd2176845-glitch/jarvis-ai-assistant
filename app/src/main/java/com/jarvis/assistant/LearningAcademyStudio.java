package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/** Structured learning tools. Sends only the learner's selected topic through the memory-isolated AI path. */
public final class LearningAcademyStudio {
    private LearningAcademyStudio() {}

    private interface ChoiceAction { void choose(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] tools = {
                "Coding Tutor • guided lessons and practice",
                "Science Explorer • clear concept explanations",
                "Technology Roadmap • step-by-step learning plan",
                "Learning Notebook • saved lessons",
                "Learning Progress & Revision"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • AI Learning Academy")
                .setItems(tools, (d, which) -> {
                    if (which == 0) codingTutor(activity, status, transcript);
                    else if (which == 1) scienceExplorer(activity, status, transcript);
                    else if (which == 2) technologyRoadmap(activity, status, transcript);
                    else if (which == 3) showNotebook(activity);
                    else showProgressDashboard(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void codingTutor(Activity activity, TextView status, TextView transcript) {
        String[] languages = {"Python", "Java", "Kotlin", "JavaScript"};
        new AlertDialog.Builder(activity).setTitle("Choose coding language")
                .setItems(languages, (d, which) ->
                        chooseLevel(activity, level -> askTopic(activity, "Coding Tutor",
                                "What coding topic do you want to learn in " + languages[which] + "?",
                                topic -> submit(activity, status, transcript,
                                        "You are a patient programming tutor. Teach " + topic + " in " + languages[which]
                                                + " at " + level + " level. Structure the lesson as: (1) learning goal, (2) simple explanation, "
                                                + "(3) one small, benign code example, (4) explain each important line, (5) common beginner mistake, "
                                                + "(6) one short practice task, (7) answer key in a separate final section. Keep code runnable and accurate. "
                                                + "Do not generate malware, credential theft, covert surveillance, backdoors, exploit instructions or harmful code. "
                                                + "If the topic is unsafe, redirect to a harmless programming concept.",
                                        "CODING LESSON READY"))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void scienceExplorer(Activity activity, TextView status, TextView transcript) {
        String[] subjects = {"Physics", "Chemistry", "Biology", "Space & Earth Science"};
        new AlertDialog.Builder(activity).setTitle("Choose science subject")
                .setItems(subjects, (d, which) ->
                        chooseLevel(activity, level -> askTopic(activity, "Science Explorer",
                                "Enter a " + subjects[which] + " concept or question",
                                topic -> submit(activity, status, transcript,
                                        "You are an age-appropriate science teacher. Explain " + topic + " in "
                                                + subjects[which] + " at " + level + " level. Include: (1) simple definition, "
                                                + "(2) how it works, (3) one everyday example, (4) key terms, (5) one misconception to avoid, "
                                                + "(6) three quick review questions with answers at the end. Be accurate and distinguish established facts from uncertainty. "
                                                + "Do not give hazardous experiment instructions, dangerous chemical procedures or unsafe activity steps; use safe conceptual examples only.",
                                        "SCIENCE EXPLANATION READY"))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void technologyRoadmap(Activity activity, TextView status, TextView transcript) {
        String[] tracks = {
                "Android App Development",
                "Website Development",
                "Artificial Intelligence Basics",
                "Digital Safety & Defensive Cybersecurity",
                "Computer Science Foundations"
        };
        new AlertDialog.Builder(activity).setTitle("Choose technology track")
                .setItems(tracks, (d, which) ->
                        chooseLevel(activity, level -> askTopic(activity, "Technology Roadmap",
                                "What is your goal or topic for " + tracks[which] + "?",
                                goal -> submit(activity, status, transcript,
                                        "Create a practical, age-appropriate learning roadmap for " + tracks[which]
                                                + ". Learner level: " + level + ". Goal: " + goal + ". "
                                                + "Organize it into stages with prerequisites, concepts to learn, free practice ideas, "
                                                + "small safe projects, estimated effort per stage, and checkpoints to assess understanding. "
                                                + "Keep projects lawful and privacy-respecting. For cybersecurity, cover defensive fundamentals, "
                                                + "account security, safe lab concepts and responsible disclosure only; do not provide attack, intrusion, "
                                                + "credential theft, malware, evasion or surveillance instructions. Avoid promising job outcomes.",
                                        "LEARNING ROADMAP READY"))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void chooseLevel(Activity activity, ChoiceAction action) {
        String[] levels = {"Beginner", "Intermediate", "Advanced"};
        new AlertDialog.Builder(activity).setTitle("Choose learning level")
                .setItems(levels, (d, which) -> action.choose(levels[which]))
                .setNegativeButton("Cancel", null).show();
    }

    private interface TopicAction { void submit(String topic); }

    private static void askTopic(Activity activity, String title, String hint, TopicAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setView(layout)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Enter a topic first.", Toast.LENGTH_SHORT).show();
                    } else if (value.length() > 300) {
                        Toast.makeText(activity, "Keep the topic under 300 characters.", Toast.LENGTH_LONG).show();
                    } else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void submit(Activity activity, TextView status, TextView transcript,
                               String prompt, String label) {
        new AlertDialog.Builder(activity).setTitle("Send learning request?")
                .setMessage("Only the topic and learning choices you entered will be sent to the JARVIS AI service. Recent conversation memory, saved notes and contacts are not attached or updated.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate lesson", (d, w) -> {
                    status.setText("AI LEARNING ACADEMY • PROCESSING");
                    transcript.setText("Preparing your learning material…");
                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        status.setText(label);
                        transcript.setText(reply);
                        new AlertDialog.Builder(activity).setTitle("JARVIS • " + label)
                                .setMessage(reply)
                                .setPositiveButton("Save lesson", (resultDialog, resultWhich) ->
                                        saveLesson(activity, label, reply))
                                .setNegativeButton("Done", null).show();
                    });
                }).show();
    }

    private static final String NOTEBOOK_KEY = "learning_notebook_v1";

    private static void saveLesson(Activity activity, String label, String content) {
        try {
            android.content.SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
            org.json.JSONArray entries = new org.json.JSONArray(prefs.getString(NOTEBOOK_KEY, "[]"));
            if (entries.length() >= 40) {
                Toast.makeText(activity, "Learning notebook is full. Delete old lessons first.", Toast.LENGTH_LONG).show();
                return;
            }
            org.json.JSONObject item = new org.json.JSONObject();
            item.put("label", label);
            item.put("content", content);
            long createdAt = System.currentTimeMillis();
            item.put("created_at", createdAt);
            item.put("updated_at", createdAt);
            item.put("status", LearningAcademyRules.NOT_STARTED);
            item.put("review_on", "");
            entries.put(item);
            prefs.edit().putString(NOTEBOOK_KEY, entries.toString()).apply();
            Toast.makeText(activity, "Lesson saved on this device.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Lesson could not be saved.", Toast.LENGTH_LONG).show();
        }
    }

    private static android.content.SharedPreferences notebookPreferences(Activity activity) {
        return activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
    }

    private static org.json.JSONArray readNotebook(Activity activity) {
        try {
            return new org.json.JSONArray(notebookPreferences(activity).getString(NOTEBOOK_KEY, "[]"));
        } catch (Exception e) {
            new AlertDialog.Builder(activity).setTitle("Learning Notebook")
                    .setMessage("Saved lessons could not be read. Nothing was changed.")
                    .setPositiveButton("Close", null).show();
            return null;
        }
    }

    private static void showProgressDashboard(Activity activity) {
        org.json.JSONArray entries = readNotebook(activity);
        if (entries == null) return;
        String[] statuses = new String[entries.length()];
        String[] reviewDates = new String[entries.length()];
        for (int i = 0; i < entries.length(); i++) {
            org.json.JSONObject item = entries.optJSONObject(i);
            statuses[i] = item == null ? null : item.optString("status", LearningAcademyRules.NOT_STARTED);
            reviewDates[i] = item == null ? null : item.optString("review_on", "");
        }
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
        String report = LearningAcademyRules.buildProgressSummary(statuses, reviewDates, today)
                + "\\n\\nRevision dates are planning notes only; this overview does not send notifications."; 
        new AlertDialog.Builder(activity).setTitle("Learning Progress & Revision")
                .setMessage(report)
                .setPositiveButton("Open notebook", (dialog, which) -> showNotebook(activity))
                .setNegativeButton("Close", null).show();
    }

    private static void showNotebook(Activity activity) {
        org.json.JSONArray entries = readNotebook(activity);
        if (entries == null) return;
        if (entries.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Learning Notebook")
                    .setMessage("No saved lessons yet. Generate a lesson and choose Save lesson.")
                    .setPositiveButton("Close", null)
                    .setNeutralButton("Progress overview", (d, w) -> showProgressDashboard(activity)).show();
            return;
        }
        String[] labels = new String[entries.length()];
        for (int i = 0; i < entries.length(); i++) {
            org.json.JSONObject item = entries.optJSONObject(i);
            if (item == null) {
                labels[i] = "Unavailable lesson";
                continue;
            }
            String review = item.optString("review_on", "");
            labels[i] = item.optString("label", "Learning lesson") + " • "
                    + item.optString("status", LearningAcademyRules.NOT_STARTED)
                    + (review.isEmpty() ? "" : " • Review " + review);
        }
        new AlertDialog.Builder(activity).setTitle("Learning Notebook • select a lesson")
                .setItems(labels, (dialog, which) -> showLessonActions(activity, which))
                .setNegativeButton("Close", null)
                .setNeutralButton("Progress overview", (d, w) -> showProgressDashboard(activity))
                .show();
    }

    private static void showLessonActions(Activity activity, int index) {
        org.json.JSONArray entries = readNotebook(activity);
        if (entries == null || index < 0 || index >= entries.length()) return;
        org.json.JSONObject item = entries.optJSONObject(index);
        if (item == null) return;
        long createdAt = item.optLong("created_at", 0L);
        String label = item.optString("label", "Learning lesson");
        String status = item.optString("status", LearningAcademyRules.NOT_STARTED);
        String review = item.optString("review_on", "");
        String saved = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new java.util.Date(createdAt));
        String message = "Status: " + status + "\\nSaved: " + saved
                + "\\nRevision date: " + (review.isEmpty() ? "Not set" : review)
                + "\\n\\n" + item.optString("content", "");
        String[] actions = {"Read lesson", "Mark not started", "Mark in progress",
                "Mark completed", "Set revision date", "Clear revision date", "Delete lesson"};
        new AlertDialog.Builder(activity).setTitle(label).setMessage(message)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) showLessonContent(activity, label, item.optString("content", ""));
                    else if (which == 1) updateLessonStatus(activity, index, createdAt, LearningAcademyRules.NOT_STARTED);
                    else if (which == 2) updateLessonStatus(activity, index, createdAt, LearningAcademyRules.IN_PROGRESS);
                    else if (which == 3) updateLessonStatus(activity, index, createdAt, LearningAcademyRules.COMPLETED);
                    else if (which == 4) setRevisionDate(activity, index, createdAt);
                    else if (which == 5) updateRevisionDate(activity, index, createdAt, "");
                    else confirmDeleteLesson(activity, index, createdAt, label);
                }).setNegativeButton("Close", null).show();
    }

    private static void showLessonContent(Activity activity, String label, String content) {
        new AlertDialog.Builder(activity).setTitle(label).setMessage(content)
                .setPositiveButton("Done", null).show();
    }

    private static void updateLessonStatus(Activity activity, int index, long createdAt, String status) {
        org.json.JSONArray entries = readNotebook(activity);
        if (entries == null || index < 0 || index >= entries.length()) return;
        org.json.JSONObject item = entries.optJSONObject(index);
        if (item == null || item.optLong("created_at", -1L) != createdAt) {
            Toast.makeText(activity, "The selected lesson changed. Reopen the notebook and try again.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            item.put("status", status);
            item.put("updated_at", System.currentTimeMillis());
            if (notebookPreferences(activity).edit().putString(NOTEBOOK_KEY, entries.toString()).commit()) {
                Toast.makeText(activity, "Learning progress updated.", Toast.LENGTH_SHORT).show();
                showProgressDashboard(activity);
            } else {
                Toast.makeText(activity, "Progress could not be saved.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Progress could not be updated.", Toast.LENGTH_LONG).show();
        }
    }

    private static void setRevisionDate(Activity activity, int index, long createdAt) {
        java.util.Calendar initial = java.util.Calendar.getInstance();
        org.json.JSONArray entries = readNotebook(activity);
        if (entries == null || index < 0 || index >= entries.length()) return;
        org.json.JSONObject item = entries.optJSONObject(index);
        if (item == null || item.optLong("created_at", -1L) != createdAt) return;
        String existing = item.optString("review_on", "");
        if (LearningAcademyRules.isValidDate(existing)) {
            try {
                java.util.Date parsed = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(existing);
                if (parsed != null) initial.setTime(parsed);
            } catch (Exception ignored) { }
        }
        new android.app.DatePickerDialog(activity, (view, year, month, day) -> {
            String selected = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            updateRevisionDate(activity, index, createdAt, selected);
        }, initial.get(java.util.Calendar.YEAR), initial.get(java.util.Calendar.MONTH),
                initial.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private static void updateRevisionDate(Activity activity, int index, long createdAt, String reviewDate) {
        if (!reviewDate.isEmpty() && !LearningAcademyRules.isValidDate(reviewDate)) {
            Toast.makeText(activity, "Choose a real YYYY-MM-DD revision date.", Toast.LENGTH_LONG).show();
            return;
        }
        org.json.JSONArray entries = readNotebook(activity);
        if (entries == null || index < 0 || index >= entries.length()) return;
        org.json.JSONObject item = entries.optJSONObject(index);
        if (item == null || item.optLong("created_at", -1L) != createdAt) return;
        try {
            item.put("review_on", reviewDate);
            item.put("updated_at", System.currentTimeMillis());
            if (notebookPreferences(activity).edit().putString(NOTEBOOK_KEY, entries.toString()).commit()) {
                Toast.makeText(activity, reviewDate.isEmpty() ? "Revision date cleared." : "Revision date saved on this device.", Toast.LENGTH_SHORT).show();
                showProgressDashboard(activity);
            } else {
                Toast.makeText(activity, "Revision date could not be saved.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Revision date could not be updated.", Toast.LENGTH_LONG).show();
        }
    }

    private static void confirmDeleteLesson(Activity activity, int index, long createdAt, String label) {
        new AlertDialog.Builder(activity).setTitle("Delete this lesson?")
                .setMessage("This removes only the selected lesson from the Learning Notebook on this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    org.json.JSONArray entries = readNotebook(activity);
                    if (entries == null || index < 0 || index >= entries.length()) return;
                    org.json.JSONObject item = entries.optJSONObject(index);
                    if (item == null || item.optLong("created_at", -1L) != createdAt) return;
                    entries.remove(index);
                    if (notebookPreferences(activity).edit().putString(NOTEBOOK_KEY, entries.toString()).commit()) {
                        Toast.makeText(activity, "Lesson deleted.", Toast.LENGTH_SHORT).show();
                        showNotebook(activity);
                    } else {
                        Toast.makeText(activity, "Lesson could not be deleted.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }
}
