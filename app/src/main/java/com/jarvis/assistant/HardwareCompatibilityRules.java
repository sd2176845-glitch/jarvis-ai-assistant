package com.jarvis.assistant;

/** Pure formatting helpers for Android-reported hardware compatibility details. */
public final class HardwareCompatibilityRules {
    private HardwareCompatibilityRules() {}

    public static String formatAbiList(String[] abis) {
        if (abis == null || abis.length == 0) return "Unavailable";
        StringBuilder result = new StringBuilder();
        for (String abi : abis) {
            if (abi == null || abi.trim().isEmpty()) continue;
            if (result.length() > 0) result.append(", ");
            result.append(abi.trim());
        }
        return result.length() == 0 ? "Unavailable" : result.toString();
    }
}
