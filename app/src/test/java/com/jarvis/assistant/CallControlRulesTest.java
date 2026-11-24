package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class CallControlRulesTest {
    @Test public void acceptsOnlyKnownDialerPackages() {
        assertTrue(CallControlRules.isSupportedCallUi("com.google.android.dialer"));
        assertTrue(CallControlRules.isSupportedCallUi("com.android.incallui"));
        assertFalse(CallControlRules.isSupportedCallUi("com.whatsapp"));
        assertFalse(CallControlRules.isSupportedCallUi("com.example.unknown"));
        assertFalse(CallControlRules.isSupportedCallUi(null));
    }
}
