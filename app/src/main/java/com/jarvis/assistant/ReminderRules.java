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

    public static boolean isValidStoredReminder(int id, String title, long triggerAtMillis, long nowMillis) {
        return id > 0 && !normalizeTitle(title).isEmpty()
                && triggerAtMillis > nowMillis;
    }

    public static boolean isSameReminder(int existingId, String existingTitle, long existingTriggerAt,
            int candidateId, String candidateTitle, long candidateTriggerAt) {
        String existing = normalizeTitle(existingTitle);
        String candidate = normalizeTitle(candidateTitle);
        return existingId > 0 && existingId != candidateId && !existing.isEmpty()
                && !candidate.isEmpty() && existing.equalsIgnoreCase(candidate)
                && existingTriggerAt == candidateTriggerAt;
    }

    public static boolean matchesReminderQuery(String title, String query) {
        String normalizedTitle = normalizeTitle(title).toLowerCase(java.util.Locale.ROOT);
        String normalizedQuery = query == null ? "" : query.trim().replaceAll("\\s+", " ")
                .toLowerCase(java.util.Locale.ROOT);
        return normalizedQuery.isEmpty() || normalizedTitle.contains(normalizedQuery);
    }

    public static boolean shouldRestore(long triggerAtMillis, long nowMillis) {
        return isFuture(triggerAtMillis, nowMillis);
    }
}
