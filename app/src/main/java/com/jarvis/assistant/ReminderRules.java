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

    /** Returns a local-calendar bucket for a future reminder without assuming fixed 24-hour days. */
    public static String scheduleGroup(long triggerAtMillis, long nowMillis) {
        java.util.Calendar today = java.util.Calendar.getInstance();
        today.setTimeInMillis(nowMillis);
        today.set(java.util.Calendar.HOUR_OF_DAY, 0);
        today.set(java.util.Calendar.MINUTE, 0);
        today.set(java.util.Calendar.SECOND, 0);
        today.set(java.util.Calendar.MILLISECOND, 0);

        java.util.Calendar target = (java.util.Calendar) today.clone();
        target.setTimeInMillis(triggerAtMillis);
        target.set(java.util.Calendar.HOUR_OF_DAY, 0);
        target.set(java.util.Calendar.MINUTE, 0);
        target.set(java.util.Calendar.SECOND, 0);
        target.set(java.util.Calendar.MILLISECOND, 0);

        if (target.before(today)) return "Past";
        if (sameCalendarDay(today, target)) return "Today";
        java.util.Calendar tomorrow = (java.util.Calendar) today.clone();
        tomorrow.add(java.util.Calendar.DAY_OF_YEAR, 1);
        if (sameCalendarDay(tomorrow, target)) return "Tomorrow";
        java.util.Calendar weekLimit = (java.util.Calendar) today.clone();
        weekLimit.add(java.util.Calendar.DAY_OF_YEAR, 7);
        if (target.before(weekLimit)) return "This week";
        return "Later";
    }

    private static boolean sameCalendarDay(java.util.Calendar left, java.util.Calendar right) {
        return left.get(java.util.Calendar.ERA) == right.get(java.util.Calendar.ERA)
                && left.get(java.util.Calendar.YEAR) == right.get(java.util.Calendar.YEAR)
                && left.get(java.util.Calendar.DAY_OF_YEAR) == right.get(java.util.Calendar.DAY_OF_YEAR);
    }

    public static boolean shouldRestore(long triggerAtMillis, long nowMillis) {
        return isFuture(triggerAtMillis, nowMillis);
    }
}
