package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class OwnerPrivacyRulesTest {
    @Test public void permissionStateIsExplicitAndReadOnly() {
        assertEquals("Granted", OwnerPrivacyRules.permissionState(true));
        assertEquals("Not granted", OwnerPrivacyRules.permissionState(false));
    }

    @Test public void inventoryCountsJsonWithoutExposingItsContents() {
        assertEquals(2, OwnerPrivacyRules.jsonArrayCount("[{\"secret\":\"A\"},{\"secret\":\"B\"}]"));
        assertEquals(0, OwnerPrivacyRules.jsonArrayCount(""));
        assertEquals(-1, OwnerPrivacyRules.jsonArrayCount("not-json"));
        String report = OwnerPrivacyRules.buildInventorySummary(12, 30, 2, 1, 0, 3, 4, 1, true);
        assertTrue(report.contains("Saved notes: 12 characters"));
        assertTrue(report.contains("Structured tasks: 2"));
        assertFalse(report.contains("secret"));
    }

    @Test public void sanitizedExportContainsNoPrivateContent() {
        String report = OwnerPrivacyRules.buildSanitizedExport("Microphone: Granted", "Saved notes: 10 characters");
        assertTrue(report.contains("aggregate local-data counts only"));
        assertFalse(report.contains("private note text"));
        assertFalse(report.contains("phone number"));
    }
}
