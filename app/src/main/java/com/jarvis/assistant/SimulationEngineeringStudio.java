package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** M18 text-first virtual learning/design tools. No physical actuation or real-world experiments. */
public final class SimulationEngineeringStudio {
    private SimulationEngineeringStudio() {}

    private interface TextAction { void submit(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Digital World Simulator • conceptual scenarios",
                "Virtual Experiment Lab • safe science concepts",
                "3D Invention Studio • product design brief",
                "Virtual Experiment Notebook • saved studies"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Simulation & Engineering")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) chooseWorld(activity, status, transcript);
                    else if (which == 1) chooseExperiment(activity, status, transcript);
                    else if (which == 2) chooseInvention(activity, status, transcript);
                    else showNotebook(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void chooseWorld(Activity activity, TextView status, TextView transcript) {
        String[] scenarios = {"Ecosystem balance", "Planetary motion concepts",
                "Water cycle and climate", "Sustainable community resources"};
        new AlertDialog.Builder(activity).setTitle("Choose a virtual world")
                .setItems(scenarios, (d, which) -> ask(activity, "Digital World Simulator",
                        "Describe what you want to understand in this scenario (maximum 600 characters).",
                        600, brief -> generate(activity, status, transcript, "DIGITAL WORLD SIMULATION",
                                SimulationEngineeringRules.digitalWorldPrompt(scenarios[which], brief))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void chooseExperiment(Activity activity, TextView status, TextView transcript) {
        String[] subjects = {"Physics concepts", "Chemistry concepts", "Biology concepts", "Earth and space science"};
        new AlertDialog.Builder(activity).setTitle("Choose a learning subject")
                .setItems(subjects, (d, which) -> ask(activity, "Virtual Experiment Lab",
                        "Enter a concept or question to explore (maximum 600 characters).",
                        600, brief -> generate(activity, status, transcript, "VIRTUAL EXPERIMENT LAB",
                                SimulationEngineeringRules.experimentPrompt(subjects[which], brief))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void chooseInvention(Activity activity, TextView status, TextView transcript) {
        String[] categories = {"School / desk organizer", "Everyday accessibility aid",
                "Sustainable household product", "Educational display model", "Abstract sculpture"};
        new AlertDialog.Builder(activity).setTitle("Choose an invention category")
                .setItems(categories, (d, which) -> ask(activity, "3D Invention Studio",
                        "Describe the everyday problem or creative idea (maximum 600 characters).",
                        600, brief -> generate(activity, status, transcript, "3D INVENTION DESIGN BRIEF",
                                SimulationEngineeringRules.inventionPrompt(categories[which], brief))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void ask(Activity activity, String title, String hint, int maxLength, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(2);
        input.setMaxLines(5);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setView(layout)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) Toast.makeText(activity, "Enter a short topic first.", Toast.LENGTH_SHORT).show();
                    else if (value.length() > maxLength) Toast.makeText(activity,
                            "Please keep the topic under " + maxLength + " characters.", Toast.LENGTH_LONG).show();
                    else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void generate(Activity activity, TextView status, TextView transcript,
                                 String title, String prompt) {
        new AlertDialog.Builder(activity).setTitle("Send learning/design brief?")
                .setMessage("Only the topic you entered and selected category will be sent to JARVIS AI. Do not include personal details or secrets. These tools create text-only virtual explanations/design briefs; they do not run real experiments, create 3D files, control robots or operate physical equipment.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate", (d, w) -> {
                    status.setText("SIMULATION & ENGINEERING • PROCESSING");
                    transcript.setText("Preparing your virtual study…");
                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        status.setText(title + " • READY");
                        transcript.setText(reply);
                        new AlertDialog.Builder(activity).setTitle("JARVIS • " + title)
                                .setMessage(reply)
                                .setPositiveButton("Save to notebook", (resultDialog, resultWhich) ->
                                        saveNotebookEntry(activity, title, reply))
                                .setNegativeButton("Done", null).show();
                    });
                }).show();
    }
}
