package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class B21SpeechLanguageRulesTest {
    @Test public void mapsOnlyTheThreeVisibleIndianLocales() {
        assertEquals("en-IN", B21SpeechLanguageRules.localeForChoice(0));
        assertEquals("hi-IN", B21SpeechLanguageRules.localeForChoice(1));
        assertEquals("bn-IN", B21SpeechLanguageRules.localeForChoice(2));
        assertEquals("en-IN", B21SpeechLanguageRules.localeForChoice(99));
        assertFalse(B21SpeechLanguageRules.isSupportedLocale("xx-XX"));
    }
}
