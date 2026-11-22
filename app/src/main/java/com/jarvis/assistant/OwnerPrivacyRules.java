package com.jarvis.assistant;

/** Pure, content-free summaries for M01 owner privacy controls. */
public final class OwnerPrivacyRules {
    private OwnerPrivacyRules() { }

    public static String permissionState(boolean granted) {
        return granted ? "Granted" : "Not granted";
    }

    /**
     * Counts top-level array entries without Android's org.json classes so the rule
     * can be unit-tested on the JVM. This is a conservative structural check, not
     * a general-purpose JSON parser; malformed/unbalanced input returns -1.
     */
    public static int jsonArrayCount(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 0;
        String value = raw.trim();
        if (value.length() < 2 || value.charAt(0) != '['
                || value.charAt(value.length() - 1) != ']') return -1;
        int objects = 0;
        int arrays = 0;
        boolean inString = false;
        boolean escaped = false;
        boolean elementStarted = false;
        int count = 0;
        for (int i = 1; i < value.length() - 1; i++) {
            char ch = value.charAt(i);
            if (inString) {
                elementStarted = true;
                if (escaped) escaped = false;
                else if (ch == '\\\\') escaped = true;
                else if (ch == '"') inString = false;
                continue;
            }
            if (ch == '"') {
                inString = true;
                elementStarted = true;
            } else if (ch == '{') {
                objects++;
                elementStarted = true;
            } else if (ch == '}') {
                if (objects == 0) return -1;
                objects--;
            } else if (ch == '[') {
                arrays++;
                elementStarted = true;
            } else if (ch == ']') {
                if (arrays == 0) return -1;
                arrays--;
            } else if (ch == ',' && objects == 0 && arrays == 0) {
                if (!elementStarted) return -1;
                count++;
                elementStarted = false;
            } else if (!Character.isWhitespace(ch)) {
                elementStarted = true;
            }
        }
        if (inString || escaped || objects != 0 || arrays != 0) return -1;
        if (!elementStarted) return count == 0 ? 0 : -1;
        return count + 1;
    }

    public static String countLabel(int count, String itemName) {
        if (count < 0) return itemName + ": unreadable (not changed)";
        return itemName + ": " + count;
    }

    public static String buildInventorySummary(int notesCharacters, int conversationCharacters,
            int tasks, int workflows, int timeline, int graphItems, int ideaVersions,
            int vehicleRecords, boolean encryptedArchivePresent) {
        StringBuilder out = new StringBuilder("LOCAL JARVIS DATA INVENTORY\n");
        out.append("Saved notes: ").append(Math.max(0, notesCharacters)).append(" characters\n");
        out.append("Recent AI context: ").append(Math.max(0, conversationCharacters)).append(" characters\n");
        out.append(countLabel(tasks, "Structured tasks")).append("\n");
        out.append(countLabel(workflows, "Manual workflows")).append("\n");
        out.append(countLabel(timeline, "Personal timeline entries")).append("\n");
        out.append(countLabel(graphItems, "Knowledge graph items")).append("\n");
        out.append(countLabel(ideaVersions, "Idea versions")).append("\n");
        out.append(countLabel(vehicleRecords, "Vehicle service records")).append("\n");
        out.append("Encrypted legacy archive: ").append(encryptedArchivePresent ? "present" : "not present").append("\n");
        out.append("\nCounts only: this report does not display note text, conversation content, names, contacts, messages, or identifiers.");
        return out.toString();
    }

    public static String buildSanitizedExport(String permissionSummary, String inventorySummary) {
        return "JARVIS OWNER PRIVACY SUMMARY\n\n"
                + (permissionSummary == null ? "Permission review unavailable." : permissionSummary)
                + "\n\n"
                + (inventorySummary == null ? "Local data inventory unavailable." : inventorySummary)
                + "\n\nThis export contains permission states and aggregate local-data counts only. "
                + "It excludes saved content, contacts, calls, SMS, account credentials and device identifiers.";
    }
}
