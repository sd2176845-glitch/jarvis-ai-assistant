package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class B22WorkflowOrchestrationRulesTest {
    @Test public void findsFirstPendingStep() {
        assertEquals(0, B22WorkflowOrchestrationRules.nextPendingStep(new boolean[]{false, false, true}));
        assertEquals(1, B22WorkflowOrchestrationRules.nextPendingStep(new boolean[]{true, false, true}));
    }

    @Test public void completedOrMissingWorkflowHasNoPendingStep() {
        assertEquals(-1, B22WorkflowOrchestrationRules.nextPendingStep(new boolean[]{true, true}));
        assertEquals(-1, B22WorkflowOrchestrationRules.nextPendingStep(new boolean[0]));
        assertEquals(-1, B22WorkflowOrchestrationRules.nextPendingStep(null));
    }

    @Test public void templatesAreBoundedAndDefensiveCopies() {
        String[] titles = B22WorkflowOrchestrationRules.templateTitles();
        assertEquals(4, titles.length);
        String[] steps = B22WorkflowOrchestrationRules.templateSteps(0);
        assertTrue(steps.length > 0);
        steps[0] = "tampered";
        assertNotEquals("tampered", B22WorkflowOrchestrationRules.templateSteps(0)[0]);
        assertFalse(B22WorkflowOrchestrationRules.isValidTemplateIndex(-1));
        assertFalse(B22WorkflowOrchestrationRules.isValidTemplateIndex(4));
    }
}
