package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class B21MessageDraftRulesTest {
    @Test public void rejectsBlankAndOversizedDrafts() {
        assertFalse(B21MessageDraftRules.isValidDraft("  "));
        assertFalse(B21MessageDraftRules.isValidDraft(null));
        assertFalse(B21MessageDraftRules.isValidDraft(new String(new char[5001]).replace('\0', 'x')));
        assertTrue(B21MessageDraftRules.isValidDraft("Please call me when you are free."));
    }
}
