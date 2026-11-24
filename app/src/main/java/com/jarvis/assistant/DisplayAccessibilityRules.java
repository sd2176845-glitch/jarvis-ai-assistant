package com.jarvis.assistant;

/** Pure rules for owner-selected, app-local display profiles. */
public final class DisplayAccessibilityRules {
    public static final int STANDARD = 0;
    public static final int LARGE = 1;
    public static final int EXTRA_LARGE = 2;
    public static final int HIGH_CONTRAST = 3;

    private DisplayAccessibilityRules() {}

    public static int normalizeProfile(int profile) {
        return profile >= STANDARD && profile <= HIGH_CONTRAST ? profile : STANDARD;
    }

    public static float statusTextSize(int profile) {
        switch (normalizeProfile(profile)) {
            case LARGE: return 15f;
            case EXTRA_LARGE: return 18f;
            case HIGH_CONTRAST: return 14f;
            default: return 12f;
        }
    }

    public static float transcriptTextSize(int profile) {
        switch (normalizeProfile(profile)) {
            case LARGE: return 18f;
            case EXTRA_LARGE: return 22f;
            case HIGH_CONTRAST: return 17f;
            default: return 15f;
        }
    }

    public static boolean isHighContrast(int profile) {
        return normalizeProfile(profile) == HIGH_CONTRAST;
    }
}
