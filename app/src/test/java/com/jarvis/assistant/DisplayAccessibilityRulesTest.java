package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class DisplayAccessibilityRulesTest {
    @Test public void profilesProvideIncreasingReadableTextSizes() {
        assertTrue(DisplayAccessibilityRules.statusTextSize(DisplayAccessibilityRules.EXTRA_LARGE)
                > DisplayAccessibilityRules.statusTextSize(DisplayAccessibilityRules.LARGE));
        assertTrue(DisplayAccessibilityRules.transcriptTextSize(DisplayAccessibilityRules.LARGE)
                > DisplayAccessibilityRules.transcriptTextSize(DisplayAccessibilityRules.STANDARD));
    }

    @Test public void highContrastIsDistinctAndUsesReadableSizes() {
        assertTrue(DisplayAccessibilityRules.isHighContrast(DisplayAccessibilityRules.HIGH_CONTRAST));
        assertFalse(DisplayAccessibilityRules.isHighContrast(DisplayAccessibilityRules.LARGE));
        assertTrue(DisplayAccessibilityRules.transcriptTextSize(DisplayAccessibilityRules.HIGH_CONTRAST)
                >= DisplayAccessibilityRules.transcriptTextSize(DisplayAccessibilityRules.STANDARD));
    }

    @Test public void invalidProfilesFallBackToStandard() {
        assertEquals(DisplayAccessibilityRules.STANDARD, DisplayAccessibilityRules.normalizeProfile(-1));
        assertEquals(DisplayAccessibilityRules.STANDARD, DisplayAccessibilityRules.normalizeProfile(99));
        assertEquals(DisplayAccessibilityRules.statusTextSize(99),
                DisplayAccessibilityRules.statusTextSize(DisplayAccessibilityRules.STANDARD), 0.001f);
    }
}
