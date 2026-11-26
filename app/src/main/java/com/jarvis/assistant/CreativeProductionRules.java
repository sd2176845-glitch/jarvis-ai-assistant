package com.jarvis.assistant;

/** Local creator preflight checks; does not render media or upload project material. */
public final class CreativeProductionRules {
    private CreativeProductionRules() {}

    public static String buildChecklist(String mediaType, String brief) {
        if (mediaType == null || mediaType.trim().isEmpty()) throw new IllegalArgumentException("Choose a media type.");
        if (brief == null || brief.trim().isEmpty()) throw new IllegalArgumentException("Enter a project brief.");
        if (brief.trim().length() > 500) throw new IllegalArgumentException("Keep the brief under 500 characters.");
        String type = mediaType.trim();
        return "CREATOR PREFLIGHT • " + type.toUpperCase(java.util.Locale.ROOT)
                + "\nProject brief: " + brief.trim()
                + "\n\nBEFORE PRODUCTION"
                + "\n[ ] Confirm the brief, audience and intended platform."
                + "\n[ ] Use original or properly licensed music, images, fonts and footage."
                + "\n[ ] Obtain permission for identifiable people, voices and private locations."
                + "\n[ ] Verify factual claims and clearly label opinion or sponsored content."
                + "\n[ ] Keep filming and production activities safe; do not stage dangerous stunts."
                + "\n\nEDITING & ACCESSIBILITY"
                + "\n[ ] Add accurate captions/subtitles and review spelling."
                + "\n[ ] Check readable text, contrast and uncluttered framing."
                + "\n[ ] Balance audio and avoid unexpected loudness."
                + "\n[ ] Check continuity, timing and platform aspect ratio."
                + "\n\nFINAL REVIEW"
                + "\n[ ] Remove private information, location clues and unintended background details."
                + "\n[ ] Confirm credits, permissions and license requirements."
                + "\n[ ] Preview the complete export before publishing."
                + "\n\nChecklist only • no media file has been rendered or published.";
    }

    public static int[] distributeSceneSeconds(int totalSeconds, int sceneCount) {
        if (totalSeconds < 1 || totalSeconds > 600) throw new IllegalArgumentException("Duration must be 1–600 seconds.");
        if (sceneCount < 1 || sceneCount > 60 || sceneCount > totalSeconds) {
            throw new IllegalArgumentException("Scene count must be 1–60 and cannot exceed total seconds.");
        }
        int[] result = new int[sceneCount];
        int base = totalSeconds / sceneCount;
        int remainder = totalSeconds % sceneCount;
        for (int i = 0; i < sceneCount; i++) result[i] = base + (i < remainder ? 1 : 0);
        return result;
    }
}
