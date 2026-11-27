package com.jarvis.assistant;

/** Pure allowlist rules for M14 local configuration recovery. */
public final class DiagnosticsRecoveryRules {
    private static final String[] ALLOWED_KEYS = {
            "assistant_mode", "voice_style", "voice_pitch", "voice_name",
            "speech_input_language", "silent_background_ack",
            "dashboard_spacing_profile_v1", "display_accessibility_profile_v1"
    };

    private DiagnosticsRecoveryRules() { }

    public static String[] allowedKeys() { return ALLOWED_KEYS.clone(); }

    public static boolean isAllowedKey(String key) {
        if (key == null) return false;
        for (String allowed : ALLOWED_KEYS) if (allowed.equals(key)) return true;
        return false;
    }

    public static boolean isSupportedValue(Object value) {
        return value instanceof String || value instanceof Boolean
                || value instanceof Integer || value instanceof Long
                || value instanceof Float || value instanceof Double;
    }

    public static boolean isAllowedValue(String key, Object value) {
        if (!isAllowedKey(key) || !isSupportedValue(value)) return false;
        switch (key) {
            case "assistant_mode":
            case "voice_style":
            case "voice_name":
            case "speech_input_language":
                return value instanceof String;
            case "voice_pitch":
                return value instanceof Float || value instanceof Double;
            case "silent_background_ack":
                return value instanceof Boolean;
            case "dashboard_spacing_profile_v1":
            case "display_accessibility_profile_v1":
                return value instanceof Integer;
            default:
                return false;
        }
    }
}
