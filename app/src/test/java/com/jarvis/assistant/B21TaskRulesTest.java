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
}
