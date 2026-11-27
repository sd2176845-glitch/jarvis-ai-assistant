package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

/** Owner-controlled, app-local dashboard spacing profiles. */
public final class DashboardProfileStudio {
    private static final String PREF = "dashboard_spacing_profile_v1";
    private DashboardProfileStudio() {}

    public static void apply(Activity activity, View root) {
        if (activity == null || root == null) return;
        SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
        int profile = DashboardProfileRules.normalize(prefs.getInt(PREF, DashboardProfileRules.STANDARD));
        float density = activity.getResources().getDisplayMetrics().density;
        int horizontal = Math.round(DashboardProfileRules.horizontalPaddingDp(profile) * density);
        int vertical = Math.round(DashboardProfileRules.verticalPaddingDp(profile) * density);
        root.setPadding(horizontal, vertical, horizontal, vertical);
    }

    public static void show(Activity activity, View root, TextView status) {
        String[] options = {"Compact", "Standard", "Spacious"};
        SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Activity.MODE_PRIVATE);
        int selected = DashboardProfileRules.normalize(prefs.getInt(PREF, DashboardProfileRules.STANDARD));
        new AlertDialog.Builder(activity).setTitle("JARVIS • Dashboard Profile")
                .setMessage("Choose the spacing of the existing dashboard. This changes only JARVIS layout padding.")
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    int profile = DashboardProfileRules.normalize(which);
                    prefs.edit().putInt(PREF, profile).apply();
                    apply(activity, root);
                    if (status != null) status.setText("DASHBOARD PROFILE • " + DashboardProfileRules.label(profile).toUpperCase(java.util.Locale.ROOT));
                    dialog.dismiss();
                }).setNegativeButton("Close", null).show();
    }
}
