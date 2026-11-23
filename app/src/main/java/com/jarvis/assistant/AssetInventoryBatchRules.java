package com.jarvis.assistant;

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
    public static boolean isValidEdit(String n,String c,String p,String q,String notes,String w) {
        return PracticalIntelligenceRules.validateAssetEntry(n,c,p,q,notes,w)==null;
    }
    private static String safe(String v){return v==null?"":v.trim();}
}