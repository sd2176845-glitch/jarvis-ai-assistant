package com.jarvis.assistant;

import java.util.Map;
import java.util.TreeMap;

/** Pure, deterministic rules for the next M19 inventory capability increment. */
public final class AssetInventoryBatchRules {
    private AssetInventoryBatchRules() {}
    public static boolean matchesFilters(String category, String selectedCategory, String condition, String selectedCondition) {
        return PracticalIntelligenceRules.matchesAssetCategory(category, selectedCategory)
                && PracticalIntelligenceRules.matchesAssetCondition(condition, selectedCondition);
    }
    public static int compareNames(String left, String right) { return safe(left).compareToIgnoreCase(safe(right)); }
    public static int compareCategoriesThenNames(String lc, String ln, String rc, String rn) {
        int c=safe(lc).compareToIgnoreCase(safe(rc)); return c!=0?c:compareNames(ln,rn);
    }
    /** Valid YYYY-MM-DD dates sort earliest first; missing/invalid dates sort last. */
    public static int compareWarrantyDates(String left, String right) {
        boolean lv=PracticalIntelligenceRules.isValidIsoDate(left), rv=PracticalIntelligenceRules.isValidIsoDate(right);
        if(lv&&rv)return left.compareTo(right); if(lv)return -1; if(rv)return 1; return 0;
    }
    /** Case-insensitive read-only category aggregation; blank legacy values are grouped safely. */
    public static Map<String, Integer> countByCategory(String[] categories) {
        Map<String, Integer> counts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (categories == null) return counts;
        for (String category : categories) {
            String label = safe(category);
            if (label.isEmpty()) label = "Uncategorized";
            Integer count = counts.get(label);
            counts.put(label, count == null ? 1 : count + 1);
        }
        return counts;
    }

    /** Case-insensitive read-only condition aggregation; blank legacy values are grouped safely. */
    public static Map<String, Integer> countByCondition(String[] conditions) {
        Map<String, Integer> counts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (conditions == null) return counts;
        for (String condition : conditions) {
            String label = safe(condition);
            if (label.isEmpty()) label = "Unspecified";
            Integer count = counts.get(label);
            counts.put(label, count == null ? 1 : count + 1);
        }
        return counts;
    }

    /** Same normalized item name and category; used only to warn, never to block intentional copies. */
    public static boolean isSameAsset(String existingName, String existingCategory,
                                      String proposedName, String proposedCategory) {
        String oldName = safe(existingName), newName = safe(proposedName);
        String oldCategory = safe(existingCategory), newCategory = safe(proposedCategory);
        return !oldName.isEmpty() && !oldCategory.isEmpty()
                && oldName.equalsIgnoreCase(newName)
                && oldCategory.equalsIgnoreCase(newCategory);
    }

    public static boolean isValidEdit(String n,String c,String p,String q,String notes,String w) {
        return PracticalIntelligenceRules.validateAssetEntry(n,c,p,q,notes,w)==null;
    }
    private static String safe(String v){return v==null?"":v.trim();}
}