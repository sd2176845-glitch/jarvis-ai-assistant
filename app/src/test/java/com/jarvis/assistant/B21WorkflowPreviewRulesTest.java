package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class B21WorkflowPreviewRulesTest {
    @Test public void previewListsStepsAndDeclaresNoExecution() {
        String preview = B21WorkflowPreviewRules.buildPreview("Morning routine",
                new String[]{"Review agenda", "Pack bag"});
        assertTrue(preview.contains("1. Review agenda"));
        assertTrue(preview.contains("2. Pack bag"));
        assertTrue(preview.contains("DRY-RUN"));
        assertTrue(preview.contains("No apps were opened"));
    }

    @Test public void emptyWorkflowHasSafeFallback() {
        assertTrue(B21WorkflowPreviewRules.buildPreview("", new String[0])
                .contains("No steps are available."));
    }
}
