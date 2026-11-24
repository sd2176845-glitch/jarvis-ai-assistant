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
    public void liveUsAqiCategoriesUseUsScaleAndKeepCpcbDistinct() {
        assertEquals("Not supplied", EnvironmentIntelligenceRules.usAqiCategory(-1));
        assertEquals("Good", EnvironmentIntelligenceRules.usAqiCategory(50));
        assertEquals("Moderate", EnvironmentIntelligenceRules.usAqiCategory(51));
        assertEquals("Unhealthy for sensitive groups", EnvironmentIntelligenceRules.usAqiCategory(101));
        assertEquals("Unhealthy", EnvironmentIntelligenceRules.usAqiCategory(151));
        assertEquals("Very unhealthy", EnvironmentIntelligenceRules.usAqiCategory(201));
        assertEquals("Hazardous", EnvironmentIntelligenceRules.usAqiCategory(301));
        assertEquals("Beyond standard US AQI bands", EnvironmentIntelligenceRules.usAqiCategory(501));
    }

    @Test
    public void liveAirQualityReportLabelsModelEstimatesAndRejectsUnsafeFields() {
        String report = EnvironmentIntelligenceRules.formatLiveAirQuality(
                "Kolkata\nInjected", 87, 42, "18.5", "Not a number", "31.2",
                "4.0", "2.1", "3.4", "2026-11-24T16:00", "Asia/Kolkata");
        assertTrue(report.contains("Kolkata Injected"));
        assertTrue(report.contains("U.S. AQI: 87 • Moderate"));
        assertTrue(report.contains("European AQI: 42"));
        assertTrue(report.contains("PM2.5: 18.5 µg/m³"));
        assertTrue(report.contains("PM10: Not supplied µg/m³"));
        assertTrue(report.contains("not CPCB monitoring-station readings"));
        assertTrue(report.contains("not India's CPCB National AQI scale"));
        assertFalse(report.contains("Not a number"));
    }

    @Test
    public void hourlyForecastFormatsModelEstimatesAndRejectsMalformedValues() {
        String report = EnvironmentIntelligenceRules.formatAirQualityForecast(
                "Kolkata\nInjected",
                new String[]{"2026-11-24T17:00", "2026-11-24T18:00"},
                new String[]{"87", "bad"},
                new String[]{"42", "55"},
                new String[]{"18.5", "not-a-number"},
                new String[]{"33", "12.0"},
                "Asia/Kolkata");
        assertTrue(report.contains("Kolkata Injected"));
        assertTrue(report.contains("NEXT 24 HOURS"));
        assertTrue(report.contains("U.S. AQI: 87 • Moderate"));
        assertTrue(report.contains("U.S. AQI: Not supplied"));
        assertTrue(report.contains("European AQI: 42"));
        assertTrue(report.contains("PM2.5: 18.5 µg/m³"));
        assertTrue(report.contains("PM2.5: Not supplied µg/m³"));
        assertTrue(report.contains("not CPCB monitoring-station readings"));
        assertTrue(report.contains("not India's CPCB National AQI scale"));
        assertFalse(report.contains("not-a-number"));
    }

    @Test
    public void hourlyForecastRejectsEmptyAndMismatchedInputsSafely() {
        assertTrue(EnvironmentIntelligenceRules.formatAirQualityForecast(
                "Kolkata", new String[0], null, null, null, null, "Asia/Kolkata")
                .contains("No hourly"));
        String report = EnvironmentIntelligenceRules.formatAirQualityForecast(
                "Kolkata", new String[]{"2026-11-24T17:00"}, null, null, null, null, "Asia/Kolkata");
        assertTrue(report.contains("U.S. AQI: Not supplied"));
        assertTrue(report.contains("PM10: Not supplied"));
    }

    @Test
    public void hourlyForecastIsBoundedTo24Entries() {
        String[] times = new String[25];
        String[] indexes = new String[25];
        String[] values = new String[25];
        for (int i = 0; i < 25; i++) {
            times[i] = "2026-11-24T" + String.format(java.util.Locale.US, "%02d", i % 24) + ":00";
            indexes[i] = "50";
            values[i] = "10.0";
        }
        times[24] = "EXTRA_25TH_HOUR";
        String report = EnvironmentIntelligenceRules.formatAirQualityForecast(
                "Kolkata", times, indexes, indexes, values, values, "Asia/Kolkata");
        assertTrue(report.contains("Showing 24 hourly model estimates"));
        assertFalse(report.contains("EXTRA_25TH_HOUR"));
    }

    @Test
    public void uvIndexUsesWhoExposureBands() {
        assertEquals("Low", EnvironmentIntelligenceRules.uvCategory(0));
        assertEquals("Low", EnvironmentIntelligenceRules.uvCategory(2.9));
        assertEquals("Moderate", EnvironmentIntelligenceRules.uvCategory(3));
        assertEquals("Moderate", EnvironmentIntelligenceRules.uvCategory(5.9));
        assertEquals("High", EnvironmentIntelligenceRules.uvCategory(6));
        assertEquals("High", EnvironmentIntelligenceRules.uvCategory(7.9));
        assertEquals("Very high", EnvironmentIntelligenceRules.uvCategory(8));
        assertEquals("Very high", EnvironmentIntelligenceRules.uvCategory(10.9));
        assertEquals("Extreme", EnvironmentIntelligenceRules.uvCategory(11));
        assertEquals("Not supplied", EnvironmentIntelligenceRules.uvCategory(-1));
        assertEquals("Not supplied", EnvironmentIntelligenceRules.uvCategory(Double.NaN));
    }

    @Test
    public void uvForecastFormatsHourlyValuesAndProtectionNote() {
        String report = EnvironmentIntelligenceRules.formatUvForecast(
                "Kolkata\nInjected",
                new String[]{"2026-11-24T10:00", "2026-11-24T11:00", "2026-11-24T12:00"},
                new String[]{"2.5", "6.2", "9.8"},
                new String[]{"3.1", "7.0", "10.2"},
                "Asia/Kolkata");
        assertTrue(report.contains("Kolkata Injected"));
        assertTrue(report.contains("NEXT 24 HOURS"));
        assertTrue(report.contains("UV index forecast: 2.5"));
        assertTrue(report.contains("Clear-sky reference: 10.2"));
        assertTrue(report.contains("Level: Very high"));
        assertTrue(report.contains("Very high or extreme UV"));
        assertTrue(report.contains("regional model estimates"));
    }

    @Test
    public void uvForecastRejectsMalformedValuesAndCapsOutputAt24Hours() {
        String[] times = new String[25];
        String[] uv = new String[25];
        String[] clear = new String[25];
        for (int i = 0; i < 25; i++) {
            times[i] = "2026-11-24T" + String.format(java.util.Locale.US, "%02d", i % 24) + ":00";
            uv[i] = i == 0 ? "bad" : "3.0";
            clear[i] = "31.0";
        }
        times[24] = "EXTRA_25TH_HOUR";
        String report = EnvironmentIntelligenceRules.formatUvForecast(
                "Kolkata", times, uv, clear, "Asia/Kolkata");
        assertTrue(report.contains("UV index forecast: Not supplied"));
        assertTrue(report.contains("Clear-sky reference: Not supplied"));
        assertTrue(report.contains("Showing 24 hourly model estimates"));
        assertFalse(report.contains("EXTRA_25TH_HOUR"));
        assertFalse(report.contains("bad"));
    }

    @Test
    public void uvForecastHandlesEmptyAndMissingArrays() {
        assertTrue(EnvironmentIntelligenceRules.formatUvForecast(
                "Kolkata", new String[0], null, null, "Asia/Kolkata").contains("No hourly"));
        String report = EnvironmentIntelligenceRules.formatUvForecast(
                "Kolkata", new String[]{"2026-11-24T10:00"}, null, null, "Asia/Kolkata");
        assertTrue(report.contains("UV index forecast: Not supplied"));
        assertTrue(report.contains("Clear-sky reference: Not supplied"));
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
