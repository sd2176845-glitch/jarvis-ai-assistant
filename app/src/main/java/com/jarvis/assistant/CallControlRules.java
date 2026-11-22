package com.jarvis.assistant;

/** Allowlist for call-control UI actions; unsupported active windows must be ignored. */
public final class CallControlRules {
    private CallControlRules() { }

    public static boolean isSupportedCallUi(String packageName) {
        return "com.google.android.dialer".equals(packageName)
                || "com.android.dialer".equals(packageName)
                || "com.android.incallui".equals(packageName)
                || "com.android.phone".equals(packageName);
    }
}
