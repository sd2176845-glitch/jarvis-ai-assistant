package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class VisionConfidenceRulesTest {
    @Test public void hidesVeryLowAndInvalidConfidence() {
        assertFalse(VisionConfidenceRules.shouldDisplay(0.20f));
        assertFalse(VisionConfidenceRules.shouldDisplay(Float.NaN));
        assertFalse(VisionConfidenceRules.shouldDisplay(Float.POSITIVE_INFINITY));
    }

    @Test public void labelsConfidenceWithoutCallingItCertainty() {
        assertEquals("Higher model confidence", VisionConfidenceRules.confidenceLabel(0.90f));
        assertEquals("Moderate model confidence", VisionConfidenceRules.confidenceLabel(0.65f));
        assertEquals("Low confidence • uncertain", VisionConfidenceRules.confidenceLabel(0.45f));
        assertEquals("Very uncertain • hidden", VisionConfidenceRules.confidenceLabel(0.10f));
    }

    @Test public void mapsBoundingBoxCentersToCoarseImageRegions() {
        assertEquals("upper left", VisionConfidenceRules.spatialRegion(0.1f, 0.1f));
        assertEquals("center", VisionConfidenceRules.spatialRegion(0.5f, 0.5f));
        assertEquals("lower right", VisionConfidenceRules.spatialRegion(0.9f, 0.9f));
    }
}
