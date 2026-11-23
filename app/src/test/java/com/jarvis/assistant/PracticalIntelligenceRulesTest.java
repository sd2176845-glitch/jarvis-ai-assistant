package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class PracticalIntelligenceRulesTest {
    @Test public void emergencyPromptAvoidsPretendingToHaveLiveAlerts() {
        String prompt = PracticalIntelligenceRules.emergencyPrompt("Flood", "Kolkata", "Family checklist");
        assertTrue(prompt.contains("general, calm emergency-preparedness checklist"));
        assertTrue(prompt.contains("Do not claim to know current alerts"));
        assertTrue(prompt.contains("NDMA/IMD"));
        assertTrue(prompt.contains("112"));
        assertTrue(prompt.contains("Do not encourage entering floodwater"));
    }

    @Test public void legalPromptIsInformationalAndDoesNotInventLaw() {
        String prompt = PracticalIntelligenceRules.legalPrompt("Clause 4: payment is due in 30 days.");
        assertTrue(prompt.contains("plain language only"));
        assertTrue(prompt.contains("Do not invent missing clauses, laws"));
        assertTrue(prompt.contains("not legal advice"));
        assertTrue(prompt.contains("qualified professional"));
    }

    @Test public void vehicleRecordValidationRejectsBadLengthsAndOdometer() {
        assertNull(PracticalIntelligenceRules.validateVehicleEntry("Scooter", "2026-11-22", "12000", "Oil service"));
        assertNotNull(PracticalIntelligenceRules.validateVehicleEntry("", "2026-11-22", "12000", "Oil service"));
        assertNotNull(PracticalIntelligenceRules.validateVehicleEntry("Scooter", "Today", "12A", "Oil service"));
        assertNotNull(PracticalIntelligenceRules.validateVehicleEntry("Scooter", "Today", "12000", ""));
    }
    @Test public void assetInventoryValidationAcceptsOptionalPurchaseDateAndNotes() {
        assertNull(PracticalIntelligenceRules.validateAssetEntry(
                "Desk lamp", "Household", "", "Good", ""));
    }

    @Test public void assetInventoryValidationRejectsMissingOrOversizedFields() {
        assertNotNull(PracticalIntelligenceRules.validateAssetEntry(
                "", "Electronics", "", "Working", ""));
        assertNotNull(PracticalIntelligenceRules.validateAssetEntry(
                "Laptop", "", "", "Working", ""));
        assertNotNull(PracticalIntelligenceRules.validateAssetEntry(
                "Laptop", "Electronics", "", "", ""));
        assertNotNull(PracticalIntelligenceRules.validateAssetEntry(
                "Laptop", "Electronics", "", "Working", new String(new char[241]).replace('\0', 'x')));
    }

    @Test public void warrantyDateValidationRequiresARealIsoDate() {
        assertTrue(PracticalIntelligenceRules.isValidIsoDate("2028-02-29"));
        assertFalse(PracticalIntelligenceRules.isValidIsoDate("2027-02-29"));
        assertFalse(PracticalIntelligenceRules.isValidIsoDate("2028-13-01"));
        assertFalse(PracticalIntelligenceRules.isValidIsoDate("28-01-2028"));
        assertFalse(PracticalIntelligenceRules.isValidIsoDate(""));
    }

    @Test public void warrantyReminderTitleIsBoundedAndNormalized() {
        assertEquals("Warranty ending: Laptop",
                PracticalIntelligenceRules.warrantyReminderTitle("  Laptop  "));
        assertEquals("", PracticalIntelligenceRules.warrantyReminderTitle(""));
        assertEquals("", PracticalIntelligenceRules.warrantyReminderTitle(new String(new char[61]).replace((char) 0, 'x')));
    }

    @Test public void warrantyStatusUsesInclusiveExpiryDateAndHandlesMissingData() {
        assertEquals("Active", PracticalIntelligenceRules.warrantyStatus("2026-11-23", "2026-11-23"));
        assertEquals("Active", PracticalIntelligenceRules.warrantyStatus("2026-12-01", "2026-11-23"));
        assertEquals("Expired", PracticalIntelligenceRules.warrantyStatus("2026-11-22", "2026-11-23"));
        assertEquals("Not recorded", PracticalIntelligenceRules.warrantyStatus("", "2026-11-23"));
        assertEquals("Date unavailable", PracticalIntelligenceRules.warrantyStatus("bad-date", "2026-11-23"));
    }

}
