package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class EmergencyPreparednessPackRulesTest {
    @Test
    public void allSupportedTopicsProduceOfflineGuidanceAndOfficialReferences() {
        for (String topic : EmergencyPreparednessPackRules.topics()) {
            String guide = EmergencyPreparednessPackRules.buildPack(topic);
            assertTrue(topic, guide.contains("OFFLINE EMERGENCY PREPAREDNESS GUIDE"));
            assertTrue(topic, guide.contains("call 112"));
            assertTrue(topic, guide.contains("not an official warning"));
            assertTrue(topic, guide.contains(EmergencyPreparednessPackRules.SACHET_URL));
            assertTrue(topic, guide.contains(EmergencyPreparednessPackRules.IMD_URL));
            assertTrue(topic, guide.contains(EmergencyPreparednessPackRules.MHA_112_URL));
            assertTrue(topic, guide.contains(EmergencyPreparednessPackRules.SOURCE_REVIEW_DATE));
        }
    }

    @Test
    public void hazardSpecificGuidanceContainsDistinctSafetyReminders() {
        assertTrue(EmergencyPreparednessPackRules.buildPack("Flood").contains("Never walk, swim or travel through moving or unknown floodwater"));
        assertTrue(EmergencyPreparednessPackRules.buildPack("Cyclone / severe storm").contains("official all-clear"));
        assertTrue(EmergencyPreparednessPackRules.buildPack("Earthquake").contains("DROP, COVER and HOLD"));
        assertTrue(EmergencyPreparednessPackRules.buildPack("Heatwave").contains("Drink water regularly"));
    }

    @Test
    public void unsupportedOrMissingTopicsAreHandledSafely() {
        assertTrue(EmergencyPreparednessPackRules.buildPack("Tsunami").contains("not available"));
        assertTrue(EmergencyPreparednessPackRules.buildPack(null).contains("not available"));
    }

    @Test
    public void offlinePackClearlySeparatesStaticGuidanceFromLiveAlerts() {
        String guide = EmergencyPreparednessPackRules.buildPack("General emergency");
        assertTrue(guide.contains("INTERNET REQUIRED"));
        assertTrue(guide.contains("live warnings are not"));
        assertTrue(guide.contains("not a live forecast"));
    }
}
