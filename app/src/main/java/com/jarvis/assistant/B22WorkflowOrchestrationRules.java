package com.jarvis.assistant;

/** Safe workflow templates and owner-confirmed manual step orchestration. */
public final class B22WorkflowOrchestrationRules {
    private B22WorkflowOrchestrationRules() { }

    private static final String[] TITLES = {
            "Morning planning",
            "Study session",
            "Project review",
            "Weekly review"
    };

    private static final String[][] STEPS = {
            {"Review today's schedule", "Check urgent and important tasks", "Choose the top three priorities", "Prepare the workspace"},
            {"Choose one study topic", "Gather the required learning materials", "Set a focus timer manually", "Review notes and record questions"},
            {"Review the project goal", "Check pending tasks and blockers", "Review test or build results", "Record the next owner-approved action"},
            {"Review completed and pending tasks", "Check overdue items", "Review active projects", "Plan priorities for the next week"}
    };

    public static String[] templateTitles() {
        return TITLES.clone();
    }

    public static String[] templateSteps(int index) {
        if (index < 0 || index >= STEPS.length) return new String[0];
        return STEPS[index].clone();
    }

    /** Returns the first unfinished step, or -1 if all steps are complete. */
    public static int nextPendingStep(boolean[] completed) {
        if (completed == null) return -1;
        for (int i = 0; i < completed.length; i++) if (!completed[i]) return i;
        return -1;
    }

    public static boolean isValidTemplateIndex(int index) {
        return index >= 0 && index < TITLES.length;
    }
}
