package com.jarvis.assistant;

/** Safe label helpers for owner-entered Bluetooth accessory inventory entries. */
public final class SmartHomeAccessoryRules {
    private static final String PREFIX = "Bluetooth • ";
    private SmartHomeAccessoryRules() {}

    public static String createBluetoothLabel(String details) {
        if (details == null) return null;
        String value = details.trim();
        if (value.isEmpty() || value.length() > 100) return null;
        return PREFIX + value;
    }

    public static boolean isBluetoothLabel(String label) {
        return label != null && label.startsWith(PREFIX);
    }
}
