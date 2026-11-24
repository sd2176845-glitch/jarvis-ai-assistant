package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class BuildReportRulesTest {
    @Test public void doesNotInferPassWhenStatusMissing() {
        String report = BuildReportRules.summarize("Gradle task output truncated");
        assertTrue(report.contains("Build: NOT VERIFIED"));
        assertTrue(report.contains("Tests: NOT VERIFIED"));
    }

    @Test public void distinguishesBuildAndTestFailure() {
        String report = BuildReportRules.summarize("BUILD FAILED; 2 tests failed; 1 task skipped");
        assertTrue(report.contains("Build: reported failed"));
        assertTrue(report.contains("Tests: failure wording"));
        assertTrue(report.contains("Skipped steps/tests"));
    }

    @Test public void reportsOnlySuccessWordingActuallyPresent() {
        String report = BuildReportRules.summarize("BUILD SUCCESSFUL; tests passed");
        assertTrue(report.contains("Build: reported successful"));
        assertTrue(report.contains("Tests: success wording"));
    }
}
