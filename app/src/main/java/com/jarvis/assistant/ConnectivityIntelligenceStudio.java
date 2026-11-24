package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
                "Network Health Snapshot • read-only",
                "Offline Capability Matrix • local readiness",
                "Connectivity Strategy Planner • local / hybrid / satellite"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Connectivity Intelligence")
                .setItems(tools, (dialog, which) -> {
                    if (which == 0) showNetworkSnapshot(activity);
                    else if (which == 1) showOfflineMatrix(activity);
                    else showStrategyPlanner(activity, status, transcript);
                }).setNegativeButton("Close", null).show();
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
