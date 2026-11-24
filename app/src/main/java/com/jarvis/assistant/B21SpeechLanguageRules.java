package com.jarvis.assistant;

/** Allowlisted Android speech-recognition locale choices. */
public final class B21SpeechLanguageRules {
    private B21SpeechLanguageRules() { }

    public static String localeForChoice(int choice) {
        switch (choice) {
            case 0: return "en-IN";
            case 1: return "hi-IN";
            case 2: return "bn-IN";
            default: return "en-IN";
        }
    }

    public static boolean isSupportedLocale(String locale) {
        return "en-IN".equals(locale) || "hi-IN".equals(locale) || "bn-IN".equals(locale);
    }
}
