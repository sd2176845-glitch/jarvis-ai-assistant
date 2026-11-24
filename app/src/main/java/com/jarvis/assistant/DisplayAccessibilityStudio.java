package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.TextView;

/** Owner-controlled, app-local readability profiles. Does not change Android system settings. */
public final class DisplayAccessibilityStudio {
    private static final String PREF_PROFILE = "display_accessibility_profile_v1";
    private DisplayAccessibilityStudio() {}

    public static void apply(Activity activity, TextView status, TextView transcript) {
        if (activity == null || status == null || transcript == null) return;
        SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
        int profile = DisplayAccessibilityRules.normalizeProfile(prefs.getInt(PREF_PROFILE, 0));
        status.setTextSize(DisplayAccessibilityRules.statusTextSize(profile));
        transcript.setTextSize(DisplayAccessibilityRules.transcriptTextSize(profile));
        if (DisplayAccessibilityRules.isHighContrast(profile)) {
            status.setTextColor(Color.WHITE);
            transcript.setTextColor(Color.WHITE);
        } else {
            status.setTextColor(Color.rgb(255, 70, 82));
            transcript.setTextColor(Color.rgb(190, 215, 230));
        }
        status.setContentDescription("JARVIS status: " + status.getText());
        transcript.setContentDescription("JARVIS response: " + transcript.getText());
    }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Standard text",
                "Large text",
                "Extra-large text",
                "High contrast",
                "Reset to standard"
        };
        new AlertDialog.Builder(activity)
                .setTitle("Display & Accessibility")
                .setMessage("Choose an app-only readability profile. Android system settings are not changed.")
                .setItems(options, (dialog, which) -> {
                    int profile = which == 4 ? 0 : which;
                    activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE)
                            .edit().putInt(PREF_PROFILE, profile).apply();
                    apply(activity, status, transcript);
                    if (status != null) status.setText("DISPLAY PROFILE • " + options[which].toUpperCase(java.util.Locale.ROOT));
                })
                .setNegativeButton("Close", null)
                .show();
    }
}
