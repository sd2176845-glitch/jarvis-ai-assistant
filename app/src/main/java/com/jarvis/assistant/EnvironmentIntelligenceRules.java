package com.jarvis.assistant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Validation and summaries for user-entered public AQI observations. */
public final class EnvironmentIntelligenceRules {
    private EnvironmentIntelligenceRules() { }

    public static final String CPCB_AQI_URL = "https://airquality.cpcb.gov.in/AQI_India/";
    public static final String OGD_AQI_URL = "https://data.gov.in/resource/real-time-air-quality-index-various-locations";
    public static final String SOURCE_NAME = "CPCB National AQI";
    public static final String SOURCE_REVIEW_DATE = "24 November 2026";

    public static String categoryForAqi(int value) {
        if (value < 0 || value > 500) return "Out of range";
        if (value <= 50) return "Good";
        if (value <= 100) return "Satisfactory";
        if (value <= 200) return "Moderate";
        if (value <= 300) return "Poor";
        if (value <= 400) return "Very Poor";
        return "Severe";
    }


    /** U.S. EPA AQI bands used by the Open-Meteo model; not India's CPCB National AQI. */
    public static String usAqiCategory(int value) {
        if (value < 0) return "Not supplied";
        if (value <= 50) return "Good";
        if (value <= 100) return "Moderate";
        if (value <= 150) return "Unhealthy for sensitive groups";
        if (value <= 200) return "Unhealthy";
        if (value <= 300) return "Very unhealthy";
        if (value <= 500) return "Hazardous";
        return "Beyond standard US AQI bands";
    }

    /**
     * Formats a bounded model response. This deliberately labels US/EU indices separately
     * from India's CPCB National AQI and never implies a ground-station measurement.
     */
    public static String formatLiveAirQuality(String place, int usAqi, int europeanAqi,
                                               String pm25, String pm10, String ozone,
                                               String nitrogenDioxide, String sulphurDioxide,
                                               String uvIndex, String modelTime, String timezone) {
        String safePlace = safeLabel(place, 80);
        String safeTime = safeLabel(modelTime, 40);
        String safeZone = safeLabel(timezone, 60);
        return "MODELLED AIR QUALITY SNAPSHOT\n\n"
                + "Place: " + safePlace + "\n"
                + "U.S. AQI: " + (usAqi < 0 ? "Not supplied" : usAqi + " • " + usAqiCategory(usAqi)) + "\n"
                + "European AQI: " + (europeanAqi < 0 ? "Not supplied" : String.valueOf(europeanAqi)) + "\n\n"
                + "Pollutants (model estimates)\n"
                + "PM2.5: " + safeMeasurement(pm25) + " µg/m³\n"
                + "PM10: " + safeMeasurement(pm10) + " µg/m³\n"
                + "Ozone: " + safeMeasurement(ozone) + " µg/m³\n"
                + "Nitrogen dioxide: " + safeMeasurement(nitrogenDioxide) + " µg/m³\n"
                + "Sulphur dioxide: " + safeMeasurement(sulphurDioxide) + " µg/m³\n"
                + "UV index: " + safeMeasurement(uvIndex) + "\n\n"
                + "Model time: " + (safeTime.isEmpty() ? "Not supplied" : safeTime) + "\n"
                + "Time zone: " + (safeZone.isEmpty() ? "Not supplied" : safeZone) + "\n\n"
                + "Source: Open-Meteo Air Quality API • CAMS model data.\n"
                + "Important: These are modelled grid estimates, not CPCB monitoring-station readings. "
                + "U.S. and European AQI scales are not India's CPCB National AQI scale. "
                + "Coverage, resolution and local conditions vary; this is not an official alert or medical advice.";
    }

    private static String safeMeasurement(String value) {
        if (value == null || !value.matches("[0-9]{1,6}(\\.[0-9]{1,3})?")) return "Not supplied";
        return value;
    }

    private static String safeLabel(String value, int maxLength) {
        if (value == null) return "";
        String cleaned = value.replace((char) 10, ' ').replace((char) 13, ' ').replace((char) 9, ' ').trim();
        if (cleaned.length() > maxLength) cleaned = cleaned.substring(0, maxLength);
        return cleaned;
    }

    public static String validateObservation(String area, String aqiText, String observedAt) {
        if (area == null || area.trim().isEmpty() || area.trim().length() > 60
                || area.contains("://") || area.contains("@")
                || area.indexOf('\n') >= 0 || area.indexOf('\r') >= 0 || area.indexOf('\t') >= 0) {
            return "Enter a city or monitoring-station label up to 60 characters; do not enter a home address.";
        }
        if (aqiText == null || !aqiText.trim().matches("[0-9]{1,3}")) {
            return "AQI must be a whole number from 0 to 500.";
        }
        try {
            int value = Integer.parseInt(aqiText.trim());
            if (value < 0 || value > 500) return "AQI must be between 0 and 500.";
        } catch (NumberFormatException e) {
            return "AQI must be between 0 and 500.";
        }
        if (!isValidObservedAt(observedAt)) {
            return "Observation time must be a real local date/time in YYYY-MM-DD HH:MM format.";
        }
        return null;
    }

    public static boolean isValidObservedAt(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}")) return false;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            format.setLenient(false);
            Date parsed = format.parse(value);
            return parsed != null && value.equals(format.format(parsed));
        } catch (Exception e) {
            return false;
        }
    }

    /** Aggregates counts only; values from different cities/stations are not averaged together. */
    public static String buildSummary(String[] areas, int[] values, String[] observedAt) {
        if (areas == null || values == null || observedAt == null
                || areas.length != values.length || values.length != observedAt.length) {
            return "Environmental log unavailable: saved observations could not be read consistently.";
        }
        int[] counts = new int[6];
        int invalid = 0;
        for (int i = 0; i < values.length; i++) {
            if (!isValidObservedAt(observedAt[i]) || areas[i] == null || areas[i].trim().isEmpty()) {
                invalid++;
                continue;
            }
            String category = categoryForAqi(values[i]);
            if ("Out of range".equals(category)) {
                invalid++;
                continue;
            }
            if ("Good".equals(category)) counts[0]++;
            else if ("Satisfactory".equals(category)) counts[1]++;
            else if ("Moderate".equals(category)) counts[2]++;
            else if ("Poor".equals(category)) counts[3]++;
            else if ("Very Poor".equals(category)) counts[4]++;
            else counts[5]++;
        }
        return "LOCAL AQI OBSERVATION SUMMARY\n\n"
                + "Valid observations: " + (values.length - invalid) + "\n"
                + "Good: " + counts[0] + "\n"
                + "Satisfactory: " + counts[1] + "\n"
                + "Moderate: " + counts[2] + "\n"
                + "Poor: " + counts[3] + "\n"
                + "Very Poor: " + counts[4] + "\n"
                + "Severe: " + counts[5] + "\n"
                + "Entries needing review: " + invalid + "\n\n"
                + "Readings from different monitoring locations are counted separately, not averaged. "
                + "This report reflects manual entries saved on this device, not live monitoring.";
    }
}
