package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class CwcFloodForecastRulesTest {
    @Test
    public void stationLevelValidationRequiresConsistentNonNegativeValues() {
        assertNull(CwcFloodForecastRules.validateLevels("Ganga at Patna",
                "48.5", "45", "50", "55"));
        assertNotNull(CwcFloodForecastRules.validateLevels("",
                "48.5", "45", "50", "55"));
        assertNotNull(CwcFloodForecastRules.validateLevels("Ganga",
                "-1", "45", "50", "55"));
        assertNotNull(CwcFloodForecastRules.validateLevels("Ganga",
                "48.5", "55", "50", "60"));
        assertNotNull(CwcFloodForecastRules.validateLevels("Ganga",
                "48.5", "45", "60", "55"));
        assertNotNull(CwcFloodForecastRules.validateLevels("https://example.com",
                "48.5", "45", "50", "55"));
    }

    @Test
    public void classificationMatchesCwcWarningDangerAndHflDefinitions() {
        assertEquals("Below warning level", CwcFloodForecastRules.classify(44, 45, 50, 55));
        assertEquals("Above normal flood situation", CwcFloodForecastRules.classify(45, 45, 50, 55));
        assertEquals("Severe flood situation", CwcFloodForecastRules.classify(50, 45, 50, 55));
        assertEquals("Extreme flood situation", CwcFloodForecastRules.classify(55, 45, 50, 55));
        assertEquals("Extreme flood situation", CwcFloodForecastRules.classify(56, 45, 50, 55));
        assertEquals("Invalid station levels", CwcFloodForecastRules.classify(40, 45, 42, 55));
    }

    @Test
    public void resultLabelsManualValuesAndNeverClaimsLiveVerification() {
        String report = CwcFloodForecastRules.buildResult(
                "Ganga at Patna", "50", "45", "50", "55");
        assertTrue(report.contains("Severe flood situation"));
        assertTrue(report.contains("manually entered"));
        assertTrue(report.contains("has not fetched or verified the live station level"));
        assertTrue(report.contains("not an evacuation order"));
    }
}
