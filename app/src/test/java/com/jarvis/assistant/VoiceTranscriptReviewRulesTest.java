package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class VoiceTranscriptReviewRulesTest {
    @Test public void unchangedTranscriptDoesNotInterruptVoiceFlow() {
        assertFalse(VoiceTranscriptReviewRules.requiresReview("Open YouTube", "Open YouTube"));
        assertFalse(VoiceTranscriptReviewRules.requiresReview("  Hello  ", "hello"));
    }

    @Test public void changedTranscriptRequiresOwnerReview() {
        assertTrue(VoiceTranscriptReviewRules.requiresReview("Call Maa", "Call Maya"));
        assertTrue(VoiceTranscriptReviewRules.requiresReview("Open YouTube", "Open WhatsApp"));
    }

    @Test public void emptyCorrectionFallsBackToOriginal() {
        assertEquals("open maps", VoiceTranscriptReviewRules.safeCorrection(" open maps ", ""));
        assertEquals("open maps", VoiceTranscriptReviewRules.safeCorrection(" open maps ", null));
        assertEquals("open maps", VoiceTranscriptReviewRules.safeCorrection("open maps", " open maps "));
    }
}
