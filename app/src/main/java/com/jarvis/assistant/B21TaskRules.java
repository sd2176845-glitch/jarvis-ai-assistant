package com.jarvis.assistant;

/** Validation rules for B21 task enhancements. Existing task storage remains canonical. */
public final class B21TaskRules {
    private B21TaskRules() { }

    public static boolean isValidTitle(String title) {
        return title != null && !title.trim().isEmpty() && title.trim().length() <= 120;
    }

    public static boolean isValidCategory(String category) {
        return oneOf(category, "Personal", "Work", "Study", "Home", "Health", "Other");
    }

    public static boolean isValidPriority(String priority) {
        return oneOf(priority, "Low", "Normal", "High", "Urgent");
    }

    public static boolean isValidDueDate(String value) {
        if (value == null || value.isEmpty()) return true;
        if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return false;
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            format.setLenient(false);
            java.util.Date parsed = format.parse(value);
            return parsed != null && format.format(parsed).equals(value);
        } catch (java.text.ParseException e) {
            return false;
        }
    }

    public static boolean isOverdue(String dueDate, boolean completed, long nowMillis) {
        if (completed || dueDate == null || dueDate.isEmpty() || !isValidDueDate(dueDate)) return false;
        // Due dates are calendar dates entered by the owner, so compare in the
        // device's local timezone rather than converting midnight through UTC.
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date(nowMillis));
        return dueDate.compareTo(today) < 0;
    }

    private static boolean oneOf(String value, String... choices) {
        if (value == null) return false;
        for (String choice : choices) if (choice.equals(value)) return true;
        return false;
    }
}
