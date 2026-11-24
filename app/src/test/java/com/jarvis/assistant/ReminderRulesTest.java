package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReminderRulesTest {
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

    @Test public void restoresOnlyFutureReminders() {
        assertTrue(ReminderRules.shouldRestore(2000L, 1000L));
        assertFalse(ReminderRules.shouldRestore(1000L, 1000L));
    }
}
