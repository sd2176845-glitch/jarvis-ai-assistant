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

    @Test public void preferenceValuesMustMatchTheirExpectedType() {
        assertTrue(DiagnosticsRecoveryRules.isAllowedValue("assistant_mode", "Caring"));
        assertTrue(DiagnosticsRecoveryRules.isAllowedValue("voice_pitch", 1.0d));
        assertTrue(DiagnosticsRecoveryRules.isAllowedValue("silent_background_ack", Boolean.TRUE));
        assertTrue(DiagnosticsRecoveryRules.isAllowedValue("dashboard_spacing_profile_v1", 2));
        assertFalse(DiagnosticsRecoveryRules.isAllowedValue("voice_pitch", "high"));
        assertFalse(DiagnosticsRecoveryRules.isAllowedValue("assistant_mode", Boolean.TRUE));
        assertFalse(DiagnosticsRecoveryRules.isAllowedValue("guest_mode_enabled", Boolean.TRUE));
    }
}
