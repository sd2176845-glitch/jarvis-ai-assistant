package com.jarvis.assistant;

/** Validation for editable message drafts before the user opens a messaging app. */
public final class B21MessageDraftRules {
    private B21MessageDraftRules() { }

    public static boolean isValidDraft(String value) {
        return value != null && !value.trim().isEmpty() && value.length() <= 5000;
    }
}
