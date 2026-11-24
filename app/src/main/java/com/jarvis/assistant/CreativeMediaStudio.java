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
                "Presentation Builder • slides / speaker notes"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Creative Media Studio")
                .setItems(options, (d, which) -> {
                    if (which == 0) videoScript(activity, status, transcript);
                    else if (which == 1) storyboard(activity, status, transcript);
                    else presentation(activity, status, transcript);
                }).setNegativeButton("Close", null).show();
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
                                .setMessage(reply).setPositiveButton("Done", null).show();
                    });
                }).show();
    }
}
