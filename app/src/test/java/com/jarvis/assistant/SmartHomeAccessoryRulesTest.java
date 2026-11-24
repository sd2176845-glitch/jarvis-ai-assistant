package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class SmartHomeAccessoryRulesTest {
    @Test public void createsTrimmedBluetoothLabel() {
        assertEquals("Bluetooth • Headphones", SmartHomeAccessoryRules.createBluetoothLabel("  Headphones  "));
    }

    @Test public void rejectsEmptyOrOversizedLabels() {
        assertNull(SmartHomeAccessoryRules.createBluetoothLabel("  "));
        assertNull(SmartHomeAccessoryRules.createBluetoothLabel(repeat('x', 101)));
    }

    @Test public void identifiesOnlyBluetoothAccessoryLabels() {
        assertTrue(SmartHomeAccessoryRules.isBluetoothLabel("Bluetooth • Speaker"));
        assertFalse(SmartHomeAccessoryRules.isBluetoothLabel("Living room • bulb"));
        assertFalse(SmartHomeAccessoryRules.isBluetoothLabel(null));
    }

    private static String repeat(char value, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) result.append(value);
        return result.toString();
    }
}
