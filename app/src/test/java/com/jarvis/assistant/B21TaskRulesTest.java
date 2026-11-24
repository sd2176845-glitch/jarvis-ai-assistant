package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class B21TaskRulesTest {
    @Test public void titleMustBePresentAndBounded() {
        assertTrue(B21TaskRules.isValidTitle("Finish homework"));
        assertFalse(B21TaskRules.isValidTitle(" "));
        assertFalse(B21TaskRules.isValidTitle(new String(new char[121]).replace('\0', 'x')));
    }

    @Test public void categoryAndPriorityUseKnownValues() {
        assertTrue(B21TaskRules.isValidCategory("Study"));
        assertFalse(B21TaskRules.isValidCategory("Secret"));
        assertTrue(B21TaskRules.isValidPriority("Urgent"));
        assertFalse(B21TaskRules.isValidPriority("Critical"));
    }

    @Test public void optionalDueDateMustBeRealIsoDate() {
        assertTrue(B21TaskRules.isValidDueDate(""));
        assertTrue(B21TaskRules.isValidDueDate("2026-11-23"));
        assertFalse(B21TaskRules.isValidDueDate("2026-02-30"));
        assertFalse(B21TaskRules.isValidDueDate("23-11-2026"));
    }

    @Test public void overdueUsesLocalCalendarDateAndIgnoresCompletedTasks() {
        long now = System.currentTimeMillis();
        java.util.Calendar local = java.util.Calendar.getInstance();
        local.setTimeInMillis(now);
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(local.getTime());
        local.add(java.util.Calendar.DAY_OF_MONTH, -1);
        String yesterday = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(local.getTime());
        assertTrue(B21TaskRules.isOverdue(yesterday, false, now));
        assertFalse(B21TaskRules.isOverdue(today, false, now));
        assertFalse(B21TaskRules.isOverdue(yesterday, true, now));
        assertFalse(B21TaskRules.isOverdue("not-a-date", false, now));
    }
}
