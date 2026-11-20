package com.jarvis.assistant;

/** Deterministic intent checks used before any command is routed to an action. */
public final class JarvisIntelligenceEngine {
    private JarvisIntelligenceEngine() {}

    /** Returns a clarification prompt when a spoken action is missing a required target. */
    public static String clarificationFor(String intent) {
        String s = intent == null ? "" : intent.trim().replaceAll("\\s+", " ");
        if (s.matches("^(open|launch|start)( app)?$")) return "Kaunsa app open karna hai?";
        if (s.matches("^(call|phone|dial)$")) return "Kisko call karna hai? Contact ka naam batao.";
        if (s.matches("^(send|message|text)( a)?( message)?$")) return "Kisko message bhejna hai, aur kya message?";
        if (s.matches("^(set|create|add) reminder$") || s.matches("^remind me$"))
            return "Kis baat ka reminder chahiye, aur kab?";
        if (s.matches("^(search|google|look up)$")) return "Kya search karna hai?";
        return null;
    }

    /** Destructive or externally visible actions must never be inferred from vague language. */
    public static boolean needsExplicitTarget(String intent) {
        String s = intent == null ? "" : intent.toLowerCase(java.util.Locale.ROOT);
        return s.matches(".*\\b(delete|erase|send|share|pay|purchase|call|message)\\b.*");
    }
}
