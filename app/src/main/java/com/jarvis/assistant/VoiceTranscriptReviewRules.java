package com.jarvis.assistant;

/** Pure rules for owner review of speech-recognition corrections. */
public final class VoiceTranscriptReviewRules {
    private VoiceTranscriptReviewRules() { }

    /** Require review whenever correction changes the recognized words. */
    public static boolean requiresReview(String original, String corrected) {
        String first = original == null ? "" : original.trim();
        String second = corrected == null ? "" : corrected.trim();
        return !first.equalsIgnoreCase(second);
    }

    /** Use the original transcript when the correction is empty. */
    public static String safeCorrection(String original, String corrected) {
        String first = original == null ? "" : original.trim();
        String second = corrected == null ? "" : corrected.trim();
        return second.isEmpty() ? first : second;
    }
}
