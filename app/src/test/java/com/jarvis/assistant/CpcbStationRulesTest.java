package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class CpcbStationRulesTest {
    @Test
    public void requestRequiresCityAndAnEphemeralApiKey() {
        assertNull(CpcbStationRules.validateRequest("Kolkata", "West_Bengal", "ABCDEF0123456789"));
        assertNull(CpcbStationRules.validateRequest("Kolkata", "", "ABCDEF0123456789"));
        assertNotNull(CpcbStationRules.validateRequest("", "West_Bengal", "ABCDEF0123456789"));
        assertNotNull(CpcbStationRules.validateRequest("Kolkata", "", "short"));
        assertNotNull(CpcbStationRules.validateRequest("https://example.com", "", "ABCDEF0123456789"));
        assertNotNull(CpcbStationRules.validateRequest("Kolkata\nInjected", "", "ABCDEF0123456789"));
    }

    @Test
    public void numericReadingsRejectUnknownMalformedAndNegativeValues() {
        assertEquals("12.5", CpcbStationRules.displayMeasurement("12.5"));
        assertEquals("0", CpcbStationRules.displayMeasurement("0"));
        assertEquals("Not supplied", CpcbStationRules.displayMeasurement("NA"));
        assertEquals("Not supplied", CpcbStationRules.displayMeasurement("not-a-number"));
        assertEquals("Not supplied", CpcbStationRules.displayMeasurement("-3"));
        assertEquals("Not supplied", CpcbStationRules.displayMeasurement("NaN"));
        assertEquals("Not supplied", CpcbStationRules.displayMeasurement("Infinity"));
    }

    @Test
    public void recordFormattingSanitizesLabelsAndDoesNotInventCombinedAqi() {
        String detail = CpcbStationRules.formatStationRecord(
                "Victoria Memorial\\nStation", "Kolkata", "West Bengal", "PM2.5",
                "18.4", "7", "31", "µg/m³", "24-11-2026 17:00");
        assertTrue(detail.contains("Victoria Memorial Station"));
        assertTrue(detail.contains("Pollutant: PM2.5"));
        assertTrue(detail.contains("Average: 18.4 µg/m³"));
        assertTrue(detail.contains("not a combined AQI value"));
        assertFalse(detail.contains("Victoria Memorial\\nStation"));
    }

    @Test
    public void summaryReportsSourceAndDoesNotClaimCompleteCoverage() {
        String summary = CpcbStationRules.buildSummary("Kolkata", "West Bengal", 8,
                "24-11-2026 17:00");
        assertTrue(summary.contains("8"));
        assertTrue(summary.contains("hourly granularity"));
        assertTrue(summary.contains("does not guarantee that every station"));
    }
}
