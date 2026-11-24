package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Owner-invoked read-only CPCB station pollutant lookup through the official OGD API.
 * The data.gov.in API key is requested for each lookup and is never persisted or logged.
 */
public final class CpcbStationStudio {
    private CpcbStationStudio() { }

    private static final int MAX_RECORDS = 100;
    private static final int MAX_RESPONSE_CHARS = 1500000;

    private static final class StationRecord {
        String station;
        String city;
        String state;
        String pollutant;
        String average;
        String minimum;
        String maximum;
        String unit;
        String updatedAt;

        String label() {
            String stationName = CpcbStationRules.displayValue(station);
            String pollutantName = CpcbStationRules.displayValue(pollutant);
            String value = CpcbStationRules.displayMeasurement(average);
            return stationName + " • " + pollutantName + " • Avg " + value;
        }

        String detail() {
            return CpcbStationRules.formatStationRecord(
                    station, city, state, pollutant, average, minimum, maximum, unit, updatedAt);
        }

        String comparisonLabel() {
            return CpcbStationRules.comparisonLabel(station, pollutant, average, unit);
        }
    }

    public static void show(Activity activity) {
        show(activity, false);
    }

    /** Separate station-by-station comparison view; reuses the same live OGD request. */
    public static void showComparison(Activity activity) {
        show(activity, true);
    }

    private static void show(Activity activity, boolean comparison) {
        EditText city = new EditText(activity);
        city.setSingleLine(true);
        city.setHint("City (required, e.g. Kolkata)");
        city.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        EditText state = new EditText(activity);
        state.setSingleLine(true);
        state.setHint("State / UT (optional, use published spelling)");
        state.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        EditText apiKey = new EditText(activity);
        apiKey.setSingleLine(true);
        apiKey.setHint("data.gov.in API key (not saved)");
        apiKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout fields = new LinearLayout(activity);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(24, 8, 24, 8);
        fields.addView(city);
        fields.addView(state);
        fields.addView(apiKey);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(comparison ? "CPCB • Compare monitoring stations" : "CPCB • Monitoring-station readings")
                .setMessage("Enter a city and your data.gov.in API key. The key is sent only over HTTPS to api.data.gov.in for this request, is not saved in JARVIS, and is never shared with another service. No GPS is used."
                        + (comparison ? " Each station and pollutant remains separate; JARVIS does not calculate a city-wide average." : ""))
                .setView(fields)
                .setPositiveButton(comparison ? "Compare stations" : "Fetch station data", null)
                .setNeutralButton("API key information", (d, which) -> openApiHelp(activity))
                .setNegativeButton("Close", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String cityValue = city.getText().toString().trim();
                String stateValue = state.getText().toString().trim();
                String keyValue = apiKey.getText().toString().trim();
                String error = CpcbStationRules.validateRequest(cityValue, stateValue, keyValue);
                if (error != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                    return;
                }
                dialog.dismiss();
                fetch(activity, cityValue, stateValue, keyValue, comparison);
            });
        });
        dialog.show();
    }

    private static void fetch(Activity activity, String city, String state, String apiKey, boolean comparison) {
        Toast.makeText(activity, "Requesting CPCB station records…", Toast.LENGTH_SHORT).show();
        Thread worker = new Thread(() -> {
            List<StationRecord> records = new ArrayList<>();
            String error = null;
            String latestUpdate = "";
            int total = 0;
            try {
                String address = buildApiUrl(city, state, apiKey);
                HttpURLConnection connection = null;
                String body;
                int status;
                try {
                    connection = (HttpURLConnection) new URL(address).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(12000);
                    connection.setReadTimeout(20000);
                    connection.setRequestProperty("Accept", "application/json");
                    status = connection.getResponseCode();
                    InputStream stream = status >= 200 && status < 300
                            ? connection.getInputStream() : connection.getErrorStream();
                    body = readBounded(stream);
                } finally {
                    if (connection != null) connection.disconnect();
                }

                if (status == 401 || status == 403) {
                    throw new IllegalStateException("The data.gov.in API key was rejected or does not have access to this resource.");
                }
                if (status < 200 || status >= 300) {
                    throw new IllegalStateException("data.gov.in returned HTTP " + status + ". Try again later.");
                }

                JSONObject root = new JSONObject(body);
                String providerStatus = root.optString("status", "");
                if ("error".equalsIgnoreCase(providerStatus)) {
                    throw new IllegalStateException("data.gov.in reported an API error. Check the key and exact city/state spelling.");
                }
                total = root.optInt("total", root.optInt("count", 0));
                JSONArray rows = root.optJSONArray("records");
                if (rows == null) {
                    throw new IllegalStateException("The official provider response did not contain a records list.");
                }
                int count = Math.min(rows.length(), MAX_RECORDS);
                for (int i = 0; i < count; i++) {
                    JSONObject source = rows.optJSONObject(i);
                    if (source == null) continue;
                    StationRecord record = parseRecord(source);
                    if (record == null) continue;
                    records.add(record);
                    if (latestUpdate.isEmpty() && !record.updatedAt.isEmpty()) {
                        latestUpdate = record.updatedAt;
                    }
                }
                if (records.isEmpty()) {
                    error = "No station-pollutant records were returned for this city/state. "
                            + "Try the exact city or state spelling used by CPCB. No live result was inferred.";
                }
            } catch (Exception e) {
                error = e.getMessage() == null
                        ? "The official CPCB data service could not be read. Open the source portal to check availability."
                        : e.getMessage();
            }

            final List<StationRecord> result = records;
            final String message = error;
            final int totalRecords = total;
            final String timestamp = latestUpdate;
            activity.runOnUiThread(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (result.isEmpty()) {
                    new AlertDialog.Builder(activity)
                            .setTitle("CPCB station lookup")
                            .setMessage(message == null ? "No station records returned." : message)
                            .setPositiveButton("Open CPCB portal", (d, w) -> openCpcbPortal(activity))
                            .setNegativeButton("Close", null)
                            .show();
                } else if (comparison) {
                    showComparisonResults(activity, city, state, result, totalRecords, timestamp);
                } else {
                    showRecords(activity, city, state, result, totalRecords, timestamp);
                }
            });
        }, "jarvis-cpcb-stations");
        worker.start();
    }

    private static String buildApiUrl(String city, String state, String apiKey) throws Exception {
        StringBuilder out = new StringBuilder(CpcbStationRules.API_BASE);
        out.append("?api-key=").append(encode(apiKey));
        out.append("&format=json&limit=1000&offset=0");
        out.append("&filters%5Bcity%5D=").append(encode(city));
        if (state != null && !state.trim().isEmpty()) {
            out.append("&filters%5Bstate%5D=").append(encode(state.trim()));
        }
        return out.toString();
    }

    private static StationRecord parseRecord(JSONObject source) {
        String station = firstValue(source, "station", "station_name");
        String pollutant = firstValue(source, "pollutant_id", "pollutant");
        if (station.isEmpty() || pollutant.isEmpty()) return null;

        StationRecord record = new StationRecord();
        record.station = station;
        record.city = firstValue(source, "city");
        record.state = firstValue(source, "state");
        record.pollutant = pollutant;
        record.average = firstValue(source, "avg_value", "average", "pollutant_avg");
        record.minimum = firstValue(source, "min_value", "minimum", "pollutant_min");
        record.maximum = firstValue(source, "max_value", "maximum", "pollutant_max");
        record.unit = firstValue(source, "unit", "pollutant_unit");
        record.updatedAt = firstValue(source, "last_update", "last_updated", "updated_at");
        return record;
    }

    private static String firstValue(JSONObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.isNull(key)) {
                String value = CpcbStationRules.clean(object.optString(key, ""), 180);
                if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) return value;
            }
        }
        return "";
    }

    private static String readBounded(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (body.length() + line.length() > MAX_RESPONSE_CHARS) {
                    throw new IllegalStateException("The provider response exceeded the supported size limit.");
                }
                body.append(line);
            }
        }
        return body.toString();
    }

    private static void showRecords(Activity activity, String city, String state,
                                    List<StationRecord> records, int total, String latestUpdate) {
        String[] labels = new String[records.size()];
        for (int i = 0; i < records.size(); i++) labels[i] = records.get(i).label();
        String summary = CpcbStationRules.buildSummary(city, state, records.size(), latestUpdate);
        if (total > records.size()) {
            summary += "\n\nShowing the first " + records.size() + " matching records from "
                    + total + " records reported by the provider.";
        }
        new AlertDialog.Builder(activity)
                .setTitle("CPCB station readings • " + records.size())
                .setMessage(summary)
                .setItems(labels, (dialog, which) -> showRecordDetail(activity, records.get(which)))
                .setPositiveButton("Open CPCB portal", (dialog, which) -> openCpcbPortal(activity))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void showComparisonResults(Activity activity, String city, String state,
                                                List<StationRecord> records, int total, String latestUpdate) {
        List<StationRecord> sorted = new ArrayList<>(records);
        Collections.sort(sorted, (left, right) -> {
            int pollutantOrder = CpcbStationRules.displayValue(left.pollutant)
                    .compareToIgnoreCase(CpcbStationRules.displayValue(right.pollutant));
            if (pollutantOrder != 0) return pollutantOrder;
            return CpcbStationRules.displayValue(left.station)
                    .compareToIgnoreCase(CpcbStationRules.displayValue(right.station));
        });

        Set<String> stations = new HashSet<>();
        Set<String> pollutants = new HashSet<>();
        String[] labels = new String[sorted.size()];
        for (int i = 0; i < sorted.size(); i++) {
            StationRecord record = sorted.get(i);
            labels[i] = record.comparisonLabel();
            stations.add(record.station);
            pollutants.add(record.pollutant);
        }

        String summary = CpcbStationRules.buildSummary(city, state, sorted.size(), latestUpdate)
                + "\n\nStations compared: " + stations.size()
                + "\nPollutants represented: " + pollutants.size()
                + "\n\n" + CpcbStationRules.comparisonDisclaimer();
        if (total > sorted.size()) {
            summary += "\n\nShowing " + sorted.size() + " matching rows from "
                    + total + " records reported by the provider.";
        }
        new AlertDialog.Builder(activity)
                .setTitle("CPCB station comparison • " + stations.size() + " stations")
                .setMessage(summary)
                .setItems(labels, (dialog, which) -> showRecordDetail(activity, sorted.get(which)))
                .setPositiveButton("Open CPCB portal", (dialog, which) -> openCpcbPortal(activity))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void showRecordDetail(Activity activity, StationRecord record) {
        new AlertDialog.Builder(activity)
                .setTitle(CpcbStationRules.displayValue(record.pollutant) + " • "
                        + CpcbStationRules.displayValue(record.station))
                .setMessage(record.detail())
                .setPositiveButton("Done", null)
                .setNeutralButton("Open CPCB", (dialog, which) -> openCpcbPortal(activity))
                .show();
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
    }

    private static void openApiHelp(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CpcbStationRules.API_HELP_URL)));
        } catch (Exception e) {
            Toast.makeText(activity, "No browser is available to open the official API help page.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private static void openCpcbPortal(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://airquality.cpcb.gov.in/AQI_India/")));
        } catch (Exception e) {
            Toast.makeText(activity, "No browser is available to open the CPCB portal.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
