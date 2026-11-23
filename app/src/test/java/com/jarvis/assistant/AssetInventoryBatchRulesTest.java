package com.jarvis.assistant;
import org.junit.Test;
import static org.junit.Assert.*;
public class AssetInventoryBatchRulesTest {
 @Test public void categoryAndConditionFiltersMustBothMatch(){
  assertTrue(AssetInventoryBatchRules.matchesFilters("Electronics"," electronics ","Good","good"));
  assertFalse(AssetInventoryBatchRules.matchesFilters("Home","Electronics","Good","good"));
  assertFalse(AssetInventoryBatchRules.matchesFilters("Electronics","","Needs repair","Good"));
  assertTrue(AssetInventoryBatchRules.matchesFilters("Home","","Good",""));
 }
 @Test public void namesSortCaseInsensitivelyAndNullSafely(){
  assertTrue(AssetInventoryBatchRules.compareNames("apple","Banana")<0);
  assertEquals(0,AssetInventoryBatchRules.compareNames(" Lamp ","lamp"));
  assertTrue(AssetInventoryBatchRules.compareNames(null,"A")<0);
 }
 @Test public void categorySortUsesNameAsTieBreaker(){
  assertTrue(AssetInventoryBatchRules.compareCategoriesThenNames("Electronics","Phone","Home","Lamp")>0);
  assertTrue(AssetInventoryBatchRules.compareCategoriesThenNames("Home","Fan","Home","Lamp")<0);
 }
 @Test public void validWarrantyDatesSortBeforeMissingOrInvalid(){
  assertTrue(AssetInventoryBatchRules.compareWarrantyDates("2026-12-01","2027-01-01")<0);
  assertTrue(AssetInventoryBatchRules.compareWarrantyDates("2026-12-01","")<0);
  assertTrue(AssetInventoryBatchRules.compareWarrantyDates("bad","2026-12-01")>0);
 }
 @Test public void editValidationReusesExistingInventoryValidation(){
  assertTrue(AssetInventoryBatchRules.isValidEdit("Lamp","Home","","Good","","2026-12-31"));
  assertFalse(AssetInventoryBatchRules.isValidEdit("","Home","","Good","",""));
 }
}