package com.jarvis.assistant;

import java.util.Locale;

/** Local, advisory review of pasted unified diffs. Never executes or applies code. */
public final class DevChangeReviewRules {
    private DevChangeReviewRules() {}

    public static String review(String diff) {
        if (diff == null || diff.trim().isEmpty()) throw new IllegalArgumentException("Paste a change diff first.");
        if (diff.length() > 5000) throw new IllegalArgumentException("Keep the diff under 5,000 characters.");
        String lower = diff.toLowerCase(Locale.ROOT);
        int additions = 0, deletions = 0;
        String[] lines = diff.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("+++") || line.startsWith("---")) continue;
            if (line.startsWith("+")) additions++;
            else if (line.startsWith("-")) deletions++;
        }
        StringBuilder out = new StringBuilder("PATCH REVIEW • MANUAL APPROVAL REQUIRED")
                .append("\nAdded lines: ").append(additions)
                .append("\nRemoved lines: ").append(deletions)
                .append("\nDiff markers: ").append((lower.contains("diff --git") || lower.contains("@@"))
                        ? "detected" : "not detected • input may be an excerpt")
                .append("\n\nREVIEW FLAGS");
        boolean flagged = false;
        flagged |= flag(out, lower, "credential / secret-like terms", "api_key", "apikey", "password", "client_secret", "access_token", "private_key");
        flagged |= flag(out, lower, "permission or access changes", "uses-permission", "requestpermissions", "system_alert_window", "accessibilityservice", "manage_external_storage");
        flagged |= flag(out, lower, "process or shell execution", "runtime.getruntime", "processbuilder", "exec(");
        flagged |= flag(out, lower, "destructive file operations", "delete_recursively", "deleteall", "rm -rf", "recursive delete");
        flagged |= flag(out, lower, "network or endpoint changes", "http://", "https://", "setrequestproperty", "urlconnection");
        flagged |= flag(out, lower, "background or persistent execution", "startforeground", "foregroundservice", "boot_completed", "workmanager");
        if (!flagged) out.append("\n• No configured high-attention patterns detected; this is not a security guarantee.");
        out.append("\n\nMANUAL CHECKLIST")
                .append("\n[ ] Read the complete diff and surrounding code.")
                .append("\n[ ] Confirm every permission, network destination and data flow is necessary.")
                .append("\n[ ] Check error handling, privacy, accessibility and backward compatibility.")
                .append("\n[ ] Run unit tests, build and relevant security checks in an isolated environment.")
                .append("\n[ ] Keep a backup and rollback path before applying changes.")
                .append("\n\nAdvisory text review only • no code was executed, applied or verified.");
        return out.toString();
    }

    private static boolean flag(StringBuilder out, String text, String label, String... patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                out.append("\n• Review ").append(label).append(" (matched: ").append(pattern).append(").");
                return true;
            }
        }
        return false;
    }
}
