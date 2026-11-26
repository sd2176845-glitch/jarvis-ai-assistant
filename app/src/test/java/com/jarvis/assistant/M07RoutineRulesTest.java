package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class M07RoutineRulesTest {
    @Test public void acceptsOnlySupportedCadences() {
        assertTrue(M07RoutineRules.isValidCadence("Daily"));
        assertTrue(M07RoutineRules.isValidCadence("Weekly"));
        assertTrue(M07RoutineRules.isValidCadence("Monthly"));
        assertFalse(M07RoutineRules.isValidCadence("Hourly"));
    }

    @Test public void advancesDailyAndWeeklyCalendarDates() {
        long now = 0L;
        assertEquals("1970-01-02", M07RoutineRules.nextDueDate("1970-01-01", "Daily", now));
        assertEquals("1970-01-08", M07RoutineRules.nextDueDate("1970-01-01", "Weekly", now));
    }

    @Test public void monthlyDateClampsAtMonthEnd() {
        assertEquals("2026-02-28", M07RoutineRules.nextDueDate("2026-01-31", "Monthly", 0L));
    }

    @Test public void blankDateStartsFromLocalTodayAndInvalidDateFailsClosed() {
        String expected = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date(System.currentTimeMillis() + 24L * 60L * 60L * 1000L));
        assertEquals(expected, M07RoutineRules.nextDueDate("", "Daily", System.currentTimeMillis()));
        assertNull(M07RoutineRules.nextDueDate("2026-02-30", "Daily", 0L));
        assertNull(M07RoutineRules.nextDueDate("2026-01-01", "Hourly", 0L));
    }
}
