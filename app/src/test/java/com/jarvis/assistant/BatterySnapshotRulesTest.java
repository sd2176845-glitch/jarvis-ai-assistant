package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class BatterySnapshotRulesTest {
    @Test public void mapsChargingStates() {
        assertEquals("Charging", BatterySnapshotRules.chargingState(2));
        assertEquals("Discharging", BatterySnapshotRules.chargingState(3));
        assertEquals("Not charging", BatterySnapshotRules.chargingState(4));
        assertEquals("Full", BatterySnapshotRules.chargingState(5));
        assertEquals("Unknown / unavailable", BatterySnapshotRules.chargingState(-1));
    }

    @Test public void mapsBatteryHealthWithoutInventingDiagnosis() {
        assertEquals("Good", BatterySnapshotRules.healthLabel(2));
        assertEquals("Overheating reported", BatterySnapshotRules.healthLabel(3));
        assertEquals("Unknown / unavailable", BatterySnapshotRules.healthLabel(0));
    }

    @Test public void formatsTemperatureAndMissingValue() {
        assertEquals("32.5 °C", BatterySnapshotRules.temperatureLabel(325));
        assertEquals("Unavailable", BatterySnapshotRules.temperatureLabel(-1));
    }
}
