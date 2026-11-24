package com.jarvis.assistant;

/** Pure formatting helpers for the Android battery broadcast snapshot. */
public final class BatterySnapshotRules {
    private BatterySnapshotRules() {}

    public static String chargingState(int status) {
        switch (status) {
            case 2: return "Charging";
            case 3: return "Discharging";
            case 4: return "Not charging";
            case 5: return "Full";
            default: return "Unknown / unavailable";
        }
    }

    public static String healthLabel(int health) {
        switch (health) {
            case 2: return "Good";
            case 3: return "Overheating reported";
            case 4: return "Battery failure reported";
            case 5: return "Over-voltage reported";
            case 6: return "Unspecified issue reported";
            case 7: return "Low-temperature condition reported";
            default: return "Unknown / unavailable";
        }
    }

    public static String temperatureLabel(int tenthsCelsius) {
        if (tenthsCelsius < 0) return "Unavailable";
        return String.format(java.util.Locale.US, "%.1f °C", tenthsCelsius / 10.0);
    }
}
