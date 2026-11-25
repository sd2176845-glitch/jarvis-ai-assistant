package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class HumanIntelligenceRulesTest {
    @Test public void recognizesExplicitSadnessCueAsTentative() {
        HumanIntelligenceRules.Cue cue =
                HumanIntelligenceRules.classifyExplicitTextCue("Main udaas hoon");
        assertEquals(HumanIntelligenceRules.Signal.SADNESS, cue.signal);
        assertEquals("tentative", cue.confidence);
    }

    @Test public void recognizesStressJoyLonelinessAndFrustration() {
        assertEquals(HumanIntelligenceRules.Signal.STRESS,
                HumanIntelligenceRules.classifyExplicitTextCue("Mujhe tension ho rahi hai").signal);
        assertEquals(HumanIntelligenceRules.Signal.JOY,
                HumanIntelligenceRules.classifyExplicitTextCue("Main bahut khush hoon").signal);
        assertEquals(HumanIntelligenceRules.Signal.LONELINESS,
                HumanIntelligenceRules.classifyExplicitTextCue("I feel alone").signal);
        assertEquals(HumanIntelligenceRules.Signal.FRUSTRATION,
                HumanIntelligenceRules.classifyExplicitTextCue("I am frustrated hoon").signal);
        assertEquals(HumanIntelligenceRules.Signal.LONELINESS,
                HumanIntelligenceRules.classifyExplicitTextCue("Mujhe lagta hai koi nahi hai").signal);
        assertEquals(HumanIntelligenceRules.Signal.SADNESS,
                HumanIntelligenceRules.classifyExplicitTextCue("Mann nahi lag raha").signal);
    }

    @Test public void negatedOrAmbiguousPhrasesDoNotTriggerEmotionCue() {
        assertEquals(HumanIntelligenceRules.Signal.NONE,
                HumanIntelligenceRules.classifyExplicitTextCue("Main udaas nahi hoon").signal);
        assertEquals(HumanIntelligenceRules.Signal.NONE,
                HumanIntelligenceRules.classifyExplicitTextCue("sad movie ka naam batao").signal);
        assertEquals(HumanIntelligenceRules.Signal.NONE,
                HumanIntelligenceRules.classifyExplicitTextCue(null).signal);
    }

    @Test public void supportiveReplyOnlyAppearsForExplicitCue() {
        assertNotNull(HumanIntelligenceRules.buildLocalSupportiveReply("Main pareshan hoon"));
        assertNull(HumanIntelligenceRules.buildLocalSupportiveReply("Mujhe kal ka weather batao"));
    }

    @Test public void guidanceContainsPrivacyAndSocialReasoningBoundaries() {
        String guidance = HumanIntelligenceRules.buildGuidance("Caring", "Main udaas hoon");
        assertTrue(guidance.contains("tentative"));
        assertTrue(guidance.contains("hidden thoughts"));
        assertTrue(guidance.contains("owner-selected media"));
        assertTrue(guidance.contains("current text/transcript"));
        assertTrue(guidance.contains("Caring"));
    }
}
