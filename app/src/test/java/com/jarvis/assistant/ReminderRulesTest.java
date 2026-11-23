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

    @Test public void restoresOnlyFutureReminders() {
        assertTrue(ReminderRules.shouldRestore(2000L, 1000L));
        assertFalse(ReminderRules.shouldRestore(1000L, 1000L));
    }
}
