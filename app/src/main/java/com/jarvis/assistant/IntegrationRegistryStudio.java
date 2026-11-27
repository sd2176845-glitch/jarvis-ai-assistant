package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.TextView;

/** Local allow-listed integration registry. It controls availability of existing JARVIS entry points only. */
public final class IntegrationRegistryStudio {
    public static final String SMART_HOME = "integration_smart_home_enabled";
    public static final String CONNECTED_SYSTEMS = "integration_connected_systems_enabled";
    private static final String PREFS = "jarvis_preferences";

    private IntegrationRegistryStudio() {}

    public static boolean isEnabled(Activity activity, String key) {
        return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE).getBoolean(key, true);
    }

    public static void show(Activity activity, TextView status) {
        String[] names = {"Smart Home Studio", "Connected Systems"};
        String[] keys = {SMART_HOME, CONNECTED_SYSTEMS};
        String[] scopes = {
                "Owner-entered local device labels, setup/routine guidance and an explicit handoff to Android Bluetooth settings. No scanning, pairing automation or remote device control.",
                "Existing owner-invoked connected-system settings screen. Network presence alone does not authorize access to another device."
        };
        showList(activity, status, names, keys, scopes);
    }

    private static void showList(Activity activity, TextView status, String[] names,
                                 String[] keys, String[] scopes) {
        String[] rows = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            rows[i] = (isEnabled(activity, keys[i]) ? "ON  •  " : "OFF •  ") + names[i];
        }
        new AlertDialog.Builder(activity).setTitle("M16 • Integration Registry")
                .setMessage("Only reviewed built-in JARVIS integrations are listed. These switches enable or disable their existing Command Center entry points; they do not install third-party code or grant Android permissions.")
                .setItems(rows, (dialog, which) -> {
                    boolean enabled = isEnabled(activity, keys[which]);
                    new AlertDialog.Builder(activity).setTitle(names[which])
                            .setMessage("Declared scope:
" + scopes[which]
                                    + "

Current state: " + (enabled ? "Enabled" : "Disabled")
                                    + "

Change availability?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton(enabled ? "Disable" : "Enable", (d, w) -> {
                                activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit()
                                        .putBoolean(keys[which], !enabled).apply();
                                if (status != null) status.setText(names[which].toUpperCase(java.util.Locale.ROOT)
                                        + (enabled ? " DISABLED" : " ENABLED"));
                                showList(activity, status, names, keys, scopes);
                            }).show();
                }).setNegativeButton("Close", null).show();
    }
}
