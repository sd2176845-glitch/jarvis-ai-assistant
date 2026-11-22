package com.jarvis.assistant;

import org.json.JSONArray;
import org.json.JSONException;

/** Pure, content-free summaries for M01 owner privacy controls. */
public final class OwnerPrivacyRules {
    private OwnerPrivacyRules() { }

    public static String permissionState(boolean granted) {
        return granted ? "Granted" : "Not granted";
    }

    /** Returns -1 for unreadable/missing JSON so corruption is not presented as an empty list. */
    public static int jsonArrayCount(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 0;
        try {
            return new JSONArray(raw).length();
        } catch (JSONException e) {
            return -1;
        }
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
