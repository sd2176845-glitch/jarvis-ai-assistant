package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;

/**
 * M01 owner privacy studio. Read-only permission/data review plus an explicitly
 * owner-triggered export containing aggregate counts only. No permissions are requested.
 */
public final class OwnerPrivacyStudio {
    private OwnerPrivacyStudio() { }

    public static void show(Activity activity, SharedPreferences prefs) {
        String[] options = {
                "Permission & access review • read-only",
                "Local data inventory • counts only",
                "Export sanitized privacy summary"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Owner Privacy Studio")
                .setMessage("Review permissions and locally stored data. Nothing is collected or changed by opening this screen.")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showPermissionReview(activity);
                    else if (which == 1) showInventory(activity, prefs);
                    else exportSummary(activity, prefs);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static String permissionSummary(Activity activity) {
        StringBuilder report = new StringBuilder("PERMISSION & ACCESS REVIEW\n\n");
        report.append("Microphone: ").append(state(activity, Manifest.permission.RECORD_AUDIO)).append("\n");
        report.append("Contacts: ").append(state(activity, Manifest.permission.READ_CONTACTS)).append("\n");
        report.append("Phone state: ").append(state(activity, Manifest.permission.READ_PHONE_STATE)).append("\n");
        report.append("Call phone: ").append(state(activity, Manifest.permission.CALL_PHONE)).append("\n");
        report.append("Receive SMS: ").append(state(activity, Manifest.permission.RECEIVE_SMS)).append("\n");
        if (Build.VERSION.SDK_INT >= 33) {
            report.append("Notifications: ").append(state(activity, Manifest.permission.POST_NOTIFICATIONS)).append("\n");
        } else {
            report.append("Notifications: controlled by Android version / app notification settings\n");
        }
        boolean overlay = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(activity);
        report.append("Display over other apps: ").append(overlay ? "Allowed" : "Not allowed").append("\n");
        String enabled = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean accessibility = enabled != null && enabled.contains(activity.getPackageName());
        report.append("JARVIS accessibility service: ").append(accessibility ? "Enabled" : "Not enabled");
        report.append("\n\nThis is a local status summary, not a security certification. "
                + "JARVIS does not request or change permissions from this screen.");
        return report.toString();
    }

    private static String state(Activity activity, String permission) {
        return OwnerPrivacyRules.permissionState(
                activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    private static void showPermissionReview(Activity activity) {
        String report = permissionSummary(activity);
        new AlertDialog.Builder(activity).setTitle("Permission review")
                .setMessage(report)
                .setPositiveButton("Android app settings", (d, w) -> openAppSettings(activity))
                .setNeutralButton("Accessibility settings", (d, w) -> openAccessibilitySettings(activity))
                .setNegativeButton("Close", null).show();
    }

    private static String inventorySummary(Activity activity, SharedPreferences prefs) {
        SharedPreferences memory = activity.getSharedPreferences("jarvis_ai_memory", Activity.MODE_PRIVATE);
        String notes = prefs.getString("jarvis_notes", "");
        if (notes == null || notes.isEmpty()) notes = prefs.getString("notes", "");
        String conversation = memory.getString("conversation", "");
        File archive = new File(activity.getFilesDir(), "jarvis_memory_legacy.enc");
        return OwnerPrivacyRules.buildInventorySummary(
                notes == null ? 0 : notes.length(),
                conversation == null ? 0 : conversation.length(),
                count(prefs, "structured_tasks_v1"),
                count(prefs, "manual_workflows_v1"),
                count(prefs, "memory_timeline_v1"),
                count(prefs, "memory_graph_v1"),
                count(prefs, "idea_versions_v1"),
                count(prefs, "vehicle_service_log_v1"),
                archive.isFile());
    }

    private static int count(SharedPreferences prefs, String key) {
        return OwnerPrivacyRules.jsonArrayCount(prefs.getString(key, "[]"));
    }

    private static void showInventory(Activity activity, SharedPreferences prefs) {
        String report = inventorySummary(activity, prefs);
        TextView content = new TextView(activity);
        content.setText(report);
        content.setTextSize(15);
        content.setPadding(28, 20, 28, 20);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(content);
        new AlertDialog.Builder(activity).setTitle("Local data inventory")
                .setView(scroll)
                .setPositiveButton("Export counts", (d, w) -> share(activity,
                        OwnerPrivacyRules.buildSanitizedExport(permissionSummary(activity), report)))
                .setNegativeButton("Close", null).show();
    }

    private static void exportSummary(Activity activity, SharedPreferences prefs) {
        String report = OwnerPrivacyRules.buildSanitizedExport(
                permissionSummary(activity), inventorySummary(activity, prefs));
        new AlertDialog.Builder(activity).setTitle("Export sanitized summary?")
                .setMessage("The export contains permission states and aggregate counts only. It does not include notes, conversation text, contacts, messages, account details or identifiers. Android will let you choose a sharing destination.")
                .setPositiveButton("Continue", (d, w) -> share(activity, report))
                .setNegativeButton("Cancel", null).show();
    }

    private static void share(Activity activity, String report) {
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, "JARVIS Owner Privacy Summary");
            send.putExtra(Intent.EXTRA_TEXT, report);
            activity.startActivity(Intent.createChooser(send, "Share privacy summary"));
        } catch (Exception e) {
            Toast.makeText(activity, "Could not open Android sharing.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openAppSettings(Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.parse("package:" + activity.getPackageName())));
        } catch (Exception e) {
            Toast.makeText(activity, "Open Android Settings > Apps > JARVIS.", Toast.LENGTH_LONG).show();
        }
    }

    private static void openAccessibilitySettings(Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } catch (Exception e) {
            Toast.makeText(activity, "Open Android Settings > Accessibility.", Toast.LENGTH_LONG).show();
        }
    }
}
