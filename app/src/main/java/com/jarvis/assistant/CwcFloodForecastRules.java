package com.jarvis.assistant;

import java.util.Locale;

/** Manual interpretation rules based on CWC's published station flood-level categories. */
public final class CwcFloodForecastRules {
    private CwcFloodForecastRules() { }

    public static final String FORECAST_URL = "https://ffs.india-water.gov.in/";
    public static final String SEVEN_DAY_URL = "https://aff.india-water.gov.in/";

    public static String validateLevels(String station, String current, String warning,
                                        String danger, String highestFloodLevel) {
        if (station == null || station.trim().isEmpty() || station.trim().length() > 100
                || station.contains("://") || station.contains("@")
                || station.indexOf('\n') >= 0 || station.indexOf('\r') >= 0
                || station.indexOf('\t') >= 0) {
            return "Enter the CWC station name shown on the official source.";
        }
        Double now = parseLevel(current);
        Double warn = parseLevel(warning);
        Double dang = parseLevel(danger);
        Double hfl = parseLevel(highestFloodLevel);
        if (now == null || warn == null || dang == null || hfl == null) {
            return "Enter four valid non-negative numeric levels, all in the same unit shown by CWC.";
        }
        if (!(warn <= dang && dang <= hfl)) {
            return "Check the station data: Warning Level must be no higher than Danger Level, and Danger Level no higher than HFL.";
        }
        return null;
    }

    private static Double parseLevel(String value) {
        if (value == null || !value.trim().matches("[0-9]{1,8}(\\.[0-9]{1,4})?")) return null;
        try {
            double parsed = Double.parseDouble(value.trim());
            if (Double.isNaN(parsed) || Double.isInfinite(parsed) || parsed < 0) return null;
            return parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String classify(double current, double warning, double danger, double hfl) {
        if (Double.isNaN(current) || Double.isInfinite(current)
                || Double.isNaN(warning) || Double.isInfinite(warning)
                || Double.isNaN(danger) || Double.isInfinite(danger)
                || Double.isNaN(hfl) || Double.isInfinite(hfl)
                || current < 0 || warning < 0 || danger < warning || hfl < danger) {
            return "Invalid station levels";
        }
        if (current >= hfl) return "Extreme flood situation";
        if (current >= danger) return "Severe flood situation";
        if (current >= warning) return "Above normal flood situation";
        return "Below warning level";
    }

    public static String explanation(String classification) {
        if ("Extreme flood situation".equals(classification)) {
            return "The entered level is at or above the station's Highest Flood Level (HFL).";
        }
        if ("Severe flood situation".equals(classification)) {
            return "The entered level is at or above the Danger Level but remains below HFL.";
        }
        if ("Above normal flood situation".equals(classification)) {
            return "The entered level is at or above the Warning Level but remains below the Danger Level.";
        }
        if ("Below warning level".equals(classification)) {
            return "The entered level is below the station's Warning Level.";
        }
        return "The supplied values cannot be classified.";
    }

    public static String buildResult(String station, String current, String warning,
                                     String danger, String hfl) {
        String classification = classify(
                Double.parseDouble(current), Double.parseDouble(warning),
                Double.parseDouble(danger), Double.parseDouble(hfl));
        String safeStation = station == null ? "" : station.replace((char) 10, ' ')
                .replace((char) 13, ' ').replace((char) 9, ' ').trim();
        return "CWC STATION LEVEL INTERPRETATION\n\n"
                + "Station: " + safeStation + "\n"
                + "Current level entered: " + current + "\n"
                + "Warning Level: " + warning + "\n"
                + "Danger Level: " + danger + "\n"
                + "Highest Flood Level (HFL): " + hfl + "\n\n"
                + "CWC category: " + classification + "\n"
                + explanation(classification) + "\n\n"
                + "Source basis: Central Water Commission flood-situation categories.\n"
                + "Important: this is a calculation from values manually entered by the owner. "
                + "JARVIS has not fetched or verified the live station level. Confirm the station, "
                + "units, timestamp and latest official CWC bulletin. This is not an evacuation order "
                + "or a guarantee of local safety. Follow current instructions from local authorities.";
    }
}
