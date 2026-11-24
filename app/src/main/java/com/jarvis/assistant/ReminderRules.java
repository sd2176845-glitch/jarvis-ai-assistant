package com.jarvis.assistant;

/** Pure validation rules for owner-created one-time reminders. */
public final class ReminderRules {
    private ReminderRules() { }

    public static String normalizeTitle(String value) {
        if (value == null) return "";
        String title = value.trim().replaceAll("\\s+", " ");
        return title.length() <= 100 ? title : "";
    }

    public static boolean isFuture(long triggerAtMillis, long nowMillis) {
        return triggerAtMillis > nowMillis;
    }

    public static boolean shouldRestore(long triggerAtMillis, long nowMillis) {
        return isFuture(triggerAtMillis, nowMillis);
    }
}
