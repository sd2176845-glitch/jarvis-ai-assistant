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

    @Test public void warrantyStatusUsesInclusiveExpiryDateAndHandlesMissingData() {
        assertEquals("Active", PracticalIntelligenceRules.warrantyStatus("2026-11-23", "2026-11-23"));
        assertEquals("Active", PracticalIntelligenceRules.warrantyStatus("2026-12-01", "2026-11-23"));
        assertEquals("Expired", PracticalIntelligenceRules.warrantyStatus("2026-11-22", "2026-11-23"));
        assertEquals("Not recorded", PracticalIntelligenceRules.warrantyStatus("", "2026-11-23"));
        assertEquals("Date unavailable", PracticalIntelligenceRules.warrantyStatus("bad-date", "2026-11-23"));
    }


    @Test public void inventorySearchIsCaseInsensitiveAndLocalRuleHandlesEmptyQuery() {
        assertTrue(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Household", "Good", "Warm white", " LAMP "));
        assertTrue(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Household", "Good", "", ""));
        assertFalse(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Household", "Good", "", "camera"));
    }

    @Test public void inventoryCategoryAndConditionFiltersAreOptionalAndCaseInsensitive() {
        assertTrue(PracticalIntelligenceRules.matchesAssetCategory("Electronics", " electronics "));
        assertFalse(PracticalIntelligenceRules.matchesAssetCategory("Household", "Electronics"));
        assertTrue(PracticalIntelligenceRules.matchesAssetCategory("Household", ""));
        assertTrue(PracticalIntelligenceRules.matchesAssetCondition("Needs Repair", "needs repair"));
        assertFalse(PracticalIntelligenceRules.matchesAssetCondition("Good", "Needs Repair"));
    }

    @Test public void warrantyCountdownAndExpiringWindowAreCalendarBased() {
        assertEquals(0L, PracticalIntelligenceRules.warrantyDaysRemaining("2026-11-23", "2026-11-23"));
        assertEquals(7L, PracticalIntelligenceRules.warrantyDaysRemaining("2026-11-30", "2026-11-23"));
        assertEquals(-1L, PracticalIntelligenceRules.warrantyDaysRemaining("2026-11-22", "2026-11-23"));
        assertEquals(Long.MIN_VALUE, PracticalIntelligenceRules.warrantyDaysRemaining("", "2026-11-23"));
        assertEquals(Long.MIN_VALUE, PracticalIntelligenceRules.warrantyDaysRemaining("2026-11-22", "bad-date"));
        assertTrue(PracticalIntelligenceRules.isWarrantyExpiringWithin("2026-11-30", "2026-11-23", 7));
        assertFalse(PracticalIntelligenceRules.isWarrantyExpiringWithin("2026-12-01", "2026-11-23", 7));
        assertFalse(PracticalIntelligenceRules.isWarrantyExpiringWithin("bad", "2026-11-23", 30));
    }

    @Test public void warrantyCountsIgnoreInvalidTodayAndCountKnownStatuses() {
        assertEquals(1, PracticalIntelligenceRules.countAssetsWithWarrantyStatus(
                new String[]{"2026-12-01", "2026-11-01", "", "bad"}, "2026-11-23", "Active"));
        assertEquals(1, PracticalIntelligenceRules.countAssetsWithWarrantyStatus(
                new String[]{"2026-12-01", "2026-11-01", "", "bad"}, "2026-11-23", "Expired"));
        assertEquals(0, PracticalIntelligenceRules.countAssetsWithWarrantyStatus(
                new String[]{"2026-12-01"}, "bad", "Active"));
    }

    @Test public void inventorySummaryRejectsImpossibleCounts() {
        assertTrue(PracticalIntelligenceRules.inventorySummary(5, 2, 1, 2).contains("Total: 5"));
        assertEquals("Inventory summary unavailable",
                PracticalIntelligenceRules.inventorySummary(2, 2, 1, 0));
        assertEquals("Inventory summary unavailable",
                PracticalIntelligenceRules.inventorySummary(-1, 0, 0, 0));
    }

    @Test public void assetExportSanitizesLineBreaksAndSeparators() {
        assertEquals("Lamp / spare | Home | Good | 2026-12-31",
                PracticalIntelligenceRules.safeAssetExportLine("Lamp | spare\\n", "Home",
                        "Good", "2026-12-31"));
    }


    @Test
    public void assetExportSanitizesActualLineBreaksTabsAndSeparators() {
        assertEquals("Lamp / spare shelf | Home shelf | Needs repair | 2026-12-31",
                PracticalIntelligenceRules.safeAssetExportLine(
                        "Lamp | spare\r\nshelf", "Home\tshelf", "Needs\nrepair", "2026-12-31"));
    }

    @Test
    public void assetExportSanitizesEscapedLineBreakSequences() {
        assertEquals("Lamp Shelf | Home | Good | 2026-12-31",
                PracticalIntelligenceRules.safeAssetExportLine(
                        "Lamp\\nShelf", "Home", "Good", "2026-12-31"));
    }


    @Test
    public void assetSearchMatchesPurchaseAndWarrantyDateFields() {
        assertTrue(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Home", "Good", "", "2024-02-15",
                "2026-12-31", "2024-02"));
        assertTrue(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Home", "Good", "", "2024-02-15",
                "2026-12-31", "12-31"));
        assertFalse(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Home", "Good", "", "2024-02-15",
                "2026-12-31", "2025"));
    }

    @Test
    public void assetSearchDateFieldsRemainCaseInsensitiveAndOptional() {
        assertTrue(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Home", "Good", "", "", "", " lamp "));
        assertTrue(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Home", "Good", "", "", "", ""));
        assertFalse(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Home", "Good", "", "", "", "warranty"));
        assertTrue(PracticalIntelligenceRules.matchesAssetQuery(
                "Desk Lamp", "Home", "Good", "warm white", "2024-01-01",
                "2026-12-31", "WARM"));
    }



    @Test public void vehicleOdometerAuditDetectsDecreaseOnlyWithinSameVehicleChronology() {
        int[] result = PracticalIntelligenceRules.vehicleOdometerAudit(
                new String[]{"Scooter", "Scooter", "Car", "Scooter", "Car"},
                new String[]{"2026-01-01", "2026-02-01", "2026-01-01", "2026-03-01", "2026-02-01"},
                new String[]{"10000", "9000", "50000", "11000", "51000"});
        assertEquals(5, result[0]);
        assertEquals(3, result[1]);
        assertEquals(1, result[2]);
    }

    @Test public void vehicleOdometerAuditIgnoresInvalidMissingAndSameDayValues() {
        int[] result = PracticalIntelligenceRules.vehicleOdometerAudit(
                new String[]{"Scooter", "Scooter", "Scooter", "", "Scooter", "Scooter"},
                new String[]{"2026-01-01", "2026-01-01", "bad-date", "2026-02-01", "2026-02-01", "2026-03-01"},
                new String[]{"10000", "9000", "8000", "7000", "", "11000"});
        assertEquals(3, result[0]);
        assertEquals(0, result[1]);
        assertEquals(0, result[2]);
    }

    @Test public void vehicleOdometerAuditSkipsEntireAmbiguousDates() {
        int[] result = PracticalIntelligenceRules.vehicleOdometerAudit(
                new String[]{"Scooter", "Scooter", "Scooter", "Scooter"},
                new String[]{"2026-01-01", "2026-02-01", "2026-02-01", "2026-03-01"},
                new String[]{"10000", "12000", "8000", "9000"});
        assertEquals(4, result[0]);
        assertEquals(1, result[1]);
        assertEquals(1, result[2]);
    }

    @Test public void vehicleOdometerAuditHandlesNullAndMismatchedArrays() {
        assertEquals(0, PracticalIntelligenceRules.vehicleOdometerAudit(null, null, null)[0]);
        assertEquals(0, PracticalIntelligenceRules.vehicleOdometerAudit(
                new String[]{"Scooter"}, new String[0], new String[]{"100"})[0]);
    }


    @Test public void vehicleNextServiceDueDateIsOptionalButMustBeRealIsoDate() {
        assertNull(PracticalIntelligenceRules.validateVehicleEntry(
                "Family car", "2026-11-01", "12000", "Routine service", ""));
        assertNull(PracticalIntelligenceRules.validateVehicleEntry(
                "Family car", "2026-11-01", "12000", "Routine service", "2026-12-24"));
        assertNotNull(PracticalIntelligenceRules.validateVehicleEntry(
                "Family car", "2026-11-01", "12000", "Routine service", "2026-02-30"));
        assertNotNull(PracticalIntelligenceRules.validateVehicleEntry(
                "Family car", "2026-11-01", "12000", "Routine service", "next month"));
    }

    @Test public void vehicleServiceDueSummaryBucketsOverdueTodayNextThirtyAndLater() {
        String report = PracticalIntelligenceRules.vehicleServiceDueSummary(
                new String[]{"Family car", "Family scooter", "Shared van", "Household car", "Bad date", "No date"},
                new String[]{"2026-11-23", "2026-11-24", "2026-12-24", "2026-12-25", "2026-02-30", ""},
                "2026-11-24");
        assertTrue(report.contains("Valid dates recorded: 4"));
        assertTrue(report.contains("Overdue: 1 • Due today: 1 • Next 30 days: 1 • Later: 1"));
        assertTrue(report.contains("Dates needing correction: 1"));
        assertTrue(report.contains("OVERDUE • Family car"));
        assertTrue(report.contains("DUE TODAY • Family scooter"));
        assertTrue(report.contains("NEXT 30 DAYS • Shared van"));
        assertTrue(report.contains("not a mechanical diagnosis"));
    }

    @Test public void vehicleServiceDueSummaryHandlesEmptyAndInvalidInputs() {
        String empty = PracticalIntelligenceRules.vehicleServiceDueSummary(
                new String[0], new String[0], "2026-11-24");
        assertTrue(empty.contains("Valid dates recorded: 0"));
        assertTrue(empty.contains("No valid service/review dates are due or overdue"));
        assertTrue(PracticalIntelligenceRules.vehicleServiceDueSummary(
                null, null, "2026-11-24").contains("overview unavailable"));
        assertTrue(PracticalIntelligenceRules.vehicleServiceDueSummary(
                new String[]{"Car"}, new String[0], "2026-11-24").contains("overview unavailable"));
        assertTrue(PracticalIntelligenceRules.vehicleServiceDueSummary(
                new String[]{"Car"}, new String[]{"2026-12-01"}, "bad-date").contains("overview unavailable"));
    }


    @Test public void journeyPlanRequiresGuardianReviewAndAvoidsBookings() {
        String prompt = PracticalIntelligenceRules.journeyPlanPrompt("Family visit to a nearby city");
        assertTrue(prompt.contains("family/guardian-reviewed"));
        assertTrue(prompt.contains("Do not book, reserve, purchase"));
        assertTrue(prompt.contains("parent/guardian approval and accompaniment"));
    }

    @Test public void collaborativeProjectPromptUsesOwnerCuratedMilestonesWithoutDuplicateStores() {
        String prompt = PracticalIntelligenceRules.collaborativeProjectPrompt("School science project with three roles");
        assertTrue(prompt.contains("owner-editable milestones"));
        assertTrue(prompt.contains("Do not claim that collaborators were invited"));
        assertTrue(prompt.contains("existing Tasks & Reminders / Project Workspace"));
    }

}
