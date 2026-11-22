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
}
