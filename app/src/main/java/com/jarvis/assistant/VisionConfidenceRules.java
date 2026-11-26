package com.jarvis.assistant;

/** Conservative presentation rules for on-device ML vision estimates. */
public final class VisionConfidenceRules {
    private VisionConfidenceRules() { }

    public static boolean shouldDisplay(float confidence) {
        return !Float.isNaN(confidence) && !Float.isInfinite(confidence) && confidence >= 0.35f;
    }

    public static String confidenceLabel(float confidence) {
        if (!shouldDisplay(confidence)) return "Very uncertain • hidden";
        if (confidence >= 0.80f) return "Higher model confidence";
        if (confidence >= 0.60f) return "Moderate model confidence";
        return "Low confidence • uncertain";
    }

    public static String spatialRegion(float centerX, float centerY) {
        String horizontal = centerX < 0.38f ? "left" : (centerX > 0.62f ? "right" : "center");
        String vertical = centerY < 0.38f ? "upper" : (centerY > 0.62f ? "lower" : "middle");
        if ("center".equals(horizontal) && "middle".equals(vertical)) return "center";
        return vertical + " " + horizontal;
    }
}
