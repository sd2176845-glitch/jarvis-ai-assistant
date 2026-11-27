package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class OwnerConsentRulesTest {
    @Test public void consentDefaultsToOff() {
        assertFalse(OwnerConsentRules.defaultConsent());
    }

    @Test public void accessRequiresBothOwnerConsentAndAndroidPermission() {
        assertFalse(OwnerConsentRules.effectiveAccess(false, false));
        assertFalse(OwnerConsentRules.effectiveAccess(false, true));
        assertFalse(OwnerConsentRules.effectiveAccess(true, false));
        assertTrue(OwnerConsentRules.effectiveAccess(true, true));
    }

    @Test public void statusExplainsTheTwoIndependentControls() {
        assertEquals("Owner consent not given", OwnerConsentRules.stateLabel(false, true));
        assertEquals("Owner consent given; Android permission not granted",
                OwnerConsentRules.stateLabel(true, false));
        assertEquals("Owner consent and Android permission granted",
                OwnerConsentRules.stateLabel(true, true));
    }
}
