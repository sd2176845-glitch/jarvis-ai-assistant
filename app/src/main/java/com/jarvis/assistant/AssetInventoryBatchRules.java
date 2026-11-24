package com.jarvis.assistant;

import java.util.ArrayList;
import java.util.List;
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
    /** Case-insensitive condition order with name as a stable tie-breaker. */
    public static int compareConditionsThenNames(String leftCondition, String leftName,
                                                 String rightCondition, String rightName) {
        int conditionOrder = safe(leftCondition).compareToIgnoreCase(safe(rightCondition));
        return conditionOrder != 0 ? conditionOrder : compareNames(leftName, rightName);
    }

    /** Valid YYYY-MM-DD warranty dates sort earliest first; missing/invalid dates sort last. */
    public static int compareWarrantyDates(String left, String right) {
        String l = safe(left), r = safe(right);
        boolean lv = PracticalIntelligenceRules.isValidIsoDate(l);
        boolean rv = PracticalIntelligenceRules.isValidIsoDate(r);
        if (lv && rv) return l.compareTo(r);
        if (lv) return -1;
        if (rv) return 1;
        return 0;
    }

    /** Valid YYYY-MM-DD purchase dates sort newest first; missing/invalid dates stay last. */
    public static int comparePurchaseDatesNewestFirst(String left, String right) {
        String l = safe(left), r = safe(right);
        boolean lv = PracticalIntelligenceRules.isValidIsoDate(l);
        boolean rv = PracticalIntelligenceRules.isValidIsoDate(r);
        if (lv && rv) return r.compareTo(l);
        if (lv) return -1;
        if (rv) return 1;
        return 0;
    }

    /** Validates an optional inclusive purchase-date range. Blank bounds are open-ended. */
    public static boolean isValidPurchaseDateRange(String fromIsoDate, String toIsoDate) {
        String from = safe(fromIsoDate), to = safe(toIsoDate);
        if (!from.isEmpty() && !PracticalIntelligenceRules.isValidIsoDate(from)) return false;
        if (!to.isEmpty() && !PracticalIntelligenceRules.isValidIsoDate(to)) return false;
        return from.isEmpty() || to.isEmpty() || from.compareTo(to) <= 0;
    }

    /** Matches an existing purchase date against an inclusive range; active ranges exclude invalid dates. */
    public static boolean matchesPurchaseDateRange(String purchaseDate,
                                                   String fromIsoDate, String toIsoDate) {
        String purchase = safe(purchaseDate);
        String from = safe(fromIsoDate), to = safe(toIsoDate);
        if (from.isEmpty() && to.isEmpty()) return true;
        if (!isValidPurchaseDateRange(from, to)
                || !PracticalIntelligenceRules.isValidIsoDate(purchase)) return false;
        return (from.isEmpty() || purchase.compareTo(from) >= 0)
                && (to.isEmpty() || purchase.compareTo(to) <= 0);
    }
    /** Applies the selected warranty status without storing or mutating any inventory data. */
    public static boolean matchesWarrantyStatus(String warrantyUntil, String todayIsoDate,
                                                String selectedStatus) {
        String selected = safe(selectedStatus);
        if (selected.isEmpty() || "All".equalsIgnoreCase(selected)) return true;
        return selected.equalsIgnoreCase(
                PracticalIntelligenceRules.warrantyStatus(warrantyUntil, todayIsoDate));
    }

    /** Returns original record indexes whose valid warranties expire inclusively within the window. */
    public static List<Integer> warrantyIndexesWithinWindow(String[] warrantyDates,
                                                              String todayIsoDate, int days) {
        List<Integer> indexes = new ArrayList<>();
        if (warrantyDates == null || days < 0
                || !PracticalIntelligenceRules.isValidIsoDate(todayIsoDate)) return indexes;
        for (int i = 0; i < warrantyDates.length; i++) {
            if (PracticalIntelligenceRules.isWarrantyExpiringWithin(
                    warrantyDates[i], todayIsoDate, days)) indexes.add(i);
        }
        return indexes;
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

    /** Read-only integrity audit for required legacy fields and malformed optional warranty dates. */
    public static List<String> inventoryDataQualityIssues(String[] names, String[] categories,
                                                          String[] conditions, String[] warrantyDates) {
        List<String> issues = new ArrayList<>();
        if (names == null || categories == null || conditions == null || warrantyDates == null
                || names.length != categories.length || names.length != conditions.length
                || names.length != warrantyDates.length) {
            issues.add("Audit unavailable: inventory fields could not be read consistently.");
            return issues;
        }
        for (int i = 0; i < names.length; i++) {
            List<String> fields = new ArrayList<>();
            if (safe(names[i]).isEmpty()) fields.add("missing name");
            if (safe(categories[i]).isEmpty()) fields.add("missing category");
            if (safe(conditions[i]).isEmpty()) fields.add("missing condition");
            String warranty = safe(warrantyDates[i]);
            if (!warranty.isEmpty() && !PracticalIntelligenceRules.isValidIsoDate(warranty))
                fields.add("invalid warranty date");
            if (!fields.isEmpty()) {
                StringBuilder line = new StringBuilder("Item ").append(i + 1).append(": ");
                for (String field : fields) {
                    if (line.charAt(line.length() - 1) != ' ') line.append(", ");
                    line.append(field);
                }
                issues.add(line.toString());
            }
        }
        return issues;
    }

    /** Read-only purchase-age buckets. Dates are owner-entered ISO dates; no value is inferred. */
    public static Map<String, Integer> purchaseAgeSummary(String[] purchaseDates, String todayIsoDate) {
        Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        counts.put("Under 1 year", 0);
        counts.put("1–2 years", 0);
        counts.put("2–5 years", 0);
        counts.put("5+ years", 0);
        counts.put("Date unavailable", 0);
        counts.put("Future date", 0);
        if (purchaseDates == null) return counts;
        boolean validToday = PracticalIntelligenceRules.isValidIsoDate(todayIsoDate);
        for (String raw : purchaseDates) {
            String date = raw == null ? "" : raw.trim();
            String bucket;
            if (!validToday || !PracticalIntelligenceRules.isValidIsoDate(date)) {
                bucket = "Date unavailable";
            } else if (date.compareTo(todayIsoDate) > 0) {
                bucket = "Future date";
            } else {
                int years = Integer.parseInt(todayIsoDate.substring(0, 4))
                        - Integer.parseInt(date.substring(0, 4));
                String todayMonthDay = todayIsoDate.substring(5);
                String purchaseMonthDay = date.substring(5);
                if (todayMonthDay.compareTo(purchaseMonthDay) < 0) years--;
                if (years < 1) bucket = "Under 1 year";
                else if (years < 2) bucket = "1–2 years";
                else if (years < 5) bucket = "2–5 years";
                else bucket = "5+ years";
            }
            counts.put(bucket, counts.get(bucket) + 1);
        }
        return counts;
    }

    public static boolean isValidEdit(String n,String c,String p,String q,String notes,String w) {
        return PracticalIntelligenceRules.validateAssetEntry(n,c,p,q,notes,w)==null;
    }
    private static String safe(String v){return v==null?"":v.trim();}
}