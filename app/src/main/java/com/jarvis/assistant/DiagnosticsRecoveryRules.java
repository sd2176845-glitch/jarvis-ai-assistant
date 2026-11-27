package com.jarvis.assistant;

/** Pure allowlist rules for M14 local configuration recovery. */
public final class DiagnosticsRecoveryRules {
    private DiagnosticsRecoveryRules() { }

    public static boolean isSupportedValue(Object value) {
        return value instanceof String || value instanceof Boolean
                || value instanceof Integer || value instanceof Long
                || value instanceof Float || value instanceof Double;
    }

    public static boolean isAllowedKey(String key) {
        if (key == null) return false;
        switch (key) {
            case "assistant_mode":
            case "voice_style":
            case "voice_pitch":
            case "voice_name":
            case "speech_input_language":
            case "silent_background_ack":
            case "dashboard_spacing_profile_v1":
            case "display_accessibility_profile_v1":
                return true;
            default:
                return false;
        }
    }
}
