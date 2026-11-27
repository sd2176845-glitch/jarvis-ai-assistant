package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class DashboardProfileRulesTest {
    @Test public void profilesHaveOrderedSpacing() {
        assertTrue(DashboardProfileRules.horizontalPaddingDp(DashboardProfileRules.SPACIOUS)
                > DashboardProfileRules.horizontalPaddingDp(DashboardProfileRules.STANDARD));
        assertTrue(DashboardProfileRules.verticalPaddingDp(DashboardProfileRules.STANDARD)
                > DashboardProfileRules.verticalPaddingDp(DashboardProfileRules.COMPACT));
    }

    @Test public void invalidProfilesFallBackToStandard() {
        assertEquals(DashboardProfileRules.STANDARD, DashboardProfileRules.normalize(-1));
        assertEquals(DashboardProfileRules.STANDARD, DashboardProfileRules.normalize(99));
        assertEquals(DashboardProfileRules.horizontalPaddingDp(99),
                DashboardProfileRules.horizontalPaddingDp(DashboardProfileRules.STANDARD));
    }
}
