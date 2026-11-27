package com.jarvis.assistant;

import java.util.Locale;

/**
 * M02 human-intelligence rules.
 *
 * This is a deliberately cautious, text-only interpretation layer. It does not
 * inspect raw audio, images, camera frames, other people, or hidden mental states.
 */
public final class HumanIntelligenceRules {
    public enum Signal {
        SADNESS, STRESS, JOY, LONELINESS, FRUSTRATION, NONE
    }

    public static final class Cue {
        public final Signal signal;
        public final String confidence;

        private Cue(Signal signal, String confidence) {
            this.signal = signal;
            this.confidence = confidence;
        }
    }

    private HumanIntelligenceRules() {}

    public static Cue classifyExplicitTextCue(String message) {
        String text = normalize(message);
        if (text.isEmpty()) return new Cue(Signal.NONE, "none");

        if (matchesAny(text, "udaas hoon", "i am sad", "i feel sad", "dil kharab hai",
                "mann nahi lag", "man nahi lag", "mann nahi lag raha", "man nahi lag raha", "rona aa raha hai", "rona aa raha")) {
            return new Cue(Signal.SADNESS, "tentative");
        }
        if (matchesAny(text, "tension ho rahi", "tension hai", "pareshan hoon",
                "ghabra", "ghabra raha", "ghabrahat ho rahi", "darr lag", "dar lag", "darr lag raha", "dar lag raha",
                "i am anxious", "i feel overwhelmed")) {
            return new Cue(Signal.STRESS, "tentative");
        }
        if (matchesAny(text, "khush hoon", "bahut khush", "bohot khush",
                "happy hoon", "i am happy", "i feel happy", "good news", "good news hai")) {
            return new Cue(Signal.JOY, "tentative");
        }
        if (matchesAny(text, "akela feel", "akela hoon", "lonely", "lonely hoon", "koi nahi hai",
                "i feel alone", "koi nahi sunta")) {
            return new Cue(Signal.LONELINESS, "tentative");
        }
        if (matchesAny(text, "frustrated hoon", "gussa aa raha", "irritated hoon",
                "bahut irritate", "fed up hoon")) {
            return new Cue(Signal.FRUSTRATION, "tentative");
        }
        return new Cue(Signal.NONE, "none");
    }

    public static String buildLocalSupportiveReply(String message) {
        Cue cue = classifyExplicitTextCue(message);
        switch (cue.signal) {
            case SADNESS:
                return "Tumhari baat se lag raha hai ki abhi waqt mushkil ho sakta hai—agar main galat samjha toh bata dena. Main dhyan se sununga. Kya tum bas baat karna chahte ho, ya milkar koi practical raasta sochen?";
            case STRESS:
                return "Lagta hai abhi tension ya ghabrahat ho sakti hai. Pehle ek pal rukte hain—kya tum bata sakte ho ki sabse zyada pareshani kis baat ki hai?";
            case JOY:
                return "Arey wah, ye sunkar achha laga! Kya good news hai? Batao, main sun raha hoon.";
            case LONELINESS:
                return "Aisa lag raha hai ki tum akelapan mehsoos kar rahe ho—agar main galat samjha toh correct karna. Jitna comfortable ho utna batao; kisi bharosemand insaan se baat karna bhi madadgar ho sakta hai.";
            case FRUSTRATION:
                return "Samajh raha hoon ki baat frustrating lag rahi hai. Chalo pehle issue ko alag-alag dekhte hain—sabse pehle kya hua?";
            default:
                return null;
        }
    }

    public static String buildGuidance(String mode, String currentMessage) {
        Cue cue = classifyExplicitTextCue(currentMessage);
        StringBuilder guidance = new StringBuilder();
        guidance.append("\nM02 HUMAN INTELLIGENCE RULES:\n")
                .append("- Match the user's language, script and conversational pace; understand Hinglish, code-switching and informal wording respectfully.\n")
                .append("- Respond empathetically and proportionately. Acknowledge what the user actually said, do not claim to literally feel emotions, and ask whether they want listening or practical help when useful.\n")
                .append("- Treat emotion cues as uncertain interpretations, not facts or diagnoses. Let the user correct the interpretation; do not infer emotion from a single ambiguous word.\n")
                .append("- For social or relationship questions, separate observable events from assumptions, offer balanced possible explanations, and never claim certainty about another person's hidden thoughts, motives, honesty or character.\n")
                .append("- For body-language questions, describe only observable features in owner-selected media or an explicitly started camera session. Never infer truthfulness, intent or personality from appearance alone.\n")
                .append("- This helper analyses only the current text/transcript. Do not imply raw audio, camera, image or third-party data was analysed; do not persist emotion labels.\n")
                .append("- Respect the selected assistant mode (").append(safeMode(mode))
                .append(") without allowing humour to override a serious or sensitive topic.\n");
        if (cue.signal != Signal.NONE) {
            guidance.append("- Current text contains a possible ").append(cue.signal.name().toLowerCase(Locale.ROOT))
                    .append(" cue; confidence is tentative. Acknowledge it gently only if relevant and allow correction.\n");
        }
        return guidance.toString();
    }

    private static String safeMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) return "Normal";
        String cleaned = mode.trim().replaceAll("[^A-Za-z ]", "");
        return cleaned.isEmpty() ? "Normal" : cleaned.substring(0, Math.min(24, cleaned.length()));
    }

    private static boolean matchesAny(String text, String... phrases) {
        for (String phrase : phrases) {
            int index = text.indexOf(phrase);
            while (index >= 0) {
                if (!isNegated(text, index, phrase.length())) return true;
                index = text.indexOf(phrase, index + phrase.length());
            }
        }
        return false;
    }

    private static boolean isNegated(String text, int phraseIndex, int phraseLength) {
        String prefix = text.substring(Math.max(0, phraseIndex - 24), phraseIndex).trim();
        int suffixEnd = Math.min(text.length(), phraseIndex + phraseLength + 24);
        String suffix = text.substring(phraseIndex + phraseLength, suffixEnd).trim();
        boolean negatedBefore = prefix.matches(".*(?:\\bnahi|\\bnahin|\\bnot|\\bnever|\\bno)\\s*$");
        boolean negatedAfter = suffix.matches("^(?:nahi|nahin|not|never|no)\\b.*");
        return negatedBefore || negatedAfter;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
