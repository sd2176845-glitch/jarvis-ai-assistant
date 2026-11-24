package com.jarvis.assistant;

import java.util.Locale;

/** Conservative parser for owner-pasted CI summaries; never infers a pass from missing output. */
public final class BuildReportRules {
    private BuildReportRules() { }

    public static String summarize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "No report text supplied. Build and test status: NOT VERIFIED.";
        }
        String text = raw.toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder("BUILD & TEST REPORT • PASTED TEXT ONLY\n\n");
        if (text.contains("build successful") || text.contains("build success")) {
            out.append("Build: reported successful in pasted text.\n");
        } else if (text.contains("build failed") || text.contains("build failure")) {
            out.append("Build: reported failed in pasted text.\n");
        } else {
            out.append("Build: NOT VERIFIED from supplied text.\n");
        }
        if (text.contains("tests passed") || text.contains("test successful") || text.contains("tests successful")) {
            out.append("Tests: success wording appears in pasted text; exact test scope is not independently verified.\n");
        } else if (text.contains("tests failed") || text.contains("test failed")) {
            out.append("Tests: failure wording appears in pasted text.\n");
        } else {
            out.append("Tests: NOT VERIFIED from supplied text.\n");
        }
        if (text.contains("skipped")) out.append("Skipped steps/tests: mentioned in pasted text; inspect the original run for details.\n");
        if (text.contains("cancelled") || text.contains("canceled")) out.append("Cancellation: mentioned in pasted text.\n");
        out.append("\nSource: owner-pasted text only. JARVIS did not query GitHub, execute a build or run tests. Missing output is never treated as a pass.");
        return out.toString();
    }
}
