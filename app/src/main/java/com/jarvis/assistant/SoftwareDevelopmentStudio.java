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
                "Code Quality Review • review pasted code",
                "Software Project Notebook • saved plans",
                "Build & Test Report Viewer • pasted CI summary",
                "Public GitHub Repository Overview • read-only"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Software Development Studio")
                .setItems(options, (d, which) -> {
                    if (which == 0) projectArchitect(activity, status, transcript);
                    else if (which == 1) bugExplainer(activity, status, transcript);
                    else if (which == 2) codeReview(activity, status, transcript);
                    else if (which == 3) showSavedPlans(activity);
                    else if (which == 4) buildReportViewer(activity);
                    else showRepositoryOverview(activity);
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
                                        "PROJECT BLUEPRINT READY")))
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
                                        "CODE REVIEW READY")))
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
                                .setMessage(reply)
                                .setPositiveButton("Save plan", (resultDialog, resultWhich) ->
                                        savePlan(activity, label, reply))
                                .setNegativeButton("Done", null).show();
                    });
                }).show();
    }

    private static void showRepositoryOverview(Activity activity) {
        askText(activity, "Public GitHub Repository Overview",
                "Enter owner/repository or a github.com/owner/repository URL. Public repositories only.",
                value -> {
                    String repo = RepositoryOverviewRules.normalize(value);
                    if (repo == null) {
                        new AlertDialog.Builder(activity).setTitle("Invalid repository")
                                .setMessage("Enter a public GitHub repository as owner/repository. No request was sent.")
                                .setPositiveButton("Close", null).show();
                        return;
                    }
                    new AlertDialog.Builder(activity).setTitle("Read public repository metadata?")
                            .setMessage("JARVIS will make a read-only GET request to GitHub's public repository API for " + repo
                                    + ". No token, private code or account data will be sent. No repository files will be changed.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Continue", (d, w) -> fetchRepositoryOverview(activity, repo))
                            .show();
                });
    }

    private static void fetchRepositoryOverview(Activity activity, String repo) {
        Toast.makeText(activity, "Reading public repository metadata…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            java.net.HttpURLConnection connection = null;
            String result;
            try {
                java.net.URL url = new java.net.URL("https://api.github.com/repos/" + repo);
                connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "JARVIS-Android-ReadOnly");
                int code = connection.getResponseCode();
                if (code == 404) {
                    result = "Repository not found or not publicly accessible. Private repositories are not accessed.";
                } else if (code != 200) {
                    result = "GitHub returned HTTP " + code + ". Try again later.";
                } else {
                    java.io.InputStream input = connection.getInputStream();
                    java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = input.read(buffer)) != -1 && bytes.size() < 50000) bytes.write(buffer, 0, count);
                    input.close();
                    org.json.JSONObject data = new org.json.JSONObject(bytes.toString("UTF-8"));
                    org.json.JSONObject license = data.optJSONObject("license");
                    result = "PUBLIC REPOSITORY OVERVIEW\\n\\n"
                            + "Name: " + data.optString("full_name", repo) + "\\n"
                            + "Description: " + data.optString("description", "Not supplied") + "\\n"
                            + "Default branch: " + data.optString("default_branch", "Not supplied") + "\\n"
                            + "Visibility: " + data.optString("visibility", "public") + "\\n"
                            + "Stars: " + data.optInt("stargazers_count", 0) + "\\n"
                            + "Forks: " + data.optInt("forks_count", 0) + "\\n"
                            + "Last pushed: " + data.optString("pushed_at", "Not supplied") + "\\n"
                            + "License: " + (license == null ? "Not listed" : license.optString("name", "Not listed")) + "\\n"
                            + "Archived: " + data.optBoolean("archived", false) + "\\n\\n"
                            + "Source: " + data.optString("html_url", "GitHub API") + "\\n"
                            + "Read-only metadata only. JARVIS did not inspect private files, clone code, run builds or change the repository.";
                }
            } catch (Exception e) {
                result = "Repository metadata could not be retrieved. Check internet access and try again.";
            } finally {
                if (connection != null) connection.disconnect();
            }
            final String output = result;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                new AlertDialog.Builder(activity).setTitle("JARVIS • Repository Overview")
                        .setMessage(output).setPositiveButton("Close", null).show();
            });
        }).start();
    }

    private static void buildReportViewer(Activity activity) {
        askText(activity, "Build & Test Report Viewer",
                "Paste a short CI/build summary. Remove tokens, secrets and private logs first.",
                report -> new AlertDialog.Builder(activity).setTitle("Build & Test Summary")
                        .setMessage(BuildReportRules.summarize(report))
                        .setPositiveButton("Close", null).show());
    }

    private static final String PLANS_KEY = "software_project_plans_v1";

    private static void savePlan(Activity activity, String type, String content) {
        try {
            android.content.SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
            org.json.JSONArray plans = new org.json.JSONArray(prefs.getString(PLANS_KEY, "[]"));
            if (plans.length() >= 30) {
                Toast.makeText(activity, "Software plan list is full. Delete old plans first.", Toast.LENGTH_LONG).show();
                return;
            }
            org.json.JSONObject item = new org.json.JSONObject();
            item.put("type", type);
            item.put("content", content);
            item.put("created_at", System.currentTimeMillis());
            item.put("status", "Draft");
            plans.put(item);
            prefs.edit().putString(PLANS_KEY, plans.toString()).apply();
            Toast.makeText(activity, "Software plan saved on this device.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Software plan could not be saved.", Toast.LENGTH_LONG).show();
        }
    }

    private static void showSavedPlans(Activity activity) {
        android.content.SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
        org.json.JSONArray plans;
        try {
            plans = new org.json.JSONArray(prefs.getString(PLANS_KEY, "[]"));
        } catch (Exception e) {
            new AlertDialog.Builder(activity).setTitle("Software Project Notebook")
                    .setMessage("Saved plans could not be read. Nothing was changed.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        if (plans.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Software Project Notebook")
                    .setMessage("No saved plans yet. Generate a blueprint or review and choose Save plan.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        String[] labels = new String[plans.length()];
        for (int i = 0; i < plans.length(); i++) {
            org.json.JSONObject item = plans.optJSONObject(i);
            labels[i] = item == null ? "Unavailable plan" : item.optString("type", "Software plan")
                    + " • " + item.optString("status", "Draft");
        }
        new AlertDialog.Builder(activity).setTitle("Software Project Notebook")
                .setItems(labels, (dialog, which) -> {
                    org.json.JSONObject item = plans.optJSONObject(which);
                    if (item != null) {
                        String date = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                .format(new java.util.Date(item.optLong("created_at", 0L)));
                        new AlertDialog.Builder(activity).setTitle(item.optString("type", "Software plan"))
                                .setMessage(item.optString("status", "Draft") + " • NOT EXECUTED\\nSaved: " + date + "\\n\\n"
                                        + item.optString("content", ""))
                                .setPositiveButton("Mark reviewed", (d, w) -> {
                                    try {
                                        item.put("status", "Reviewed");
                                        prefs.edit().putString(PLANS_KEY, plans.toString()).apply();
                                        Toast.makeText(activity, "Software plan marked reviewed.", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(activity, "Plan status could not be updated.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .setNegativeButton("Delete plan", (d, w) ->
                                        new AlertDialog.Builder(activity).setTitle("Delete this software plan?")
                                                .setMessage("This removes only the selected plan from this device.")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton("Delete", (cd, cw) -> {
                                                    plans.remove(which);
                                                    prefs.edit().putString(PLANS_KEY, plans.toString()).apply();
                                                    Toast.makeText(activity, "Software plan deleted.", Toast.LENGTH_SHORT).show();
                                                }).show())
                                .show();
                    }
                })
                .setNegativeButton("Close", null)
                .setNeutralButton("Delete all", (dialog, which) ->
                        new AlertDialog.Builder(activity).setTitle("Delete saved software plans?")
                                .setMessage("This removes only the software plans stored by JARVIS on this device.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (d, w) -> {
                                    prefs.edit().remove(PLANS_KEY).apply();
                                    Toast.makeText(activity, "Software plans cleared.", Toast.LENGTH_SHORT).show();
                                }).show())
                .show();
    }
}
