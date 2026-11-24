package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class HardwareCompatibilityRulesTest {
    @Test public void formatsAvailableAbisInReportedOrder() {
        assertEquals("arm64-v8a, armeabi-v7a",
                HardwareCompatibilityRules.formatAbiList(new String[]{"arm64-v8a", "armeabi-v7a"}));
    }

    @Test public void ignoresBlankAbiEntries() {
        assertEquals("arm64-v8a",
                HardwareCompatibilityRules.formatAbiList(new String[]{" ", "arm64-v8a", null}));
    }

    @Test public void handlesMissingAbiInformation() {
        assertEquals("Unavailable", HardwareCompatibilityRules.formatAbiList(null));
        assertEquals("Unavailable", HardwareCompatibilityRules.formatAbiList(new String[0]));
        assertEquals("Unavailable", HardwareCompatibilityRules.formatAbiList(new String[]{" ", null}));
    }
}
