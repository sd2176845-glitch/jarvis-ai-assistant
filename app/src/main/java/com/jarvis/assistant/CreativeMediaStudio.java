package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Owner-invoked creative planning tools. Produces original text prompts/outlines, not media files. */
public final class CreativeMediaStudio {
    private CreativeMediaStudio() {}

    private interface TextAction { void submit(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] tools = {
                "Image Concept Builder • original image prompt",
                "Video Storyboard Studio • scenes / narration / captions",
                "Presentation Planner • slide outline / speaker notes",
                "Creative Project Organizer • saved drafts"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Creative Media Studio")
                .setItems(tools, (dialog, which) -> {
                    if (which == 0) imageConcept(activity, status, transcript);
                    else if (which == 1) videoStoryboard(activity, status, transcript);
                    else if (which == 2) presentationPlanner(activity, status, transcript);
                    else showProjects(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void imageConcept(Activity activity, TextView status, TextView transcript) {
        String[] formats = {"Square 1:1", "Portrait 9:16", "Landscape 16:9"};
        new AlertDialog.Builder(activity).setTitle("Choose image format")
                .setItems(formats, (dialog, which) -> askDetails(activity,
                        "Image concept",
                        "Describe the scene, subject and mood. Avoid private or identifying details.",
                        1200,
                        brief -> submit(activity, status, transcript,
                                "Create an original, detailed image-generation prompt based on this brief: " + brief
                                        + ". Output: (1) one clear main prompt, (2) composition and camera framing, "
                                        + "(3) lighting and color palette, (4) background and visual details, "
                                        + "(5) a short accessibility alt-text description. Format: " + formats[which] + ". "
                                        + "Keep the concept safe and age-appropriate. Do not imitate a living artist or reproduce a copyrighted character, "
                                        + "logo or existing artwork. Do not create sexualized content, graphic violence, deceptive identity documents, "
                                        + "or imagery that encourages harmful body ideals. This is a text prompt only; do not claim an image file was generated.",
                                "IMAGE PROMPT READY")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void videoStoryboard(Activity activity, TextView status, TextView transcript) {
        String[] lengths = {"15 seconds", "30 seconds", "60 seconds", "90 seconds"};
        new AlertDialog.Builder(activity).setTitle("Choose video length")
                .setItems(lengths, (dialog, which) -> askDetails(activity,
                        "Video concept",
                        "Describe the video idea and intended platform/audience.",
                        1200,
                        brief -> submit(activity, status, transcript,
                                "Create an original, safe video storyboard for this concept: " + brief
                                        + ". Target duration: " + lengths[which] + ". Return a table with scene number, "
                                        + "approximate time, visual direction, camera movement, voiceover/dialogue and on-screen caption. "
                                        + "Add a short opening hook, coherent transitions, an ending and a practical shot checklist. "
                                        + "Keep all scenes age-appropriate and non-graphic. Use original wording; do not reproduce song lyrics, "
                                        + "copyrighted scripts or imitate a specific living creator. This is a storyboard text plan only; "
                                        + "do not claim a video was rendered.",
                                "VIDEO STORYBOARD READY")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void presentationPlanner(Activity activity, TextView status, TextView transcript) {
        String[] sizes = {"5 slides", "8 slides", "10 slides", "12 slides"};
        new AlertDialog.Builder(activity).setTitle("Choose presentation size")
                .setItems(sizes, (dialog, which) -> askDetails(activity,
                        "Presentation brief",
                        "Enter topic, audience and purpose. Do not include confidential information.",
                        1200,
                        brief -> submit(activity, status, transcript,
                                "Prepare an original presentation plan for this brief: " + brief
                                        + ". Length: " + sizes[which] + ". Return a slide-by-slide outline with concise titles, "
                                        + "3 to 5 key bullets per slide, suggested visual type, speaker notes and a clear opening/conclusion. "
                                        + "Flag facts that need verification and do not invent citations, statistics or sources. "
                                        + "Use age-appropriate, accessible language and avoid exposing personal or confidential data. "
                                        + "This is an outline only; do not claim a PowerPoint or PDF file was created.",
                                "PRESENTATION OUTLINE READY")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void askDetails(Activity activity, String title, String hint,
                                   int maxLength, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(3);
        input.setMaxLines(6);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setView(layout)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Enter a short creative brief first.", Toast.LENGTH_SHORT).show();
                    } else if (value.length() > maxLength) {
                        Toast.makeText(activity, "Please keep the brief under " + maxLength + " characters.", Toast.LENGTH_LONG).show();
                    } else {
                        action.submit(value);
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private static void submit(Activity activity, TextView status, TextView transcript,
                               String prompt, String label) {
        new AlertDialog.Builder(activity).setTitle("Send creative brief?")
                .setMessage("Only the brief and format choice you entered will be sent to JARVIS AI. Do not include passwords, private details or confidential material. This tool creates text prompts and plans, not image/video/presentation files.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate plan", (dialog, which) -> {
                    status.setText("CREATIVE MEDIA STUDIO • PROCESSING");
                    transcript.setText("Preparing your creative plan…");
                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        status.setText(label);
                        transcript.setText(reply);
                        new AlertDialog.Builder(activity).setTitle("JARVIS • " + label)
                                .setMessage(reply)
                                .setPositiveButton("Save draft", (resultDialog, resultWhich) ->
                                        saveProject(activity, label, reply))
                                .setNegativeButton("Done", null).show();
                    });
                }).show();
    }

    private static final String PROJECTS_KEY = "creative_projects_v1";

    private static void saveProject(Activity activity, String type, String content) {
        try {
            android.content.SharedPreferences prefs = activity.getSharedPreferences(
                    "jarvis_preferences", Activity.MODE_PRIVATE);
            org.json.JSONArray projects = new org.json.JSONArray(prefs.getString(PROJECTS_KEY, "[]"));
            if (projects.length() >= 30) {
                Toast.makeText(activity, "Project list is full. Review or delete old drafts first.", Toast.LENGTH_LONG).show();
                return;
            }
            org.json.JSONObject item = new org.json.JSONObject();
            item.put("type", type);
            item.put("content", content);
            item.put("created_at", System.currentTimeMillis());
            item.put("status", "Draft • owner-created");
            projects.put(item);
            prefs.edit().putString(PROJECTS_KEY, projects.toString()).apply();
            Toast.makeText(activity, "Creative draft saved on this device.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Draft could not be saved.", Toast.LENGTH_LONG).show();
        }
    }

    private static void showProjects(Activity activity) {
        android.content.SharedPreferences prefs = activity.getSharedPreferences(
                "jarvis_preferences", Activity.MODE_PRIVATE);
        org.json.JSONArray projects;
        try {
            projects = new org.json.JSONArray(prefs.getString(PROJECTS_KEY, "[]"));
        } catch (Exception e) {
            new AlertDialog.Builder(activity).setTitle("Creative Project Organizer")
                    .setMessage("Saved drafts could not be read. Nothing was changed.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        if (projects.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Creative Project Organizer")
                    .setMessage("No drafts saved yet. Generate a creative plan and choose Save draft.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        String[] labels = new String[projects.length()];
        for (int i = 0; i < projects.length(); i++) {
            org.json.JSONObject item = projects.optJSONObject(i);
            labels[i] = item == null ? "Unavailable draft"
                    : item.optString("type", "Creative draft") + " • Draft";
        }
        new AlertDialog.Builder(activity).setTitle("Creative Project Organizer")
                .setItems(labels, (dialog, which) -> {
                    org.json.JSONObject item = projects.optJSONObject(which);
                    if (item != null) {
                        String date = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",
                                java.util.Locale.getDefault()).format(
                                new java.util.Date(item.optLong("created_at", 0L)));
                        new AlertDialog.Builder(activity).setTitle(item.optString("type", "Creative draft"))
                                .setMessage("DRAFT • NOT A RENDERED MEDIA FILE\nSaved: " + date + "\n\n"
                                        + item.optString("content", ""))
                                .setPositiveButton("Close", null).show();
                    }
                })
                .setNegativeButton("Close", null)
                .setNeutralButton("Delete all", (dialog, which) ->
                        new AlertDialog.Builder(activity).setTitle("Delete all creative drafts?")
                                .setMessage("This removes only the saved drafts stored by JARVIS on this device.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (d, w) -> {
                                    prefs.edit().remove(PROJECTS_KEY).apply();
                                    Toast.makeText(activity, "Creative drafts cleared.", Toast.LENGTH_SHORT).show();
                                }).show())
                .show();
    }
}
