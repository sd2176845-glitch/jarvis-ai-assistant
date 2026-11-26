package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Text-first creative planning tools; they do not render or export image/video/presentation files. */
public final class CreativeMediaStudio {
    private CreativeMediaStudio() {}

    private interface ChoiceAction { void choose(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Image Concept Builder • original image prompt",
                "Video Script Planner • hook / narration / scenes",
                "Storyboard & Shot List • scene-by-scene plan",
                "Presentation Builder • slides / speaker notes",
                "Creative Project Organizer • saved drafts",
                "Production Preflight • rights / consent / accessibility"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Creative Media Studio")
                .setItems(options, (d, which) -> {
                    if (which == 0) imageConcept(activity, status, transcript);
                    else if (which == 1) videoScript(activity, status, transcript);
                    else if (which == 2) storyboard(activity, status, transcript);
                    else if (which == 3) presentation(activity, status, transcript);
                    else if (which == 4) showProjects(activity);
                    else productionPreflight(activity);
                }).setNegativeButton("Close", null).show();
    }



    private static void productionPreflight(Activity activity) {
        String[] types = {"Image / graphic", "Video / short-form", "Presentation"};
        new AlertDialog.Builder(activity).setTitle("Creator Production Preflight")
                .setItems(types, (dialog, which) ->
                        askTopic(activity, "Production brief",
                                "Describe the project (maximum 500 characters)",
                                brief -> {
                                    String checklist = CreativeProductionRules.buildChecklist(types[which], brief);
                                    new AlertDialog.Builder(activity).setTitle("JARVIS • Production Checklist")
                                            .setMessage(checklist)
                                            .setPositiveButton("Done", null)
                                            .show();
                                }))
                .setNegativeButton("Cancel", null).show();
    }

    private static void imageConcept(Activity activity, TextView status, TextView transcript) {
        String[] formats = {"Square 1:1", "Portrait 9:16", "Landscape 16:9"};
        new AlertDialog.Builder(activity).setTitle("Choose image format")
                .setItems(formats, (dialog, which) ->
                        askTopic(activity, "Image Concept Builder",
                                "Describe the subject, scene, mood and audience. Avoid private identifying details.",
                                concept -> submit(activity, status, transcript,
                                        "Create an original, age-appropriate image-generation prompt from this brief: " + concept
                                                + ". Format: " + formats[which] + ". Return one main prompt, composition and framing, "
                                                + "lighting and color palette, background details, plus concise accessibility alt text. "
                                                + "Do not imitate a living artist or reproduce copyrighted characters, logos or existing artwork. "
                                                + "Avoid sexualized content, graphic violence, deceptive identity documents and harmful body ideals. "
                                                + "This is a text prompt only; do not claim an image file was generated.",
                                        "IMAGE PROMPT READY")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void videoScript(Activity activity, TextView status, TextView transcript) {
        String[] formats = {"Vertical short video", "Educational explainer", "Storytelling video"};
        new AlertDialog.Builder(activity).setTitle("Choose video format")
                .setItems(formats, (d, which) ->
                        chooseDuration(activity, duration ->
                                askTopic(activity, "Video Script Planner",
                                        "Enter the video topic and intended audience",
                                        topic -> submit(activity, status, transcript,
                                                "Write an original video script for " + formats[which]
                                                        + ", target duration " + duration + ". Topic and audience: " + topic
                                                        + ". Include a strong but truthful opening hook, timed narration, scene descriptions, "
                                                        + "on-screen text, optional sound-design notes, accessible captions and a closing line. "
                                                        + "Keep timing realistic and distinguish factual claims from creative framing. "
                                                        + "Do not include copyrighted song lyrics, copied scripts, dangerous stunts or instructions for risky acts. "
                                                        + "Do not imply a real person endorsed the content without permission. "
                                                        + "Return a text script only; do not claim a video file was rendered.",
                                                "VIDEO SCRIPT READY"))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void chooseDuration(Activity activity, ChoiceAction action) {
        String[] durations = {"15 seconds", "30 seconds", "60 seconds", "2 minutes"};
        new AlertDialog.Builder(activity).setTitle("Choose target duration")
                .setItems(durations, (d, which) -> action.choose(durations[which]))
                .setNegativeButton("Cancel", null).show();
    }

    private static void storyboard(Activity activity, TextView status, TextView transcript) {
        String[] styles = {"Educational", "Product demonstration", "Original short story"};
        new AlertDialog.Builder(activity).setTitle("Choose storyboard style")
                .setItems(styles, (d, which) ->
                        askTopic(activity, "Storyboard & Shot List",
                                "Describe the concept, location and intended audience",
                                concept -> submit(activity, status, transcript,
                                        "Create an original storyboard and practical shot list for a "
                                                + styles[which] + " video based on: " + concept + ". "
                                                + "Use a table-like numbered sequence with scene number, estimated duration, visual action, "
                                                + "framing/composition, narration or dialogue, on-screen text and transition. "
                                                + "Add a concise continuity checklist and accessibility notes for captions/contrast. "
                                                + "Keep all actions safe and ordinary; do not suggest dangerous stunts, weapons, risky challenges or hazardous setups. "
                                                + "Do not use a real person's likeness or private location without permission. "
                                                + "Return a text storyboard only; no media file is rendered.",
                                        "STORYBOARD READY")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void presentation(Activity activity, TextView status, TextView transcript) {
        String[] audiences = {"School / learning", "Project proposal", "General audience"};
        new AlertDialog.Builder(activity).setTitle("Choose presentation audience")
                .setItems(audiences, (d, which) ->
                        askTopic(activity, "Presentation Builder",
                                "Enter topic, purpose and preferred slide count (5–10)",
                                details -> submit(activity, status, transcript,
                                        "Create a clear presentation plan for " + audiences[which]
                                                + ". User topic, purpose and requested slide count: " + details
                                                + ". Return a title slide, a logical slide-by-slide outline, concise bullets, "
                                                + "speaker notes, suggested original visual ideas and a closing summary. "
                                                + "Include a sources-to-verify section for factual claims; never invent citations, URLs or statistics. "
                                                + "Keep the language appropriate for the selected audience. "
                                                + "Return a text outline only; do not claim a PowerPoint file was created.",
                                        "PRESENTATION OUTLINE READY")))
                .setNegativeButton("Cancel", null).show();
    }

    private interface TopicAction { void submit(String value); }

    private static void askTopic(Activity activity, String title, String hint, TopicAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(3);
        input.setMaxLines(7);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setView(layout)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Enter a topic or concept first.", Toast.LENGTH_SHORT).show();
                    } else if (value.length() > 500) {
                        Toast.makeText(activity, "Keep the creative brief under 500 characters.", Toast.LENGTH_LONG).show();
                    } else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void submit(Activity activity, TextView status, TextView transcript,
                               String prompt, String label) {
        new AlertDialog.Builder(activity).setTitle("Send creative brief?")
                .setMessage("Only the creative brief and selected format will be sent to JARVIS AI. No saved conversation, notes or contacts are attached. This studio produces text plans, not rendered media files.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate plan", (d, w) -> {
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
            item.put("project_name", type);
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
                    : item.optString("project_name", item.optString("type", "Creative draft"))
                    + " • " + item.optString("status", "Draft");
        }
        new AlertDialog.Builder(activity).setTitle("Creative Project Organizer")
                .setItems(labels, (dialog, which) -> {
                    org.json.JSONObject item = projects.optJSONObject(which);
                    if (item != null) {
                        String date = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",
                                java.util.Locale.getDefault()).format(
                                new java.util.Date(item.optLong("created_at", 0L)));
                        new AlertDialog.Builder(activity).setTitle(item.optString("project_name", item.optString("type", "Creative draft")))
                                .setMessage(item.optString("status", "Draft") + " • NOT A RENDERED MEDIA FILE\\nSaved: " + date + "\\n\\n"
                                        + item.optString("content", ""))
                                .setPositiveButton("Mark reviewed", (d, w) -> {
                                    try {
                                        item.put("status", "Reviewed");
                                        prefs.edit().putString(PROJECTS_KEY, projects.toString()).apply();
                                        Toast.makeText(activity, "Creative draft marked reviewed.", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(activity, "Draft status could not be updated.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .setNeutralButton("Rename", (d, w) -> {
                                    EditText rename = new EditText(activity);
                                    rename.setSingleLine(true);
                                    rename.setText(item.optString("project_name", item.optString("type", "")));
                                    new AlertDialog.Builder(activity).setTitle("Rename creative project")
                                            .setView(rename)
                                            .setNegativeButton("Cancel", null)
                                            .setPositiveButton("Save", (rd, rw) -> {
                                                String value = rename.getText().toString().trim();
                                                if (value.isEmpty() || value.length() > 60) {
                                                    Toast.makeText(activity, "Use a name between 1 and 60 characters.", Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                                try {
                                                    item.put("project_name", value);
                                                    prefs.edit().putString(PROJECTS_KEY, projects.toString()).apply();
                                                    Toast.makeText(activity, "Project renamed.", Toast.LENGTH_SHORT).show();
                                                } catch (Exception e) {
                                                    Toast.makeText(activity, "Project could not be renamed.", Toast.LENGTH_LONG).show();
                                                }
                                            }).show();
                                })
                                .setNegativeButton("Delete", (d, w) ->
                                        new AlertDialog.Builder(activity).setTitle("Delete this creative draft?")
                                                .setMessage("This removes only the selected draft from this device.")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton("Delete", (cd, cw) -> {
                                                    projects.remove(which);
                                                    prefs.edit().putString(PROJECTS_KEY, projects.toString()).apply();
                                                    Toast.makeText(activity, "Creative draft deleted.", Toast.LENGTH_SHORT).show();
                                                }).show())
                                .show();
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
