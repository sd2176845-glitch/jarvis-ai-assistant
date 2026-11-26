package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class MemorySearchRulesTest {
    @Test public void searchesCaseInsensitivelyAcrossFields() {
        assertTrue(MemorySearchRules.matches("MALDA", "Home", "Malda district"));
        assertFalse(MemorySearchRules.matches("Mumbai", "Home", "Malda"));
    }

    @Test public void rejectsEmptyQueriesAndNullFields() {
        assertFalse(MemorySearchRules.matches("", "anything"));
        assertFalse(MemorySearchRules.matches("word", (String[]) null));
        assertFalse(MemorySearchRules.matches("word", "  ", null));
    }

    @Test public void previewIsBoundedAndWhitespaceIsNormalized() {
        assertEquals("hello world", MemorySearchRules.preview(" hello   world ", 30));
        assertEquals("hell…", MemorySearchRules.preview("hello world", 5));
        assertEquals("", MemorySearchRules.preview(null, 10));
    }
}
