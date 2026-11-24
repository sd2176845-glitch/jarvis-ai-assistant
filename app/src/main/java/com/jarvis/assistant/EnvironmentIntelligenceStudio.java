package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Manual, owner-entered environmental observation log.
 * No GPS, background sensors, network polling or automatic data claims.
 */
public final class EnvironmentIntelligenceStudio {
    private EnvironmentIntelligenceStudio() { }

    private static final String PREFS = "jarvis_preferences";
    private static final String KEY = "environment_observations_v1";
    private static final int MAX_ENTRIES = 100;

    public static void show(Activity activity) {
        String[] actions = {
                "Open official CPCB AQI dashboard",
                "Record a manual AQI reading",
                "View saved observations",
                "AQI categories & data notes"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Environmental Intelligence")
                .setMessage("Manual city/station readings only. JARVIS does not access your location, fetch live AQI or monitor the environment in the background.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) openOfficialSource(activity);
                    else if (which == 1) addObservation(activity);
                    else if (which == 2) showObservations(activity);
                    else showCategoryGuide(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static android.content.SharedPreferences preferences(Activity activity) {
        return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    }

    private static JSONArray readEntries(Activity activity) {
        try {
            JSONArray entries = new JSONArray(preferences(activity).getString(KEY, "[]"));
            if (entries.length() > MAX_ENTRIES) {
                Toast.makeText(activity, "Observation log exceeds its supported limit. Nothing was changed.",
                        Toast.LENGTH_LONG).show();
                return null;
            }
            return entries;
        } catch (Exception e) {
            Toast.makeText(activity, "Saved observations could not be read. Nothing was changed.",
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static void addObservation(Activity activity) {
        EditText area = new EditText(activity);
        area.setHint("City / monitoring station (not home address)");
        area.setSingleLine(true);
        area.setMaxLines(1);

        EditText aqi = new EditText(activity);
        aqi.setHint("AQI value (0–500)");
        aqi.setSingleLine(true);
        aqi.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText observedAt = new EditText(activity);
        observedAt.setHint("Observation time YYYY-MM-DD HH:MM");
        observedAt.setSingleLine(true);
        observedAt.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));

        LinearLayout fields = new LinearLayout(activity);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(24, 8, 24, 8);
        fields.addView(area);
        fields.addView(aqi);
        fields.addView(observedAt);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(fields);

        new AlertDialog.Builder(activity).setTitle("Record an AQI observation")
                .setMessage("First check the official CPCB dashboard. The time field starts with your phone local time; compare and edit it to match the reading timestamp shown by CPCB. Enter only a city/station label, not a home address. This is a manual record, not live monitoring.")
                .setView(scroll).setNegativeButton("Cancel", null)
                .setPositiveButton("Save locally", (dialog, which) -> {
                    String place = area.getText().toString().trim();
                    String valueText = aqi.getText().toString().trim();
                    String time = observedAt.getText().toString().trim();
                    String error = EnvironmentIntelligenceRules.validateObservation(place, valueText, time);
                    if (error != null) {
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONArray existing = readEntries(activity);
                    if (existing == null) return;
                    if (existing.length() >= MAX_ENTRIES) {
                        Toast.makeText(activity, "Log is full. Remove an older entry before adding another.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    try {
                        int number = Integer.parseInt(valueText);
                        JSONObject record = new JSONObject();
                        record.put("area", place);
                        record.put("aqi", number);
                        record.put("category", EnvironmentIntelligenceRules.categoryForAqi(number));
                        record.put("observed_at", time);
                        record.put("source", EnvironmentIntelligenceRules.SOURCE_NAME);
                        record.put("recorded_at", System.currentTimeMillis());
                        JSONArray updated = new JSONArray();
                        updated.put(record);
                        for (int i = 0; i < existing.length(); i++) {
                            JSONObject previous = existing.optJSONObject(i);
                            if (previous != null) updated.put(previous);
                        }
                        if (preferences(activity).edit().putString(KEY, updated.toString()).commit()) {
                            Toast.makeText(activity, "AQI observation saved on this device.", Toast.LENGTH_SHORT).show();
                            showObservations(activity);
                        } else {
                            Toast.makeText(activity, "Observation could not be saved.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(activity, "Observation could not be saved.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void showObservations(Activity activity) {
        JSONArray entries = readEntries(activity);
        if (entries == null) return;
        if (entries.length() == 0) {
            new AlertDialog.Builder(activity).setTitle("Saved AQI observations")
                    .setMessage("No manual readings saved yet. Open the official CPCB dashboard, then record a city/station value and its displayed time.")
                    .setPositiveButton("Open CPCB", (d, w) -> openOfficialSource(activity))
                    .setNegativeButton("Close", null).show();
            return;
        }

        String[] areas = new String[entries.length()];
        int[] values = new int[entries.length()];
        String[] times = new String[entries.length()];
        String[] labels = new String[entries.length()];
        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.optJSONObject(i);
            if (item == null) {
                areas[i] = "";
                values[i] = -1;
                times[i] = "";
                labels[i] = "Invalid saved entry";
                continue;
            }
            areas[i] = item.optString("area", "");
            values[i] = item.optInt("aqi", -1);
            times[i] = item.optString("observed_at", "");
            labels[i] = areas[i] + " • AQI " + values[i] + " • "
                    + EnvironmentIntelligenceRules.categoryForAqi(values[i])
                    + " • " + times[i];
        }

        String summary = EnvironmentIntelligenceRules.buildSummary(areas, values, times)
                + "\n\nNewest manual entries appear first. Readings from different stations are not averaged.";
        new AlertDialog.Builder(activity).setTitle("Saved AQI observations")
                .setMessage(summary)
                .setItems(labels, (dialog, which) -> showObservationDetail(activity, which))
                .setNeutralButton("Clear log", (dialog, which) -> confirmClear(activity))
                .setNegativeButton("Close", null).show();
    }

    private static void showObservationDetail(Activity activity, int index) {
        JSONArray entries = readEntries(activity);
        if (entries == null || index < 0 || index >= entries.length()) return;
        JSONObject item = entries.optJSONObject(index);
        if (item == null) return;
        int value = item.optInt("aqi", -1);
        String message = "Location label: " + item.optString("area", "")
                + "\nAQI: " + value
                + "\nCategory: " + EnvironmentIntelligenceRules.categoryForAqi(value)
                + "\nObservation time entered: " + item.optString("observed_at", "")
                + "\nSource: " + item.optString("source", EnvironmentIntelligenceRules.SOURCE_NAME)
                + "\nSaved on this device: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date(item.optLong("recorded_at", 0L)))
                + "\n\nCheck the current station value and timestamp on the official CPCB website. "
                + "Instrument readings may contain gaps or abnormal values.";
        new AlertDialog.Builder(activity).setTitle("AQI observation")
                .setMessage(message)
                .setNeutralButton("Delete entry", (dialog, which) -> confirmDelete(activity, index))
                .setNegativeButton("Close", null).show();
    }

    private static void confirmDelete(Activity activity, int index) {
        new AlertDialog.Builder(activity).setTitle("Delete this observation?")
                .setMessage("Only this saved manual entry will be removed from this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    JSONArray entries = readEntries(activity);
                    if (entries == null || index < 0 || index >= entries.length()) return;
                    entries.remove(index);
                    if (preferences(activity).edit().putString(KEY, entries.toString()).commit()) {
                        Toast.makeText(activity, "Observation deleted.", Toast.LENGTH_SHORT).show();
                        showObservations(activity);
                    } else {
                        Toast.makeText(activity, "Observation could not be deleted.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void confirmClear(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Clear AQI observation log?")
                .setMessage("This removes all manually saved AQI observations from this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    if (preferences(activity).edit().remove(KEY).commit()) {
                        Toast.makeText(activity, "AQI observation log cleared.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "Log could not be cleared.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void showCategoryGuide(Activity activity) {
        String guide = "INDIA NATIONAL AQI CATEGORIES\n\n"
                + "0–50 • Good\n51–100 • Satisfactory\n101–200 • Moderate\n"
                + "201–300 • Poor\n301–400 • Very Poor\n401–500 • Severe\n\n"
                + "Categories follow the CPCB National Air Quality Index. Read the station, timestamp and official advisory; a city label or single station may not represent every neighbourhood.\n\n"
                + "CPCB field-instrument data may contain gaps or abnormal values. This guide and the saved log do not provide medical advice or live alerts.\n"
                + "Source review date: " + EnvironmentIntelligenceRules.SOURCE_REVIEW_DATE;
        new AlertDialog.Builder(activity).setTitle("AQI categories & data notes")
                .setMessage(guide)
                .setPositiveButton("Open CPCB", (d, w) -> openOfficialSource(activity))
                .setNeutralButton("Open OGD data note", (d, w) -> openUrl(activity, EnvironmentIntelligenceRules.OGD_AQI_URL))
                .setNegativeButton("Close", null).show();
    }

    private static void openOfficialSource(Activity activity) {
        openUrl(activity, EnvironmentIntelligenceRules.CPCB_AQI_URL);
    }

    private static void openUrl(Activity activity, String url) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(activity, "No browser is available. Saved observations remain on this device.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
