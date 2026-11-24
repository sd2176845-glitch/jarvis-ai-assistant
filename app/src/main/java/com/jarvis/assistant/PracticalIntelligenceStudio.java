package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** M19 owner-invoked preparedness/legal explainers and local vehicle service log. */
public final class PracticalIntelligenceStudio {
    private PracticalIntelligenceStudio() {}

    private static final String PREFS = "jarvis_preferences";
    private static final String VEHICLE_LOG_KEY = "vehicle_service_log_v1";
    private static final int MAX_RECORDS = 50;

    private interface TextAction { void submit(String value); }

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] options = {
                "Emergency Readiness Planner • general checklist",
                "Legal Document Explainer • selected text only",
                "Vehicle Maintenance Log • local records"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Practical Intelligence")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) emergencyPlanner(activity, status, transcript);
                    else if (which == 1) legalExplainer(activity, status, transcript);
                    else vehicleLogMenu(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void emergencyPlanner(Activity activity, TextView status, TextView transcript) {
        String[] hazards = {"Flood", "Cyclone / severe storm", "Heatwave", "Earthquake", "General emergency"};
        new AlertDialog.Builder(activity).setTitle("Choose preparedness topic")
                .setItems(hazards, (dialog, which) -> askText(activity, "Emergency readiness brief",
                        "Optional city/district and general household needs. Do not enter an exact address or personal details.",
                        500, brief -> confirmAi(activity, status, transcript, "EMERGENCY READINESS PLAN",
                                PracticalIntelligenceRules.emergencyPrompt(hazards[which], "Only the area you chose in your brief", brief),
                                "Only the short brief you entered will be sent. This creates general preparedness guidance, not live alerts or official evacuation instructions.")))
                .setNegativeButton("Cancel", null).show();
    }

    private static void legalExplainer(Activity activity, TextView status, TextView transcript) {
        askText(activity, "Paste a document excerpt",
                "Paste only the passage you want explained (maximum 4,000 characters). Remove names, addresses, phone numbers, ID numbers, signatures and account details first.",
                4000, excerpt -> confirmAi(activity, status, transcript, "LEGAL DOCUMENT EXPLANATION",
                        PracticalIntelligenceRules.legalPrompt(excerpt),
                        "The selected text will be sent to JARVIS AI for explanation. Remove personal identifiers first. This is informational only, not legal advice." ));
    }

    private static void askText(Activity activity, String title, String hint, int max, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(3);
        input.setMaxLines(8);
        input.setGravity(Gravity.TOP);
        input.setPadding(24, 16, 24, 16);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(input);
        new AlertDialog.Builder(activity).setTitle(title).setView(scroll)
                .setPositiveButton("Continue", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) Toast.makeText(activity, "Enter a short brief first.", Toast.LENGTH_SHORT).show();
                    else if (value.length() > max) Toast.makeText(activity,
                            "Please keep the entry under " + max + " characters.", Toast.LENGTH_LONG).show();
                    else action.submit(value);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void confirmAi(Activity activity, TextView status, TextView transcript,
                                 String title, String prompt, String disclosure) {
        new AlertDialog.Builder(activity).setTitle("Confirm AI request")
                .setMessage(disclosure + "\n\nJARVIS will not save the submitted text in its notes or recent conversation memory.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (d, w) -> {
                    status.setText(title + " • PROCESSING");
                    transcript.setText("Preparing your result…");
                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        status.setText(title + " • READY");
                        transcript.setText(reply);
                        new AlertDialog.Builder(activity).setTitle("JARVIS • " + title)
                                .setMessage(reply).setPositiveButton("Done", null).show();
                    });
                }).show();
    }

    private static SharedPreferences preferences(Activity activity) {
        return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    }

    private static JSONArray readVehicleLog(Activity activity) {
        try {
            String raw = preferences(activity).getString(VEHICLE_LOG_KEY, "[]");
            return new JSONArray(raw == null ? "[]" : raw);
        } catch (Exception e) {
            Toast.makeText(activity, "Vehicle log data is invalid; existing data was left unchanged.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static void vehicleLogMenu(Activity activity) {
        String[] actions = {"Add maintenance record", "View / remove records", "Clear maintenance log"};
        new AlertDialog.Builder(activity).setTitle("Local Vehicle Maintenance Log")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) addVehicleRecord(activity);
                    else if (which == 1) viewVehicleRecords(activity);
                    else clearVehicleLog(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static void addVehicleRecord(Activity activity) {
        JSONArray current = readVehicleLog(activity);
        if (current == null) return;
        if (current.length() >= MAX_RECORDS) {
            Toast.makeText(activity, "The local log is limited to " + MAX_RECORDS + " records.", Toast.LENGTH_LONG).show();
            return;
        }
        EditText vehicle = field(activity, "Vehicle nickname (no plate/VIN)");
        EditText date = field(activity, "Service date (e.g. 2026-11-22)");
        EditText odometer = field(activity, "Odometer reading (digits only, optional)");
        odometer.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        EditText summary = field(activity, "Maintenance summary");
        summary.setMinLines(2);
        summary.setMaxLines(4);
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 8);
        form.addView(vehicle);
        form.addView(date);
        form.addView(odometer);
        form.addView(summary);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(form);
        new AlertDialog.Builder(activity).setTitle("Add service record")
                .setMessage("Store only a nickname and maintenance summary. Do not enter registration numbers, VINs, insurance details or personal information.")
                .setView(scroll)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save locally", (dialog, which) -> {
                    String v = vehicle.getText().toString().trim();
                    String d = date.getText().toString().trim();
                    String o = odometer.getText().toString().trim();
                    String s = summary.getText().toString().trim();
                    String error = PracticalIntelligenceRules.validateVehicleEntry(v, d, o, s);
                    if (error != null) {
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONArray latest = readVehicleLog(activity);
                    if (latest == null || latest.length() >= MAX_RECORDS) return;
                    try {
                        JSONObject item = new JSONObject();
                        item.put("vehicle", v);
                        item.put("date", d);
                        item.put("odometer", o);
                        item.put("summary", s);
                        item.put("created_at", System.currentTimeMillis());
                        latest.put(item);
                        boolean saved = preferences(activity).edit().putString(VEHICLE_LOG_KEY, latest.toString()).commit();
                        Toast.makeText(activity, saved ? "Maintenance record saved on this phone." : "Could not save record.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(activity, "Record could not be saved.", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private static EditText field(Activity activity, String hint) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setSingleLine(true);
        return input;
    }

    private static void viewVehicleRecords(Activity activity) {
        JSONArray records = readVehicleLog(activity);
        if (records == null) return;
        if (records.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Vehicle Maintenance Log")
                    .setMessage("No service records saved yet.").setPositiveButton("OK", null).show();
            return;
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item == null) labels.add((i + 1) + ". Invalid record");
            else labels.add((i + 1) + ". " + item.optString("vehicle") + " • "
                    + item.optString("date") + " • " + item.optString("summary"));
        }
        String[] choices = labels.toArray(new String[0]);
        new AlertDialog.Builder(activity).setTitle("Saved records • tap to remove")
                .setItems(choices, (dialog, which) -> new AlertDialog.Builder(activity)
                        .setTitle("Remove this maintenance record?")
                        .setMessage(choices[which]).setNegativeButton("Cancel", null)
                        .setPositiveButton("Remove", (d, w) -> removeVehicleRecord(activity, which)).show())
                .setNegativeButton("Close", null).show();
    }

    private static void removeVehicleRecord(Activity activity, int index) {
        JSONArray current = readVehicleLog(activity);
        if (current == null || index < 0 || index >= current.length()) return;
        JSONArray updated = new JSONArray();
        for (int i = 0; i < current.length(); i++) if (i != index) updated.put(current.opt(i));
        boolean saved = preferences(activity).edit().putString(VEHICLE_LOG_KEY, updated.toString()).commit();
        Toast.makeText(activity, saved ? "Maintenance record removed." : "Could not update log.", Toast.LENGTH_SHORT).show();
    }

    private static void clearVehicleLog(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Clear vehicle maintenance log?")
                .setMessage("This deletes only the locally saved maintenance records. It does not change tasks or reminders.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    boolean saved = preferences(activity).edit().remove(VEHICLE_LOG_KEY).commit();
                    Toast.makeText(activity, saved ? "Local maintenance log cleared." : "Could not clear log.", Toast.LENGTH_SHORT).show();
                }).show();
    }
}
