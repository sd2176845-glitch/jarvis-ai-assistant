package com.jarvis.assistant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Calendar-safe recurrence rules for owner-managed routines; no background scheduler. */
public final class M07RoutineRules {
    private M07RoutineRules() { }

    public static boolean isValidCadence(String cadence) {
        return "Daily".equals(cadence) || "Weekly".equals(cadence) || "Monthly".equals(cadence);
    }

    /** Returns the next local calendar date, or null when the supplied date/cadence is invalid. */
    public static String nextDueDate(String dueDate, String cadence, long nowMillis) {
        if (!isValidCadence(cadence)) return null;
        Calendar calendar = Calendar.getInstance();
        if (dueDate == null || dueDate.trim().isEmpty()) {
            calendar.setTimeInMillis(nowMillis);
        } else {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            parser.setLenient(false);
            try {
                Date parsed = parser.parse(dueDate);
                if (parsed == null || !parser.format(parsed).equals(dueDate)) return null;
                calendar.setTime(parsed);
            } catch (ParseException e) {
                return null;
            }
        }
        if ("Daily".equals(cadence)) calendar.add(Calendar.DAY_OF_MONTH, 1);
        else if ("Weekly".equals(cadence)) calendar.add(Calendar.DAY_OF_MONTH, 7);
        else calendar.add(Calendar.MONTH, 1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }
}
