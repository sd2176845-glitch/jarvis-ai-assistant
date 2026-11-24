package com.jarvis.assistant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Pure progress and revision-plan rules for the existing learning notebook. */
public final class LearningAcademyRules {
    private LearningAcademyRules() { }

    public static final String NOT_STARTED = "Not started";
    public static final String IN_PROGRESS = "In progress";
    public static final String COMPLETED = "Completed";

    /** Returns [total, not-started, in-progress, completed, needs-review]. Null means unreadable. */
    public static int[] progressCounts(String[] statuses) {
        if (statuses == null) return null;
        int[] counts = new int[5];
        counts[0] = statuses.length;
        for (String raw : statuses) {
            String status = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            if (status.equals("not started")) counts[1]++;
            else if (status.equals("in progress")) counts[2]++;
            else if (status.equals("completed")) counts[3]++;
            else counts[4]++;
        }
        return counts;
    }

    /** Revision dates are owner-selected; no notifications or background scheduler are created here. */
    public static String buildProgressSummary(String[] statuses, String[] reviewDates, String today) {
        int[] counts = progressCounts(statuses);
        if (counts == null || reviewDates == null || statuses.length != reviewDates.length
                || !isValidDate(today)) {
            return "Learning progress unavailable: notebook data or date could not be read consistently.";
        }
        Calendar limit = Calendar.getInstance();
        try {
            limit.setTime(parseDate(today));
            limit.add(Calendar.DAY_OF_YEAR, 7);
        } catch (ParseException e) {
            return "Learning progress unavailable: current date could not be read.";
        }
        String sevenDays = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(limit.getTime());
        int reviewDue = 0, reviewNextSeven = 0, invalidReviewDates = 0;
        for (String raw : reviewDates) {
            String date = raw == null ? "" : raw.trim();
            if (date.isEmpty()) continue;
            if (!isValidDate(date)) {
                invalidReviewDates++;
            } else if (date.compareTo(today) <= 0) {
                reviewDue++;
            } else if (date.compareTo(sevenDays) <= 0) {
                reviewNextSeven++;
            }
        }
        return "LEARNING PROGRESS & REVISION\n\n"
                + "Saved lessons: " + counts[0] + "\n"
                + "Not started: " + counts[1] + "\n"
                + "In progress: " + counts[2] + "\n"
                + "Completed: " + counts[3] + "\n"
                + "Needs status review: " + counts[4] + "\n\n"
                + "Revision dates due: " + reviewDue + "\n"
                + "Revision dates in next 7 days: " + reviewNextSeven + "\n"
                + "Invalid revision dates: " + invalidReviewDates + "\n\n"
                + "Progress reflects only the status and revision dates you set in the existing Learning Notebook.";
    }

    public static boolean isValidDate(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return false;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            return value.equals(format.format(format.parse(value)));
        } catch (Exception e) {
            return false;
        }
    }

    private static java.util.Date parseDate(String value) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        return format.parse(value);
    }
}
