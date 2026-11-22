package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Owner-invoked software planning and code-assistance tools; no code is executed or auto-written. */
public final class SoftwareDevelopmentStudio {
    private SoftwareDevelopmentStudio() {}

    private interface ChoiceAction { void choose(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Project Architect • requirements and structure",
                "Bug & Error Explainer • diagnose pasted error",
                "Code Quality Review • review pasted code"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Software Development Studio")
                .setItems(options, (d, which) -> {
                    if (which == 0) projectArchitect(activity, status, transcript);
                    else if (which == 1) bugExplainer(activity, status, transcript);
                    else codeReview(activity, status, transcript);
                }).setNegativeButton("Close", null).show();
    }

    private static void projectArchitect(Activity activity, TextView status, TextView transcript) {
        String[] platforms = {"Android app", "Website", "Backend API", "Desktop tool", "Cross-platform app"};
        new AlertDialog.Builder(activity).setTitle("Choose project platform")
                .setItems(platforms, (d, which) ->
                        askText(activity, "Project idea",
                                "Describe the app/tool you want to plan (do not include passwords or API keys)",
                                idea -> submit(activity, status, transcript,
                                        "Act as a careful software architect. Create a beginner-readable project blueprint for this "
                                                + platforms[which] + " idea: " + idea + ". Include: clarified requirements and assumptions; "
                                                + "core user stories; suggested architecture; modular folder/file tree; data model; API boundaries if needed; "
                                                + "privacy/security considerations; accessibility; error/offline handling; and a practical build/test sequence. "
                                                + "Separate required features from optional future improvements. Do not invent credentials or expose secrets. "
                                                + "Do not design covert surveillance, backdoors, credential theft, malware, unauthorized access or harmful functionality. "
                                                + "Do not claim files were created or code was executed; return a reviewable plan only.",
                                        "PROJECT BLUEPRINT READY"))))
                .setNegativeButton("Cancel", null).show();
    }

    private static void bugExplainer(Activity activity, TextView status, TextView transcript) {
        askText(activity, "Bug & Error Explainer",
                "Paste the error message and, if useful, a small relevant code excerpt. Remove secrets first.",
                details -> submit(activity, status, transcript,
                        "Act as a safe debugging assistant. Analyze this owner-provided error report and optional code excerpt: "
                                + details + ". Return: what the error means; the most likely causes ranked with evidence; "
                                + "the smallest suggested correction; exact file/line guidance only if the input supports it; "
                                + "a verification checklist; and what information is still missing. Never claim you ran the code or inspected files. "
                                + "Do not help debug malware, credential theft, covert surveillance, backdoors, unauthorized access or harmful code; "
                                + "redirect to safe software behavior. Warn if the pasted content appears to contain secrets and do not repeat them.",
                        "BUG ANALYSIS READY"));
    }

    private static void codeReview(Activity activity, TextView status, TextView transcript) {
        String[] languages = {"Java", "Kotlin", "Python", "JavaScript / TypeScript", "Other / not sure"};
        new AlertDialog.Builder(activity).setTitle("Choose code language")
                .setItems(languages, (d, which) ->
                        askText(activity, "Code Quality Review",
                                "Paste a small code excerpt (maximum 5,000 characters). Remove passwords, keys and private data.",
                                code -> submit(activity, status, transcript,
                                        "Perform a careful review of this owner-provided " + languages[which] + " code excerpt: "
                                                + code + ". Return findings grouped by severity: correctness/bugs, readability/maintainability, "
                                                + "performance, accessibility/usability where relevant, and defensive security/privacy. "
                                                + "For each finding cite only evidence visible in the excerpt and suggest a minimal safe fix. "
                                                + "If no issue is evident, say so without claiming the whole project is secure. "
                                                + "Do not execute the code, access external files or invent test results. "
                                                + "Do not review or improve malware, credential theft, covert surveillance, backdoors, evasion or unauthorized access; "
                                                + "redirect to benign examples.",
                                        "CODE REVIEW READY"))))
                .setNegativeButton("Cancel", null).show();
    }

    private interface TextAction { void submit(String value); }

    private static void askText(Activity activity, String title, String hint, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(3);
        input.setMaxLines(8);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setView(layout)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Enter a description or code excerpt first.", Toast.LENGTH_SHORT).show();
                    } else if (value.length() > 5000) {
                        Toast.makeText(activity, "Keep the input under 5,000 characters.", Toast.LENGTH_LONG).show();
                    } else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void submit(Activity activity, TextView status, TextView transcript,
                               String prompt, String label) {
        new AlertDialog.Builder(activity).setTitle("Send to JARVIS AI?")
                .setMessage("Only the project description, error text or code excerpt you entered will be sent. Remove passwords, API keys, tokens and personal data first. No saved conversation, notes or contacts are attached.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send", (d, w) -> {
                    status.setText("SOFTWARE DEVELOPMENT STUDIO • PROCESSING");
                    transcript.setText("Reviewing your development request…");
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
