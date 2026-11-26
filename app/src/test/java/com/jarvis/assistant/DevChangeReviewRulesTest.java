package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class DevChangeReviewRulesTest {
    @Test public void summarizesDiffAndRequiresManualReview() {
        String result = DevChangeReviewRules.review("diff --git a/A.java b/A.java\n@@ -1 +1 @@\n-old\n+new");
        assertTrue(result.contains("Added lines: 1"));
        assertTrue(result.contains("Removed lines: 1"));
        assertTrue(result.contains("MANUAL APPROVAL REQUIRED"));
        assertTrue(result.contains("no code was executed"));
    }

    @Test public void flagsPermissionAndSecretPatterns() {
        String result = DevChangeReviewRules.review("+<uses-permission android:name=\"android.permission.RECORD_AUDIO\"/>\n+String api_key = \"REDACTED\";");
        assertTrue(result.contains("permission or access changes"));
        assertTrue(result.contains("credential / secret-like terms"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyDiff() {
        DevChangeReviewRules.review(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOversizedDiff() {
        DevChangeReviewRules.review(new String(new char[5001]).replace('\0', 'x'));
    }
}
