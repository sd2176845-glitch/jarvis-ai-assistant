package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class LearningAcademyRulesTest {
    @Test
    public void progressCountsReuseExistingNotebookStatuses() {
        int[] counts = LearningAcademyRules.progressCounts(new String[]{
                "Not started", "In progress", "Completed", "Completed", "", null, "unknown"
        });
        assertNotNull(counts);
        assertArrayEquals(new int[]{7, 1, 1, 2, 3}, counts);
    }

    @Test
    public void progressSummaryCountsRevisionDatesDueAndUpcoming() {
        String summary = LearningAcademyRules.buildProgressSummary(
                new String[]{"Not started", "In progress", "Completed", "Not started", "Mystery"},
                new String[]{"2026-11-23", "2026-11-24", "2026-12-01", "2026-12-02", "bad-date"},
                "2026-11-24");
        assertTrue(summary.contains("Saved lessons: 5"));
        assertTrue(summary.contains("Not started: 2"));
        assertTrue(summary.contains("In progress: 1"));
        assertTrue(summary.contains("Completed: 1"));
        assertTrue(summary.contains("Needs status review: 1"));
        assertTrue(summary.contains("Revision dates due: 2"));
        assertTrue(summary.contains("Revision dates in next 7 days: 1"));
        assertTrue(summary.contains("Invalid revision dates: 1"));
    }

    @Test
    public void progressSummaryHandlesEmptyAndUnreadableData() {
        assertTrue(LearningAcademyRules.buildProgressSummary(
                new String[0], new String[0], "2026-11-24").contains("Saved lessons: 0"));
        assertTrue(LearningAcademyRules.buildProgressSummary(
                null, new String[0], "2026-11-24").contains("unavailable"));
        assertTrue(LearningAcademyRules.buildProgressSummary(
                new String[]{"In progress"}, new String[0], "2026-11-24").contains("unavailable"));
    }

    @Test
    public void revisionDateValidationRejectsImpossibleDates() {
        assertTrue(LearningAcademyRules.isValidDate("2028-02-29"));
        assertFalse(LearningAcademyRules.isValidDate("2027-02-29"));
        assertFalse(LearningAcademyRules.isValidDate("2026-13-01"));
        assertFalse(LearningAcademyRules.isValidDate("not-a-date"));
        assertFalse(LearningAcademyRules.isValidDate(""));
        assertFalse(LearningAcademyRules.isValidDate(null));
    }
}
