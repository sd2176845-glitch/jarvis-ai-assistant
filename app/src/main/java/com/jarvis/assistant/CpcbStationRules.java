package com.jarvis.assistant;

/** Pure validation and display rules for official CPCB station pollutant records. */
public final class CpcbStationRules {
    private CpcbStationRules() { }

    public static final String RESOURCE_ID = "3b01bcb8-0b14-4abf-b6f2-c1bfd384ba69";
    public static final String API_BASE =
            "https://api.data.gov.in/resource/" + RESOURCE_ID;
    public static final String DATASET_URL =
            "https://www.data.gov.in/resource/real-time-air-quality-index-various-locations";
    public static final String API_HELP_URL = "https://www.data.gov.in/help";

    public static String validateRequest(String city, String state, String apiKey) {
        if (city == null || city.trim().isEmpty() || city.trim().length() > 80
                || hasUnsafeCharacters(city)) {
            return "Enter a city name up to 80 characters.";
        }
        if (state != null && (!state.trim().isEmpty())
                && (state.trim().length() > 80 || hasUnsafeCharacters(state))) {
            return "Enter a state name up to 80 characters, or leave it blank.";
        }
        if (apiKey == null || !apiKey.trim().matches("[A-Za-z0-9._-]{12,200}")) {
            return "Enter a valid data.gov.in API key. The key is used only for this request and is not saved.";
        }
        return null;
    }

    private static boolean hasUnsafeCharacters(String value) {
        if (value == null) return true;
        return value.contains("://") || value.contains("@")
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0
                || value.indexOf('\t') >= 0;
    }

    public static String clean(String value, int maxLength) {
        if (value == null) return "";
        String result = value.replace((char) 10, ' ')
                .replace((char) 13, ' ')
                .replace((char) 9, ' ')
                .replaceAll("\\s+", " ").trim();
        if (result.length() > maxLength) result = result.substring(0, maxLength);
        return result;
    }

    public static String displayValue(String value) {
        String result = clean(value, 120);
        return result.isEmpty() || "-".equals(result) || "NA".equalsIgnoreCase(result)
                || "null".equalsIgnoreCase(result) ? "Not supplied" : result;
    }

    /** Keep instrument values numeric; never reinterpret an arbitrary string as a reading. */
    public static String displayMeasurement(String value) {
        String result = clean(value, 24);
        if (!result.matches("[0-9]{1,8}(\\.[0-9]{1,4})?")) return "Not supplied";
        try {
            double parsed = Double.parseDouble(result);
            if (Double.isNaN(parsed) || Double.isInfinite(parsed) || parsed < 0) {
                return "Not supplied";
            }
            return result;
        } catch (NumberFormatException e) {
            return "Not supplied";
        }
    }

    public static String formatStationRecord(String station, String city, String state,
                                             String pollutant, String average, String minimum,
                                             String maximum, String unit, String updatedAt) {
        StringBuilder out = new StringBuilder("CPCB MONITORING-STATION RECORD\n\n");
        out.append("Station: ").append(displayValue(station)).append("\n");
        out.append("City: ").append(displayValue(city)).append("\n");
        out.append("State / UT: ").append(displayValue(state)).append("\n");
        out.append("Pollutant: ").append(displayValue(pollutant)).append("\n");
        String unitLabel = clean(unit, 32);
        out.append("Average: ").append(displayMeasurement(average));
        if (!unitLabel.isEmpty()) out.append(" ").append(unitLabel);
        out.append("\nMinimum: ").append(displayMeasurement(minimum));
        if (!unitLabel.isEmpty()) out.append(" ").append(unitLabel);
        out.append("\nMaximum: ").append(displayMeasurement(maximum));
        if (!unitLabel.isEmpty()) out.append(" ").append(unitLabel);
        out.append("\nLast update: ").append(displayValue(updatedAt)).append("\n\n");
        out.append("Source: CPCB real-time air-quality dataset via data.gov.in.\n");
        out.append("This is a pollutant-specific station instrument record, not a combined AQI value. ");
        out.append("The publisher warns that live instrument readings can contain errors or abnormal values; ");
        out.append("check the official CPCB portal and timestamp before relying on a reading.");
        return out.toString();
    }

    /** A station-specific comparison label; values are never combined across stations. */
    public static String comparisonLabel(String station, String pollutant, String average, String unit) {
        StringBuilder out = new StringBuilder();
        out.append(displayValue(pollutant)).append(" • ")
                .append(displayValue(station)).append(" • Avg ")
                .append(displayMeasurement(average));
        String unitLabel = clean(unit, 32);
        if (!unitLabel.isEmpty()) out.append(" ").append(unitLabel);
        return out.toString();
    }

    public static String comparisonDisclaimer() {
        return "Station comparison only: each pollutant value remains tied to its named monitoring station. "
                + "JARVIS does not average readings across stations or treat a pollutant concentration as a combined AQI score. "
                + "One station may not represent the entire city.";
    }

    public static String buildSummary(String city, String state, int records, String latestUpdate) {
        StringBuilder out = new StringBuilder("OFFICIAL CPCB STATION DATA\n\n");
        out.append("Requested city: ").append(displayValue(city)).append("\n");
        out.append("Requested state: ").append(displayValue(state)).append("\n");
        out.append("Station-pollutant records returned: ").append(Math.max(0, records)).append("\n");
        out.append("Latest timestamp in returned rows: ").append(displayValue(latestUpdate)).append("\n\n");
        out.append("Source: Central Pollution Control Board • Open Government Data Platform India.\n");
        out.append("The dataset is published at hourly granularity. It contains direct field-instrument data and may show ");
        out.append("errors or abnormal values. This view does not calculate or infer an AQI score, does not issue a health alert, ");
        out.append("and does not guarantee that every station in the city is represented.");
        return out.toString();
    }
}
