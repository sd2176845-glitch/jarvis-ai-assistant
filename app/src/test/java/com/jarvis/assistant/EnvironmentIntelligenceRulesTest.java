package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class EnvironmentIntelligenceRulesTest {
    @Test
    public void aqiCategoriesMatchCpcbBreakpoints() {
        assertEquals("Good", EnvironmentIntelligenceRules.categoryForAqi(0));
        assertEquals("Good", EnvironmentIntelligenceRules.categoryForAqi(50));
        assertEquals("Satisfactory", EnvironmentIntelligenceRules.categoryForAqi(51));
        assertEquals("Satisfactory", EnvironmentIntelligenceRules.categoryForAqi(100));
        assertEquals("Moderate", EnvironmentIntelligenceRules.categoryForAqi(101));
        assertEquals("Moderate", EnvironmentIntelligenceRules.categoryForAqi(200));
        assertEquals("Poor", EnvironmentIntelligenceRules.categoryForAqi(201));
        assertEquals("Poor", EnvironmentIntelligenceRules.categoryForAqi(300));
        assertEquals("Very Poor", EnvironmentIntelligenceRules.categoryForAqi(301));
        assertEquals("Very Poor", EnvironmentIntelligenceRules.categoryForAqi(400));
        assertEquals("Severe", EnvironmentIntelligenceRules.categoryForAqi(401));
        assertEquals("Severe", EnvironmentIntelligenceRules.categoryForAqi(500));
        assertEquals("Out of range", EnvironmentIntelligenceRules.categoryForAqi(-1));
        assertEquals("Out of range", EnvironmentIntelligenceRules.categoryForAqi(501));
    }

    @Test
    public void manualObservationRequiresBroadAreaValidValueAndLocalTimestamp() {
        assertNull(EnvironmentIntelligenceRules.validateObservation(
                "Kolkata / Victoria Memorial", "87", "2026-11-24 15:30"));
        assertNotNull(EnvironmentIntelligenceRules.validateObservation("", "87", "2026-11-24 15:30"));
        assertNotNull(EnvironmentIntelligenceRules.validateObservation("https://example.com", "87", "2026-11-24 15:30"));
        assertNotNull(EnvironmentIntelligenceRules.validateObservation("Kolkata", "501", "2026-11-24 15:30"));
        assertNotNull(EnvironmentIntelligenceRules.validateObservation("Kolkata", "bad", "2026-11-24 15:30"));
        assertNotNull(EnvironmentIntelligenceRules.validateObservation("Kolkata", "87", "2026-02-30 15:30"));
        assertNotNull(EnvironmentIntelligenceRules.validateObservation("Kolkata", "87", "2026-11-24 25:30"));
    }

    @Test
    public void summaryCountsLocationReadingsWithoutAveragingThem() {
        String report = EnvironmentIntelligenceRules.buildSummary(
                new String[]{"Kolkata", "Malda", "Delhi", "Mumbai", "Chennai", "Jaipur", "Broken"},
                new int[]{34, 90, 150, 240, 350, 450, 501},
                new String[]{"2026-11-24 10:00", "2026-11-24 10:00", "2026-11-24 10:00",
                        "2026-11-24 10:00", "2026-11-24 10:00", "2026-11-24 10:00", "bad"});
        assertTrue(report.contains("Valid observations: 6"));
        assertTrue(report.contains("Good: 1"));
        assertTrue(report.contains("Satisfactory: 1"));
        assertTrue(report.contains("Moderate: 1"));
        assertTrue(report.contains("Poor: 1"));
        assertTrue(report.contains("Very Poor: 1"));
        assertTrue(report.contains("Severe: 1"));
        assertTrue(report.contains("Entries needing review: 1"));
        assertTrue(report.contains("not averaged"));
        assertTrue(report.contains("not live monitoring"));
    }

    @Test
    public void summaryRejectsInconsistentArrays() {
        assertTrue(EnvironmentIntelligenceRules.buildSummary(
                null, new int[0], new String[0]).contains("unavailable"));
        assertTrue(EnvironmentIntelligenceRules.buildSummary(
                new String[]{"Kolkata"}, new int[0], new String[0]).contains("unavailable"));
    }
}
