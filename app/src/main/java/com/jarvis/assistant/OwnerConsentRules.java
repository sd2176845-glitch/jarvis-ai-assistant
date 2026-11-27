package com.jarvis.assistant;

/** Pure M01 consent policy helpers, independent of Android permission grants. */
public final class OwnerConsentRules {
    private OwnerConsentRules() { }

    public static boolean effectiveAccess(boolean ownerConsent, boolean androidPermissionGranted) {
        return ownerConsent && androidPermissionGranted;
    }

    public static boolean defaultConsent() {
        return false;
    }

    public static String stateLabel(boolean consented, boolean permissionGranted) {
        if (!consented) return "Owner consent not given";
        if (!permissionGranted) return "Owner consent given; Android permission not granted";
        return "Owner consent and Android permission granted";
    }
}
