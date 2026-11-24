package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** Local-first smart-home organizer and owner-invoked setup/routine guidance. No device scanning or control. */
public final class SmartHomeStudio {
    private SmartHomeStudio() {}

    private static final String PREFS = "jarvis_preferences";
    private static final String INVENTORY_KEY = "smart_home_inventory_v1";
    private static final int MAX_ITEMS = 30;

    private interface TextAction { void submit(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] tools = {
                "Device Setup Guide • safe configuration checklist",
                "Room Device Inventory • local list",
                "Smart Routine Planner • manual routines"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Smart Home Studio")
                .setItems(tools, (dialog, which) -> {
                    if (which == 0) setupGuide(activity, status, transcript);
                    else if (which == 1) inventoryMenu(activity);
                    else routinePlanner(activity, status, transcript);
                }).setNegativeButton("Close", null).show();
    }

    private static void setupGuide(Activity activity, TextView status, TextView transcript) {
        askText(activity, "Device Setup Guide",
                "Enter device type, brand/model and what you need help setting up. Never enter passwords, Wi-Fi keys or access codes.",
                700, details -> sendAi(activity, status, transcript,
                        "Give a cautious, beginner-friendly smart-home setup checklist for this owner-described device: "
                                + details + ". Explain how to use the manufacturer's official app/manual, check supported Wi-Fi bands "
                                + "and compatibility claims against official documentation, install firmware updates, change default credentials "
                                + "using the official app, enable multi-factor authentication when available, review privacy settings, "
                                + "and test normal operation. Never ask for or repeat passwords, tokens, Wi-Fi keys, private IP/MAC addresses "
                                + "or access codes. Do not suggest bypassing pairing, account protections or network security. "
                                + "If model-specific facts are uncertain, clearly say to verify the manufacturer's manual. "
                                + "This is guidance only; JARVIS does not connect to or control the device.",
                        "SMART DEVICE SETUP GUIDE"));
    }

    private static void routinePlanner(Activity activity, TextView status, TextView transcript) {
        String[] types = {"Morning routine", "Leaving home", "Evening routine", "Energy-saving routine", "Custom routine"};
        new AlertDialog.Builder(activity).setTitle("Choose routine type")
                .setItems(types, (dialog, which) -> askText(activity, "Routine details",
                        "Describe the rooms and compatible devices you own. Do not include credentials or network details.",
                        900, details -> sendAi(activity, status, transcript,
                                "Create a practical, owner-operated smart-home routine of type " + types[which]
                                        + " using only the devices described here: " + details + ". "
                                        + "Return an ordered manual checklist, required app/device prerequisites, safety checks, "
                                        + "a manual fallback if a device is offline, and a note about actions requiring explicit owner approval. "
                                        + "Do not create hidden automation, security bypasses, unattended access, locks/door controls, "
                                        + "or safety-critical actions involving heating, cooking or electrical equipment. "
                                        + "This is a suggested checklist only; JARVIS will not execute it or send device commands.",
                                "SMART ROUTINE PLAN")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void inventoryMenu(Activity activity) {
        String[] actions = {
                "Add a room/device label",
                "Add Bluetooth accessory label",
                "Open Android Bluetooth settings",
                "View / remove devices",
                "Clear inventory"
        };
        new AlertDialog.Builder(activity).setTitle("Local Smart Home & Accessory Inventory")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) addInventoryItem(activity);
                    else if (which == 1) addBluetoothAccessory(activity);
                    else if (which == 2) openBluetoothSettings(activity);
                    else if (which == 3) viewInventory(activity);
                    else clearInventory(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static SharedPreferences preferences(Activity activity) {
        return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    }

    private static JSONArray readInventory(Activity activity) {
        try {
            String raw = preferences(activity).getString(INVENTORY_KEY, "[]");
            JSONArray array = new JSONArray(raw == null ? "[]" : raw);
            return array;
        } catch (Exception e) {
            Toast.makeText(activity, "Inventory data could not be read; it was left unchanged.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static void addInventoryItem(Activity activity) {
        JSONArray current = readInventory(activity);
        if (current == null) return;
        if (current.length() >= MAX_ITEMS) {
            Toast.makeText(activity, "Inventory limit is " + MAX_ITEMS + " items.", Toast.LENGTH_LONG).show();
            return;
        }
        askText(activity, "Add room/device label",
                "Example: Living room — smart bulb. Do not store passwords, Wi-Fi details, IP/MAC addresses or access codes.",
                120, label -> {
                    JSONArray latest = readInventory(activity);
                    if (latest == null) return;
                    if (latest.length() >= MAX_ITEMS) {
                        Toast.makeText(activity, "Inventory limit is " + MAX_ITEMS + " items.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    latest.put(label);
                    boolean saved = preferences(activity).edit().putString(INVENTORY_KEY, latest.toString()).commit();
                    Toast.makeText(activity, saved ? "Device label saved locally." : "Could not save inventory.", Toast.LENGTH_SHORT).show();
                });
    }

    private static void addBluetoothAccessory(Activity activity) {
        JSONArray current = readInventory(activity);
        if (current == null) return;
        if (current.length() >= MAX_ITEMS) {
            Toast.makeText(activity, "Inventory limit is " + MAX_ITEMS + " items.", Toast.LENGTH_LONG).show();
            return;
        }
        askText(activity, "Add Bluetooth accessory label",
                "Example: Headphones — personal device. Do not enter serial numbers, MAC addresses, PINs or account details.",
                100, details -> {
                    String label = SmartHomeAccessoryRules.createBluetoothLabel(details);
                    if (label == null) {
                        Toast.makeText(activity, "Enter a short accessory label.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray latest = readInventory(activity);
                    if (latest == null) return;
                    if (latest.length() >= MAX_ITEMS) {
                        Toast.makeText(activity, "Inventory limit is " + MAX_ITEMS + " items.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    latest.put(label);
                    boolean saved = preferences(activity).edit().putString(INVENTORY_KEY, latest.toString()).commit();
                    Toast.makeText(activity, saved ? "Bluetooth accessory label saved locally." : "Could not save inventory.", Toast.LENGTH_SHORT).show();
                });
    }

    private static void openBluetoothSettings(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Open Android Bluetooth settings?")
                .setMessage("You will pair or unpair accessories yourself in Android settings. JARVIS does not scan nearby devices, read device identifiers, or pair/connect automatically. To revoke a pairing, choose the accessory in Android Bluetooth settings and select Forget.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Open settings", (dialog, which) -> {
                    try {
                        activity.startActivity(new android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
                    } catch (Exception e) {
                        Toast.makeText(activity, "Bluetooth settings could not be opened. Use Android Settings > Bluetooth.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void viewInventory(Activity activity) {
        JSONArray array = readInventory(activity);
        if (array == null) return;
        if (array.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Smart Home Inventory")
                    .setMessage("No devices saved yet. Add a simple room/device label to start.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) labels.add((i + 1) + ". " + array.optString(i, "Unnamed device"));
        String[] items = labels.toArray(new String[0]);
        new AlertDialog.Builder(activity).setTitle("Saved device labels • tap to remove")
                .setItems(items, (dialog, which) -> new AlertDialog.Builder(activity)
                        .setTitle("Remove this label?")
                        .setMessage(items[which] + (SmartHomeAccessoryRules.isBluetoothLabel(array.optString(which, ""))
                                ? "\n\nThis removes only the JARVIS inventory label. To unpair the accessory, open Android Bluetooth settings and choose Forget."
                                : "\n\nThis removes only the locally saved label."))
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Remove", (d, w) -> removeInventoryItem(activity, which))
                        .show())
                .setNegativeButton("Close", null).show();
    }

    private static void removeInventoryItem(Activity activity, int index) {
        JSONArray current = readInventory(activity);
        if (current == null || index < 0 || index >= current.length()) return;
        JSONArray updated = new JSONArray();
        for (int i = 0; i < current.length(); i++) {
            if (i != index) updated.put(current.optString(i, ""));
        }
        boolean saved = preferences(activity).edit().putString(INVENTORY_KEY, updated.toString()).commit();
        Toast.makeText(activity, saved ? "Inventory label removed." : "Could not update inventory.", Toast.LENGTH_SHORT).show();
    }

    private static void clearInventory(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Clear smart-home inventory?")
                .setMessage("This deletes only the locally saved room/device labels. It does not affect any device or its settings.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    boolean saved = preferences(activity).edit().remove(INVENTORY_KEY).commit();
                    Toast.makeText(activity, saved ? "Local inventory cleared." : "Could not clear inventory.", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private static void askText(Activity activity, String title, String hint, int maxLength, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(2);
        input.setMaxLines(5);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(title).setView(layout)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Enter a short description first.", Toast.LENGTH_SHORT).show();
                    } else if (value.length() > maxLength) {
                        Toast.makeText(activity, "Please keep the entry under " + maxLength + " characters.", Toast.LENGTH_LONG).show();
                    } else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void sendAi(Activity activity, TextView status, TextView transcript,
                               String prompt, String label) {
        new AlertDialog.Builder(activity).setTitle("Send setup/routine brief?")
                .setMessage("Only the description you entered will be sent to JARVIS AI. Never include passwords, Wi-Fi keys, account tokens, IP/MAC addresses or access codes. JARVIS does not scan, pair with or control devices.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate guide", (dialog, which) -> {
                    status.setText("SMART HOME STUDIO • PROCESSING");
                    transcript.setText("Preparing your smart-home guide…");
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
