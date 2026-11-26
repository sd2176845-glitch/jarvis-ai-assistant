package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class CreativeProductionRulesTest {
    @Test public void checklistIncludesRightsConsentAndAccessibility() {
        String result = CreativeProductionRules.buildChecklist("Short video", "Science explainer");
        assertTrue(result.contains("licensed"));
        assertTrue(result.contains("permission"));
        assertTrue(result.contains("captions"));
        assertTrue(result.contains("no media file has been rendered"));
    }

    @Test public void sceneDurationsPreserveTotalAndSpreadRemainder() {
        assertArrayEquals(new int[]{4, 3, 3}, CreativeProductionRules.distributeSceneSeconds(10, 3));
        assertArrayEquals(new int[]{5}, CreativeProductionRules.distributeSceneSeconds(5, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyBrief() {
        CreativeProductionRules.buildChecklist("Video", " ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsImpossibleSceneCount() {
        CreativeProductionRules.distributeSceneSeconds(2, 3);
    }
}
