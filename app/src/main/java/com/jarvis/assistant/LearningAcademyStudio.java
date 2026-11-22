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
                "Learning Notebook • saved lessons"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • AI Learning Academy")
                .setItems(tools, (d, which) -> {
                    if (which == 0) codingTutor(activity, status, transcript);
                    else if (which == 1) scienceExplorer(activity, status, transcript);
                    else if (which == 2) technologyRoadmap(activity, status, transcript);
                    else showNotebook(activity);
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
            item.put("created_at", System.currentTimeMillis());
            entries.put(item);
            prefs.edit().putString(NOTEBOOK_KEY, entries.toString()).apply();
            Toast.makeText(activity, "Lesson saved on this device.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Lesson could not be saved.", Toast.LENGTH_LONG).show();
        }
    }

    private static void showNotebook(Activity activity) {
        android.content.SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
        org.json.JSONArray entries;
        try {
            entries = new org.json.JSONArray(prefs.getString(NOTEBOOK_KEY, "[]"));
        } catch (Exception e) {
            new AlertDialog.Builder(activity).setTitle("Learning Notebook")
                    .setMessage("Saved lessons could not be read. Nothing was changed.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        if (entries.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Learning Notebook")
                    .setMessage("No saved lessons yet. Generate a lesson and choose Save lesson.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        String[] labels = new String[entries.length()];
        for (int i = 0; i < entries.length(); i++) {
            org.json.JSONObject item = entries.optJSONObject(i);
            labels[i] = item == null ? "Unavailable lesson" : item.optString("label", "Learning lesson");
        }
        new AlertDialog.Builder(activity).setTitle("Learning Notebook")
                .setItems(labels, (dialog, which) -> {
                    org.json.JSONObject item = entries.optJSONObject(which);
                    if (item != null) {
                        String date = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                .format(new java.util.Date(item.optLong("created_at", 0L)));
                        new AlertDialog.Builder(activity).setTitle(item.optString("label", "Learning lesson"))
                                .setMessage("OWNER-SAVED LEARNING NOTE\\nSaved: " + date + "\\n\\n"
                                        + item.optString("content", ""))
                                .setPositiveButton("Close", null).show();
                    }
                })
                .setNegativeButton("Close", null)
                .setNeutralButton("Delete all", (dialog, which) ->
                        new AlertDialog.Builder(activity).setTitle("Delete saved lessons?")
                                .setMessage("This removes only the learning notes stored by JARVIS on this device.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (d, w) -> {
                                    prefs.edit().remove(NOTEBOOK_KEY).apply();
                                    Toast.makeText(activity, "Learning notebook cleared.", Toast.LENGTH_SHORT).show();
                                }).show())
                .show();
    }
}
