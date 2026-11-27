package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * M17 connectivity tools. Reads only the active network's public capability state;
 * never scans networks, reads identifiers, changes settings, or connects to devices.
 */
public final class ConnectivityIntelligenceStudio {
    private ConnectivityIntelligenceStudio() {}

    private interface TextAction { void submit(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] tools = {
                "AI Operation Mode • Local / Global / Hybrid",
                "Network Health Snapshot • read-only",
                "Live Connectivity Monitor • foreground only",
                "Offline Capability Matrix • local readiness",
                "Connectivity Strategy Planner • local / hybrid / satellite",
                "Connectivity Readiness Advisor • recommendations"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Connectivity Intelligence")
                .setItems(tools, (dialog, which) -> {
                    if (which == 0) showAiModePicker(activity);
                    else if (which == 1) showNetworkSnapshot(activity);
                    else if (which == 2) showLiveConnectivityMonitor(activity);
                    else if (which == 3) showOfflineMatrix(activity);
                    else if (which == 4) showStrategyPlanner(activity, status, transcript);
                    else showReadinessAdvisor(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void showAiModePicker(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(
                "jarvis_preferences", Context.MODE_PRIVATE);
        String current = ConnectivityIntelligenceRules.normalizeAiMode(
                preferences.getString("ai_connectivity_mode", "Hybrid"));
        String[] modes = {
                "Local • built-in phone functions only",
                "Global • cloud AI required",
                "Hybrid • cloud AI with limited local fallback"
        };
        int selected = "Local".equals(current) ? 0 : ("Global".equals(current) ? 1 : 2);
        String disclosure = ConnectivityIntelligenceRules.aiModeDescription(current)
                + "\n\nThis setting controls AI chat, standalone AI-studio prompts and speech-transcript correction routed through JARVIS AI. "
                + "Local mode keeps those requests on this phone. Direct public-data lookups in their own studios remain separate, owner-confirmed internet lookups.";
        new AlertDialog.Builder(activity).setTitle("AI Operation Mode • " + current)
                .setMessage(disclosure)
                .setSingleChoiceItems(modes, selected, (dialog, which) -> {
                    String mode = which == 0 ? "Local" : (which == 1 ? "Global" : "Hybrid");
                    preferences.edit().putString("ai_connectivity_mode", mode).apply();
                    Toast.makeText(activity, "AI mode set to " + mode, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static void showNetworkSnapshot(Activity activity) {
        String report;
        try {
            ConnectivityManager manager = (ConnectivityManager)
                    activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null || Build.VERSION.SDK_INT < 23) {
                report = "Network capability information is unavailable on this Android version.";
            } else {
                Network active = manager.getActiveNetwork();
                if (active == null) {
                    report = "NETWORK: No active network reported.\nINTERNET: Not currently available.";
                } else {
                    NetworkCapabilities caps = manager.getNetworkCapabilities(active);
                    if (caps == null) {
                        report = ConnectivityIntelligenceRules.networkSnapshot(true, false, "", false, false, false);
                    } else {
                        StringBuilder transports = new StringBuilder();
                        appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_WIFI, "Wi-Fi");
                        appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_CELLULAR, "Mobile data");
                        appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_ETHERNET, "Ethernet");
                        appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_VPN, "VPN");
                        if (transports.length() == 0) transports.append("Other / unspecified");

                        boolean internet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                        boolean validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                        report = ConnectivityIntelligenceRules.networkSnapshot(true, true, transports.toString(),
                                internet, validated,
                                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED));
                    }
                }
            }
        } catch (SecurityException e) {
            report = "Android did not allow network-state details to be read.";
        } catch (Exception e) {
            report = "Network status is temporarily unavailable. No settings were changed.";
        }
        new AlertDialog.Builder(activity).setTitle("Network Health Snapshot")
                .setMessage(report).setPositiveButton("Close", null).show();
    }

    private static Runnable activeMonitorCleanup;

    /** Stops the live callback whenever MainActivity is no longer foreground-visible. */
    public static void stopLiveConnectivityMonitor() {
        Runnable cleanup = activeMonitorCleanup;
        activeMonitorCleanup = null;
        if (cleanup != null) {
            try { cleanup.run(); } catch (Exception ignored) { }
        }
    }

    private static void showLiveConnectivityMonitor(Activity activity) {
        ConnectivityManager manager = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null || Build.VERSION.SDK_INT < 23) {
            new AlertDialog.Builder(activity).setTitle("Live Connectivity Monitor")
                    .setMessage("Android does not expose a supported active-network monitor on this device.")
                    .setPositiveButton("Close", null).show();
            return;
        }

        TextView output = new TextView(activity);
        output.setTextSize(15);
        output.setPadding(24, 18, 24, 18);
        output.setText("Starting foreground-only network monitoring…");
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(output, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Live Connectivity Monitor")
                .setMessage("Tracks Android's active default route only while this screen is open. No nearby-device scan or connection attempt is performed.")
                .setView(layout)
                .setPositiveButton("Stop & Close", null)
                .create();

        final boolean[] registered = {false};
        final boolean[] stopped = {false};
        final String[] lastEvent = {"Monitor started"};
        final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            private void refresh(String event) {
                lastEvent[0] = event;
                activity.runOnUiThread(() -> updateLiveMonitorText(activity, manager, output, dialog, lastEvent[0]));
            }

            @Override public void onAvailable(Network network) {
                refresh("Android reported that a network became available");
            }

            @Override public void onLost(Network network) {
                refresh("Android reported that a network was lost");
            }

            @Override public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                refresh("Android reported a network capability change");
            }

            @Override public void onUnavailable() {
                refresh("Android reported that no requested network is available");
            }
        };

        Runnable cleanup = () -> {
            if (stopped[0]) return;
            stopped[0] = true;
            if (registered[0]) {
                try { manager.unregisterNetworkCallback(callback); } catch (Exception ignored) { }
                registered[0] = false;
            }
            lastEvent[0] = "Monitoring stopped because the screen closed or the app left the foreground";
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                activity.runOnUiThread(() -> {
                    if (dialog.isShowing()) {
                        output.setText(ConnectivityIntelligenceRules.liveConnectivityStatus(
                                false, false, "", false, false, false, lastEvent[0]));
                    }
                });
            }
        };

        dialog.setOnDismissListener(ignored -> {
            if (activeMonitorCleanup == cleanup) activeMonitorCleanup = null;
            cleanup.run();
        });
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> dialog.dismiss()));
        dialog.show();

        stopLiveConnectivityMonitor();
        activeMonitorCleanup = cleanup;
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                manager.registerDefaultNetworkCallback(callback);
            } else {
                manager.registerNetworkCallback(
                        new NetworkRequest.Builder().clearCapabilities().build(), callback);
            }
            registered[0] = true;
            updateLiveMonitorText(activity, manager, output, dialog, lastEvent[0]);
        } catch (SecurityException e) {
            output.setText("Android denied access to network-state callbacks. No network settings were changed.");
        } catch (Exception e) {
            output.setText("Live monitoring could not start on this device. The one-time Network Health Snapshot remains available.");
        }
    }

    private static void updateLiveMonitorText(Activity activity, ConnectivityManager manager,
                                              TextView output, AlertDialog dialog, String event) {
        if (activity.isFinishing() || activity.isDestroyed() || !dialog.isShowing()) return;
        try {
            Network active = manager.getActiveNetwork();
            if (active == null) {
                output.setText(ConnectivityIntelligenceRules.liveConnectivityStatus(
                        false, false, "", false, false, false, event));
                return;
            }
            NetworkCapabilities caps = manager.getNetworkCapabilities(active);
            if (caps == null) {
                output.setText(ConnectivityIntelligenceRules.liveConnectivityStatus(
                        true, false, "", false, false, false, event));
                return;
            }
            StringBuilder transports = new StringBuilder();
            appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_WIFI, "Wi-Fi");
            appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_CELLULAR, "Mobile data");
            appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_ETHERNET, "Ethernet");
            appendTransport(transports, caps, NetworkCapabilities.TRANSPORT_VPN, "VPN");
            if (transports.length() == 0) transports.append("Other / unspecified");
            output.setText(ConnectivityIntelligenceRules.liveConnectivityStatus(
                    true, true, transports.toString(),
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
                    event));
        } catch (SecurityException e) {
            output.setText("Android denied access to network status. Monitoring remains limited to visible status only.");
        } catch (Exception e) {
            output.setText("Current network status is temporarily unavailable. No settings were changed.");
        }
    }

    private static void appendTransport(StringBuilder out, NetworkCapabilities caps,
                                        int transport, String label) {
        if (caps.hasTransport(transport)) {
            if (out.length() > 0) out.append(" • ");
            out.append(label);
        }
    }

    private static void showOfflineMatrix(Activity activity) {
        String report = ConnectivityIntelligenceRules.offlineCapabilityMatrix();
        new AlertDialog.Builder(activity).setTitle("Offline Capability Matrix")
                .setMessage(report).setPositiveButton("Close", null).show();
    }


    /** Owner-invoked, read-only advice based on Android's current default-network capabilities. */
    private static void showReadinessAdvisor(Activity activity) {
        String report;
        try {
            ConnectivityManager manager = (ConnectivityManager)
                    activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null || Build.VERSION.SDK_INT < 23) {
                report = ConnectivityIntelligenceRules.connectivityReadinessReport(
                        "Hybrid", false, false, false, false, false);
            } else {
                Network active = manager.getActiveNetwork();
                NetworkCapabilities caps = active == null ? null : manager.getNetworkCapabilities(active);
                SharedPreferences prefs = activity.getSharedPreferences("jarvis_preferences", Context.MODE_PRIVATE);
                String mode = ConnectivityIntelligenceRules.normalizeAiMode(
                        prefs.getString("ai_connectivity_mode", "Hybrid"));
                report = ConnectivityIntelligenceRules.connectivityReadinessReport(mode, active != null,
                        caps != null, caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
                        caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                        caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED));
            }
        } catch (Exception e) {
            report = "Readiness report is temporarily unavailable. No network settings were changed.";
        }
        new AlertDialog.Builder(activity).setTitle("Connectivity Readiness Advisor")
                .setMessage(report).setPositiveButton("Close", null).show();
    }

    private static void showStrategyPlanner(Activity activity, TextView status, TextView transcript) {
        String[] strategies = {
                "Local-only network concept",
                "Hybrid local + internet design",
                "Satellite internet & CubeSat learning"
        };
        new AlertDialog.Builder(activity).setTitle("Choose a connectivity topic")
                .setItems(strategies, (dialog, which) ->
                        askBrief(activity, strategies[which], details ->
                                confirmAndGenerate(activity, status, transcript, strategies[which], details)))
                .setNegativeButton("Cancel", null).show();
    }

    private static void askBrief(Activity activity, String strategy, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint("Describe your goal, devices and constraints. No passwords, keys or private network identifiers.");
        input.setMinLines(2);
        input.setMaxLines(5);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(strategy)
                .setMessage("Enter a short, non-sensitive brief (maximum 700 characters).")
                .setView(layout)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Enter a short goal first.", Toast.LENGTH_SHORT).show();
                    } else if (value.length() > 700) {
                        Toast.makeText(activity, "Please keep the brief under 700 characters.", Toast.LENGTH_LONG).show();
                    } else {
                        action.submit(value);
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private static void confirmAndGenerate(Activity activity, TextView status, TextView transcript,
                                           String strategy, String details) {
        String prompt = ConnectivityIntelligenceRules.plannerPrompt(strategy, details);
        new AlertDialog.Builder(activity).setTitle("Send connectivity brief?")
                .setMessage("Only the brief you entered and the selected topic will be sent to JARVIS AI. Do not include passwords, Wi-Fi keys, account tokens, IP/MAC addresses or private identifiers. This planner will not configure networks or connect to satellite services.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate plan", (d, w) -> {
                    status.setText("CONNECTIVITY INTELLIGENCE • PROCESSING");
                    transcript.setText("Preparing your connectivity plan…");
                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        status.setText("CONNECTIVITY PLAN READY");
                        transcript.setText(reply);
                        new AlertDialog.Builder(activity).setTitle("JARVIS • Connectivity Plan")
                                .setMessage(reply).setPositiveButton("Done", null).show();
                    });
                }).show();
    }
}
