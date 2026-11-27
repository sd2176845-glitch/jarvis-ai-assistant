package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReminderRulesTest {
    @Test public void storedReminderValidationRequiresValidFutureRecord() {
        long now = 1000L;
        assertTrue(ReminderRules.isValidStoredReminder(1, "Pay bill", 1001L, now));
        assertFalse(ReminderRules.isValidStoredReminder(0, "Pay bill", 1001L, now));
        assertFalse(ReminderRules.isValidStoredReminder(1, "   ", 1001L, now));
        assertFalse(ReminderRules.isValidStoredReminder(1, "Pay bill", 1000L, now));
    }

    @Test public void trimsAndCollapsesTitleWhitespace() {
        assertEquals("Call the clinic", ReminderRules.normalizeTitle("  Call   the   clinic  "));
    }

    @Test public void rejectsEmptyAndOverlongTitles() {
        assertEquals("", ReminderRules.normalizeTitle("   "));
        assertEquals("", ReminderRules.normalizeTitle(new String(new char[101]).replace('\0', 'x')));
    }

    @Test public void acceptsOnlyFutureReminderTimes() {
        assertTrue(ReminderRules.isFuture(2000L, 1000L));
        assertFalse(ReminderRules.isFuture(1000L, 1000L));
        assertFalse(ReminderRules.isFuture(999L, 1000L));
    }

    @Test public void detectsSameTitleAndExactTimeButAllowsDifferentTimeOrId() {
        assertTrue(ReminderRules.isSameReminder(10, "  Pay   bill ", 5000L,
                11, "pay bill", 5000L));
        assertFalse(ReminderRules.isSameReminder(10, "Pay bill", 5000L,
                10, "Pay bill", 5000L));
        assertFalse(ReminderRules.isSameReminder(10, "Pay bill", 5000L,
                11, "Pay bill", 6000L));
        assertFalse(ReminderRules.isSameReminder(10, "Pay bill", 5000L,
                11, "Call home", 5000L));
    }

    @Test public void reminderSearchMatchesCaseInsensitivelyAndIgnoresExtraSpaces() {
        assertTrue(ReminderRules.matchesReminderQuery("  Pay   Electricity Bill ", "electricity"));
        assertTrue(ReminderRules.matchesReminderQuery("Call Home", "  CALL   "));
        assertTrue(ReminderRules.matchesReminderQuery("Call Home", ""));
        assertFalse(ReminderRules.matchesReminderQuery("Call Home", "clinic"));
    }

    @Test public void groupsRemindersByLocalCalendarDayAndWeek() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        now.clear();
        now.set(2026, java.util.Calendar.NOVEMBER, 27, 10, 0, 0);
        long nowMillis = now.getTimeInMillis();

        java.util.Calendar target = (java.util.Calendar) now.clone();
        target.set(java.util.Calendar.HOUR_OF_DAY, 18);
        assertEquals("Today", ReminderRules.scheduleGroup(target.getTimeInMillis(), nowMillis));

        target.add(java.util.Calendar.DAY_OF_YEAR, 1);
        assertEquals("Tomorrow", ReminderRules.scheduleGroup(target.getTimeInMillis(), nowMillis));

        target.add(java.util.Calendar.DAY_OF_YEAR, 2);
        assertEquals("This week", ReminderRules.scheduleGroup(target.getTimeInMillis(), nowMillis));

        target.add(java.util.Calendar.DAY_OF_YEAR, 5);
        assertEquals("Later", ReminderRules.scheduleGroup(target.getTimeInMillis(), nowMillis));

        target.add(java.util.Calendar.DAY_OF_YEAR, -9);
        assertEquals("Past", ReminderRules.scheduleGroup(target.getTimeInMillis(), nowMillis));
    }

    @Test public void restoresOnlyFutureReminders() {
        assertTrue(ReminderRules.shouldRestore(2000L, 1000L));
        assertFalse(ReminderRules.shouldRestore(1000L, 1000L));
    }
}
