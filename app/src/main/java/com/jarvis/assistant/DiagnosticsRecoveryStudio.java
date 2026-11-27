package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONObject;


/**
 * M14 controlled recovery for a narrow allowlist of JARVIS-owned presentation/voice preferences.
 * It never changes Android system settings, permissions, credentials, contacts, notes, tasks or memory.
 */
public final class DiagnosticsRecoveryStudio {
    private static final String STORE = "jarvis_m14_recovery_v1";
    private static final String CHECKPOINT = "checkpoint";
    private static final String ROLLBACK = "rollback";

    private DiagnosticsRecoveryStudio() { }

    public static void show(Activity activity, SharedPreferences preferences) {
        String[] actions = {
                "Create configuration checkpoint",
                "Review saved checkpoint",
                "Restore saved checkpoint",
                "Rollback last restore"
        };
        new AlertDialog.Builder(activity).setTitle("M14 • Controlled recovery")
                .setMessage("Only selected JARVIS voice and display preferences are included. Android settings, permissions, account data, contacts, notes, tasks and memory are never changed.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) createCheckpoint(activity, preferences);
                    else if (which == 1) reviewCheckpoint(activity);
                    else if (which == 2) confirmRestore(activity, preferences);
                    else confirmRollback(activity, preferences);
                })
                .setNegativeButton("Close", null).show();
    }

    private static SharedPreferences store(Activity activity) {
        return activity.getSharedPreferences(STORE, Activity.MODE_PRIVATE);
    }

    private static JSONObject snapshot(SharedPreferences source) throws Exception {
        JSONObject values = new JSONObject();
        for (String key : DiagnosticsRecoveryRules.allowedKeys()) {
            if (!source.contains(key)) continue;
            Object value = source.getAll().get(key);
            if (DiagnosticsRecoveryRules.isSupportedValue(value)) values.put(key, value);
        }
        JSONObject result = new JSONObject();
        result.put("version", 1);
        result.put("created_at", System.currentTimeMillis());
        result.put("values", values);
        return result;
    }

    private static void createCheckpoint(Activity activity, SharedPreferences preferences) {
        try {
            JSONObject snapshot = snapshot(preferences);
            if (!store(activity).edit().putString(CHECKPOINT, snapshot.toString()).commit()) {
                throw new IllegalStateException("Checkpoint could not be saved.");
            }
            Toast.makeText(activity, "Configuration checkpoint saved on this device.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Checkpoint failed: " + safeMessage(e), Toast.LENGTH_LONG).show();
        }
    }

    private static JSONObject read(String key, Activity activity) {
        try {
            String raw = store(activity).getString(key, "");
            if (raw.isEmpty()) return null;
            JSONObject root = new JSONObject(raw);
            JSONObject values = root.optJSONObject("values");
            if (root.optInt("version", -1) != 1 || values == null) return null;
            return root;
        } catch (Exception e) {
            return null;
        }
    }

    private static void reviewCheckpoint(Activity activity) {
        JSONObject saved = read(CHECKPOINT, activity);
        if (saved == null) {
            new AlertDialog.Builder(activity).setTitle("No valid checkpoint")
                    .setMessage("Create a checkpoint first. A checkpoint stores only the allowlisted JARVIS voice and display preferences.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        JSONObject values = saved.optJSONObject("values");
        StringBuilder report = new StringBuilder("Saved: ")
                .append(new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                        .format(new java.util.Date(saved.optLong("created_at", 0))))
                .append("\n\nIncluded settings:");
        org.json.JSONArray names = values.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                report.append("\n• ").append(names.optString(i));
            }
        }
        report.append("\n\nNo private content or Android system settings are included.");
        new AlertDialog.Builder(activity).setTitle("Checkpoint review")
                .setMessage(report.toString()).setPositiveButton("Close", null).show();
    }

    private static void confirmRestore(Activity activity, SharedPreferences preferences) {
        JSONObject checkpoint = read(CHECKPOINT, activity);
        if (checkpoint == null) {
            Toast.makeText(activity, "No valid checkpoint available.", Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(activity).setTitle("Restore JARVIS preferences?")
                .setMessage("The current allowlisted preferences will first be saved as a rollback point. Then only the eight approved voice/display preference keys will be restored. No permissions or private content will change.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Restore", (d, w) -> {
                    try {
                        JSONObject current = snapshot(preferences);
                        SharedPreferences.Editor storeEditor = store(activity).edit()
                                .putString(ROLLBACK, current.toString());
                        if (!storeEditor.commit()) throw new IllegalStateException("Rollback point could not be saved.");
                        apply(checkpoint, preferences);
                        Toast.makeText(activity, "Approved preferences restored. Rollback point saved.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(activity, "Restore failed: " + safeMessage(e), Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void confirmRollback(Activity activity, SharedPreferences preferences) {
        JSONObject rollback = read(ROLLBACK, activity);
        if (rollback == null) {
            Toast.makeText(activity, "No rollback point is available.", Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(activity).setTitle("Rollback last restore?")
                .setMessage("This restores the allowlisted voice/display preferences captured immediately before the last restore.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Rollback", (d, w) -> {
                    try {
                        apply(rollback, preferences);
                        store(activity).edit().remove(ROLLBACK).commit();
                        Toast.makeText(activity, "Last restore rolled back.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(activity, "Rollback failed: " + safeMessage(e), Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void apply(JSONObject checkpoint, SharedPreferences preferences) throws Exception {
        JSONObject values = checkpoint.getJSONObject("values");
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : DiagnosticsRecoveryRules.allowedKeys()) editor.remove(key);
        org.json.JSONArray names = values.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                String key = names.getString(i);
                if (!DiagnosticsRecoveryRules.isAllowedKey(key)) continue;
                Object value = values.get(key);
                if (!DiagnosticsRecoveryRules.isSupportedValue(value)) continue;
                if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Integer) editor.putInt(key, (Integer) value);
                else if (value instanceof Long) editor.putLong(key, (Long) value);
                else if (value instanceof Float) editor.putFloat(key, (Float) value);
                else if (value instanceof Double) editor.putFloat(key, ((Double) value).floatValue());
                else if (value instanceof String) editor.putString(key, (String) value);
            }
        }
        if (!editor.commit()) throw new IllegalStateException("Preference restore could not be committed.");
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) return "Check storage and retry.";
        return message.length() > 100 ? message.substring(0, 100) : message;
    }
}
