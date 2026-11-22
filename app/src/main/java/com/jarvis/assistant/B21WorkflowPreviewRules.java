package com.jarvis.assistant;

/** Pure preview formatter for the existing manual checklist workflow store. */
public final class B21WorkflowPreviewRules {
    private B21WorkflowPreviewRules() { }

    public static String buildPreview(String title, String[] steps) {
        StringBuilder out = new StringBuilder("DRY-RUN • NO ACTIONS EXECUTED\n");
        out.append("WORKFLOW: ").append(title == null || title.trim().isEmpty() ? "Untitled workflow" : title.trim());
        out.append("\n\n");
        if (steps == null || steps.length == 0) {
            out.append("No steps are available.");
        } else {
            for (int i = 0; i < steps.length; i++) {
                String step = steps[i] == null || steps[i].trim().isEmpty()
                        ? "Unavailable step" : steps[i].trim();
                out.append(i + 1).append(". ").append(step).append("\n");
            }
        }
        out.append("\nRead-only checklist preview. No apps were opened, messages sent, settings changed, or steps performed.");
        return out.toString();
    }
}
