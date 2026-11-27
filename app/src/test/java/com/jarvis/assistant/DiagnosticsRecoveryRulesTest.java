package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class DiagnosticsRecoveryRulesTest {
    @Test public void onlyPresentationAndVoicePreferencesAreAllowed() {
        assertTrue(DiagnosticsRecoveryRules.isAllowedKey("assistant_mode"));
        assertTrue(DiagnosticsRecoveryRules.isAllowedKey("dashboard_spacing_profile_v1"));
        assertFalse(DiagnosticsRecoveryRules.isAllowedKey("guest_mode_enabled"));
        assertFalse(DiagnosticsRecoveryRules.isAllowedKey("emergency_contact"));
        assertFalse(DiagnosticsRecoveryRules.isAllowedKey("jarvis_notes"));
        assertFalse(DiagnosticsRecoveryRules.isAllowedKey(null));
    }

    @Test public void onlyPrimitivePreferenceValuesAreSupported() {
        assertTrue(DiagnosticsRecoveryRules.isSupportedValue("Normal"));
        assertTrue(DiagnosticsRecoveryRules.isSupportedValue(Boolean.TRUE));
        assertTrue(DiagnosticsRecoveryRules.isSupportedValue(1));
        assertTrue(DiagnosticsRecoveryRules.isSupportedValue(1.0f));
        assertFalse(DiagnosticsRecoveryRules.isSupportedValue(new Object()));
        assertFalse(DiagnosticsRecoveryRules.isSupportedValue(null));
    }
}
