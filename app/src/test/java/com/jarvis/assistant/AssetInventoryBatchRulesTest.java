package com.jarvis.assistant;

import org.junit.Test;

import static org.junit.Assert.*;

public class AssetInventoryBatchRulesTest {
    @Test
    public void categoryAndConditionFiltersMustBothMatch() {
        assertTrue(AssetInventoryBatchRules.matchesFilters("Electronics", " electronics ", "Good", "good"));
        assertFalse(AssetInventoryBatchRules.matchesFilters("Home", "Electronics", "Good", "good"));
        assertFalse(AssetInventoryBatchRules.matchesFilters("Electronics", "", "Needs repair", "Good"));
        assertTrue(AssetInventoryBatchRules.matchesFilters("Home", "", "Good", ""));
    }

    @Test
    public void namesSortCaseInsensitivelyAndNullSafely() {
        assertTrue(AssetInventoryBatchRules.compareNames("apple", "Banana") < 0);
        assertEquals(0, AssetInventoryBatchRules.compareNames(" Lamp ", "lamp"));
        assertTrue(AssetInventoryBatchRules.compareNames(null, "A") < 0);
    }

    @Test
    public void categorySortUsesNameAsTieBreaker() {
        // Electronics comes before Home alphabetically.
        assertTrue(AssetInventoryBatchRules.compareCategoriesThenNames(
                "Electronics", "Phone", "Home", "Lamp") < 0);
        assertTrue(AssetInventoryBatchRules.compareCategoriesThenNames(
                "Home", "Lamp", "Electronics", "Phone") > 0);
        assertTrue(AssetInventoryBatchRules.compareCategoriesThenNames(
                "Home", "Fan", "Home", "Lamp") < 0);
        assertTrue(AssetInventoryBatchRules.compareCategoriesThenNames(
                "Home", "Lamp", "Home", "Fan") > 0);
    }

    @Test
    public void validWarrantyDatesSortBeforeMissingOrInvalid() {
        assertTrue(AssetInventoryBatchRules.compareWarrantyDates("2026-12-01", "2027-01-01") < 0);
        assertTrue(AssetInventoryBatchRules.compareWarrantyDates("2026-12-01", "") < 0);
        assertTrue(AssetInventoryBatchRules.compareWarrantyDates("bad", "2026-12-01") > 0);
    }

    @Test
    public void editValidationReusesExistingInventoryValidation() {
        assertTrue(AssetInventoryBatchRules.isValidEdit("Lamp", "Home", "", "Good", "", "2026-12-31"));
        assertFalse(AssetInventoryBatchRules.isValidEdit("", "Home", "", "Good", "", ""));
        assertFalse(AssetInventoryBatchRules.isValidEdit("Lamp", "Home", "", "Good", "", "2026-02-30"));
    }

    @Test
    public void categorySummaryCountsCaseInsensitivelyAndHandlesMissingCategories() {
        java.util.Map<String, Integer> counts = AssetInventoryBatchRules.countByCategory(
                new String[]{"Home", " Electronics ", "electronics", null, "   "});
        assertEquals(3, counts.size());
        assertEquals(Integer.valueOf(2), counts.get("Electronics"));
        assertEquals(Integer.valueOf(1), counts.get("home"));
        assertEquals(Integer.valueOf(2), counts.get("Uncategorized"));
        assertEquals("Electronics", counts.keySet().iterator().next());
    }

    @Test
    public void categorySummaryHandlesNullAndEmptyInventory() {
        assertTrue(AssetInventoryBatchRules.countByCategory(null).isEmpty());
        assertTrue(AssetInventoryBatchRules.countByCategory(new String[0]).isEmpty());
    }



    @Test
    public void duplicateAssetWarningMatchesTrimmedNameAndCategoryCaseInsensitively() {
        assertTrue(AssetInventoryBatchRules.isSameAsset(
                " Desk Lamp ", "Home", "desk lamp", " home "));
        assertFalse(AssetInventoryBatchRules.isSameAsset(
                "Desk Lamp", "Home", "Floor Lamp", "Home"));
        assertFalse(AssetInventoryBatchRules.isSameAsset(
                "Desk Lamp", "Home", "Desk Lamp", "Electronics"));
    }

    @Test
    public void duplicateAssetRuleDoesNotMatchMissingIdentityFields() {
        assertFalse(AssetInventoryBatchRules.isSameAsset("", "Home", "Lamp", "Home"));
        assertFalse(AssetInventoryBatchRules.isSameAsset("Lamp", "", "Lamp", "Home"));
        assertFalse(AssetInventoryBatchRules.isSameAsset(null, "Home", "Lamp", "Home"));
    }


}
