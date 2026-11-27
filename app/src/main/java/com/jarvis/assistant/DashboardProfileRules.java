package com.jarvis.assistant;

/** Pure layout rules for owner-selected dashboard spacing profiles. */
public final class DashboardProfileRules {
    public static final int COMPACT = 0;
    public static final int STANDARD = 1;
    public static final int SPACIOUS = 2;
    private DashboardProfileRules() {}

    public static int normalize(int profile) {
        return profile >= COMPACT && profile <= SPACIOUS ? profile : STANDARD;
    }

    public static int horizontalPaddingDp(int profile) {
        switch (normalize(profile)) {
            case COMPACT: return 14;
            case SPACIOUS: return 30;
            default: return 22;
        }
    }

    public static int verticalPaddingDp(int profile) {
        switch (normalize(profile)) {
            case COMPACT: return 14;
            case SPACIOUS: return 34;
            default: return 24;
        }
    }

    public static String label(int profile) {
        switch (normalize(profile)) {
            case COMPACT: return "Compact";
            case SPACIOUS: return "Spacious";
            default: return "Standard";
        }
    }
}
