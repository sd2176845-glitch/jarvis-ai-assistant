package com.jarvis.assistant;

/** Prompt builder for the user-invoked Creator Studio. Generated output is always a draft. */
public final class JarvisCreatorEngine {
    private JarvisCreatorEngine() {}
    public static String buildPrompt(String format, String brief) {
        String request = brief == null ? "" : brief.trim();
        if (request.isEmpty()) request = "Create a useful general example.";
        String kind = format == null ? "caption" : format.toLowerCase(java.util.Locale.ROOT);
        String instruction;
        if (kind.contains("script")) instruction = "Write an original 30-second short-video script with a hook, 3 concise scenes, and a closing line.";
        else if (kind.contains("story")) instruction = "Write 3 original Instagram story text options, each short and natural.";
        else if (kind.contains("bio")) instruction = "Write 5 concise social profile bio options. Avoid false claims.";
        else instruction = "Write 5 original social-media caption options with distinct tones. Avoid copied lyrics or copyrighted catchphrases.";
        return instruction + " Use the user's requested language and tone. This is a draft for the user to review, not an auto-post. Brief: " + request;
    }
}
