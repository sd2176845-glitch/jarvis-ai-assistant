package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Locale;

/**
 * M14 owner-triggered diagnostics.
 * Checks app capability/permission state only; it does not read private user content,
 * collect logs, change settings, or transmit data without an explicit export action.
 */
final class DiagnosticsStudio {
    private static final String HISTORY_KEY = "m14_diagnostic_history_v1";
    private static final int MAX_HISTORY = 10;

    private DiagnosticsStudio() { }

    static void show(Activity activity, SharedPreferences preferences, boolean ttsReady) {
        if (!OwnerConsentStudio.isAllowed(preferences, OwnerConsentStudio.LOCAL_DIAGNOSTICS)) {
            Toast.makeText(activity, "Local diagnostics consent is required. Review the owner consent choices first.", Toast.LENGTH_LONG).show();
            OwnerConsentStudio.show(activity, preferences);
            return;
        }
        String[] actions = {
                "Run app health check",
                "Check JARVIS backend health",
                "Export sanitized report",
                "View saved diagnostic history",
                "Clear diagnostic history",
                "Configuration recovery & rollback"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Diagnostics Studio")
                .setMessage("Owner-started checks only. Reports exclude contacts, messages, conversation text, call data and raw logs.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) showReport(activity, preferences, ttsReady, false);
                    else if (which == 1) checkBackendHealth(activity);
                    else if (which == 2) showReport(activity, preferences, ttsReady, true);
                    else if (which == 3) showHistory(activity, preferences, ttsReady);
                    else if (which == 4) clearHistory(activity, preferences);
                    else DiagnosticsRecoveryStudio.show(activity, preferences);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static void checkBackendHealth(Activity activity) {
        Toast.makeText(activity, "Checking JARVIS backend health…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            HttpURLConnection connection = null;
            String report;
            try {
                connection = (HttpURLConnection) new java.net.URL(
                        "https://jarvis-ai-backend-bq4m.onrender.com/health").openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                connection.setRequestProperty("Accept", "application/json");
                int code = connection.getResponseCode();
                if (code != 200) {
                    report = "BACKEND STATUS: HTTP " + code + "\\nAI availability was not confirmed.";
                } else {
                    java.io.InputStream input = connection.getInputStream();
                    java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = input.read(buffer)) != -1 && bytes.size() < 16000) {
                        bytes.write(buffer, 0, count);
                    }
                    input.close();
                    JSONObject root = new JSONObject(bytes.toString("UTF-8"));
                    boolean ok = root.optBoolean("ok", false);
                    boolean configured = root.optBoolean("aiConfigured", false);
                    report = "JARVIS BACKEND HEALTH\\n\\n"
                            + "Service: " + root.optString("service", "Not reported") + "\\n"
                            + "HTTP: " + code + "\\n"
                            + "Service response: " + (ok ? "OK" : "Not confirmed") + "\\n"
                            + "Provider: " + root.optString("provider", "Not reported") + "\\n"
                            + "Model: " + root.optString("model", "Not reported") + "\\n"
                            + "AI configured: " + (configured ? "Yes" : "No / not reported")
                            + "\\n\\nThis is a point-in-time public health endpoint check. It does not test a full AI response, account access or private data. No API key or user content was sent.";
                }
            } catch (Exception e) {
                report = "BACKEND STATUS: UNAVAILABLE\\nCould not confirm a response. Check internet access or try again later. No user content or credentials were sent.";
            } finally {
                if (connection != null) connection.disconnect();
            }
            final String result = report;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                new AlertDialog.Builder(activity).setTitle("JARVIS • Backend health")
                        .setMessage(result).setPositiveButton("Close", null).show();
            });
        }).start();
    }

    private static void showReport(Activity activity, SharedPreferences preferences,
                                   boolean ttsReady, boolean exportImmediately) {
        HealthReport result = buildReport(activity, ttsReady);
        if (exportImmediately) {
            exportReport(activity, result.report);
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • App health report")
                .setMessage(result.report)
                .setPositiveButton("Save snapshot", (d, w) -> {
                    saveSnapshot(preferences, result);
                    Toast.makeText(activity, "Diagnostic summary saved on this device.", Toast.LENGTH_SHORT).show();
                    show(activity, preferences, ttsReady);
                })
                .setNeutralButton("Export report", (d, w) -> exportReport(activity, result.report))
                .setNegativeButton("Close", null)
                .show();
    }

    private static HealthReport buildReport(Context context, boolean ttsReady) {
        StringBuilder out = new StringBuilder("JARVIS APP HEALTH CHECK\n");
        out.append("Generated: ").append(new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date())).append("\n");
        out.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL)
                .append(" • Android ").append(Build.VERSION.RELEASE)
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n");

        int passed = 0;
        int total = 4;
        boolean speech = SpeechRecognizer.isRecognitionAvailable(context);
        out.append("CORE CHECKS\n");
        out.append(mark(speech)).append(" Speech recognition service ")
                .append(speech ? "available" : "unavailable").append("\n");
        if (speech) passed++;

        out.append(mark(ttsReady)).append(" Text-to-Speech engine ")
                .append(ttsReady ? "initialized" : "not initialized").append("\n");
        if (ttsReady) passed++;

        boolean internetDeclared = context.getPackageManager().checkPermission(
                Manifest.permission.INTERNET, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
        out.append(mark(internetDeclared)).append(" Internet permission ")
                .append(internetDeclared ? "declared" : "not declared").append("\n");
        if (internetDeclared) passed++;

        boolean packageReadable = false;
        String version = "unavailable";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName == null ? "unknown" : info.versionName;
            packageReadable = true;
        } catch (Exception ignored) { }
        out.append(mark(packageReadable)).append(" App package metadata ")
                .append(packageReadable ? "readable" : "unavailable")
                .append(" • version ").append(version).append("\n");
        if (packageReadable) passed++;

        out.append("\nOPTIONAL PERMISSION STATUS\n");
        appendPermission(out, context, Manifest.permission.RECORD_AUDIO, "Microphone");
        appendPermission(out, context, Manifest.permission.READ_CONTACTS, "Contacts");
        appendPermission(out, context, Manifest.permission.CALL_PHONE, "Phone calls");
        if (Build.VERSION.SDK_INT >= 33) {
            appendPermission(out, context, Manifest.permission.POST_NOTIFICATIONS, "Notifications");
        } else {
            out.append("— Notifications: runtime permission not required on this Android version\n");
        }

        out.append("\nRESULT: ").append(passed).append("/").append(total).append(" core checks passed.\n");
        out.append("This is an app capability check, not a malware scan or full device diagnosis. "
                + "No private content or raw logs were accessed. Nothing was uploaded.");
        return new HealthReport(out.toString(), passed, total);
    }

    private static void appendPermission(StringBuilder out, Context context,
                                         String permission, String label) {
        boolean granted = Build.VERSION.SDK_INT < 23
                || context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        out.append(granted ? "✓ " : "• ").append(label).append(": ")
                .append(granted ? "granted" : "not granted").append("\n");
    }

    private static String mark(boolean passed) {
        return passed ? "✓" : "!";
    }

    private static void saveSnapshot(SharedPreferences preferences, HealthReport report) {
        JSONArray history = readHistory(preferences);
        if (history == null) history = new JSONArray();
        JSONObject item = new JSONObject();
        try {
            item.put("timestamp", System.currentTimeMillis());
            item.put("passed", report.passed);
            item.put("total", report.total);
            history.put(item);
            while (history.length() > MAX_HISTORY) history.remove(0);
            preferences.edit().putString(HISTORY_KEY, history.toString()).apply();
        } catch (JSONException ignored) { }
    }

    private static JSONArray readHistory(SharedPreferences preferences) {
        try {
            return new JSONArray(preferences.getString(HISTORY_KEY, "[]"));
        } catch (JSONException e) {
            return null;
        }
    }

    private static void showHistory(Activity activity, SharedPreferences preferences, boolean ttsReady) {
        JSONArray history = readHistory(preferences);
        if (history == null) {
            new AlertDialog.Builder(activity).setTitle("Diagnostic history")
                    .setMessage("History data could not be read. Nothing has been changed.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        String[] rows = new String[history.length()];
        for (int i = 0; i < history.length(); i++) {
            JSONObject item = history.optJSONObject(i);
            if (item == null) {
                rows[i] = "Unavailable record";
                continue;
            }
            long timestamp = item.optLong("timestamp", 0L);
            String date = timestamp > 0L
                    ? new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(new Date(timestamp))
                    : "Date unavailable";
            rows[i] = date + " • " + item.optInt("passed", 0)
                    + "/" + item.optInt("total", 0) + " checks";
        }
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Diagnostic history")
                .setMessage(rows.length == 0
                        ? "No saved diagnostic summaries. Run a health check and choose Save snapshot."
                        : "Only timestamps and pass counts are stored; no report text or personal content.")
                .setItems(rows, (d, which) -> {
                    JSONObject item = history.optJSONObject(which);
                    if (item != null) {
                        Toast.makeText(activity, item.optInt("passed", 0) + "/"
                                + item.optInt("total", 0) + " checks passed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Clear history", (d, w) -> clearHistory(activity, preferences))
                .setNegativeButton("Back", (d, w) -> show(activity, preferences, ttsReady))
                .show();
    }

    private static void clearHistory(Activity activity, SharedPreferences preferences) {
        new AlertDialog.Builder(activity)
                .setTitle("Clear diagnostic history?")
                .setMessage("This deletes only the saved diagnostic summaries from this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (d, w) -> {
                    preferences.edit().remove(HISTORY_KEY).apply();
                    Toast.makeText(activity, "Diagnostic history cleared.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private static void exportReport(Activity activity, String report) {
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, "JARVIS Sanitized App Health Report");
            share.putExtra(Intent.EXTRA_TEXT, report);
            activity.startActivity(Intent.createChooser(share, "Review report and choose export destination"));
        } catch (Exception e) {
            Toast.makeText(activity, "Report export is not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private static final class HealthReport {
        final String report;
        final int passed;
        final int total;

        HealthReport(String report, int passed, int total) {
            this.report = report;
            this.passed = passed;
            this.total = total;
        }
    }
}
