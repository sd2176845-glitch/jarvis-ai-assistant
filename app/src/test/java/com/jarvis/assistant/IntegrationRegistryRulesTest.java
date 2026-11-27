package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class IntegrationRegistryRulesTest {
    @Test public void integrationKeysAreDistinctAndStable() {
        assertNotEquals(IntegrationRegistryStudio.SMART_HOME,
                IntegrationRegistryStudio.CONNECTED_SYSTEMS);
        assertTrue(IntegrationRegistryStudio.SMART_HOME.startsWith("integration_"));
        assertTrue(IntegrationRegistryStudio.CONNECTED_SYSTEMS.startsWith("integration_"));
    }
}
