package com.jarvis.assistant;

/** Pure local intent classifier. It never performs device actions. */
public final class OfflineCommandClassifier {
    private OfflineCommandClassifier() {}

    public static String classify(String spokenText) {
        String text = spokenText == null ? "" : spokenText.trim().toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^\\\\p{L}\\\\p{N}\\\\s]", " ")
                .replaceAll("\\\\s+", " ").trim();
        if (text.isEmpty()) return "UNKNOWN_OFFLINE_COMMAND";

        // Keep phone and message commands local so names and draft text do not need
        // to be sent to the transcript-correction endpoint before routing.
        if (startsWithAny(text, "call ", "call", "phone karo", "phone lagao", "ko phone karo",
                "ko call karo", "message ", "sms ", "text ", "make a payment", "pay ")) {
            return "NEEDS_ONLINE_OR_OWNER_CONFIRMATION";
        }

        if (startsWithAny(text, "battery", "charge kitna", "battery kitna", "बैटरी", "चार्ज")) {
            return "DEVICE_BATTERY";
        }
        if (startsWithAny(text, "time", "what time", "kitne baje", "samay", "समय", "कितने बजे")) {
            return "DEVICE_TIME";
        }
        if (startsWithAny(text, "date", "aaj ki date", "tarikh", "तारीख", "दिनांक")) {
            return "DEVICE_DATE";
        }

        if (startsWithAny(text, "remember ", "remember that ", "what do you remember",
                "meri memory", "kya yaad hai", "clear memory", "forget my memory",
                "memory delete", "sab yaad bhool", "show my note", "show my notes",
                "show notes", "notes kholo", "note kholo", "notes")) {
            return "LOCAL_NOTE";
        }

        if (startsWithAny(text, "open settings", "settings kholo", "phone settings", "settings",
                "wifi settings", "wifi kholo", "wi fi settings", "bluetooth settings",
                "bluetooth kholo", "location settings", "location kholo")) {
            return "OPEN_SETTINGS";
        }

        if (startsWithAny(text, "open youtube", "youtube kholo", "youtube chalao", "youtube",
                "open chrome", "chrome kholo", "chrome",
                "open whatsapp", "whatsapp kholo", "whatsapp",
                "open instagram", "instagram kholo", "instagram",
                "open facebook", "facebook kholo", "facebook",
                "open camera", "camera kholo", "camera",
                "open maps", "maps kholo", "google maps", "map kholo",
                "open calculator", "calculator kholo", "calculator",
                "open calendar", "calendar kholo", "calendar",
                "open clock", "clock kholo", "ghadi kholo",
                "open phone", "dialer kholo", "call screen kholo", "dialer",
                "open contacts", "contacts kholo", "contact kholo")) {
            return "LOCAL_COMMAND";
        }

        if (startsWithAny(text, "set alarm", "alarm lagao", "alarm kholo",
                "search ", "search for ", "google ",
                "open vision", "vision kholo", "google lens", "scan image",
                "show tasks", "task list", "reminders", "reminder kholo",
                "connected systems", "quick controls", "system controls",
                "study mode", "padhai mode", "gaming mode", "game mode",
                "travel mode", "safar mode", "night mode", "raat mode",
                "caring mode", "funny mode", "serious mode", "normal mode",
                "calm mode", "shaant mode", "energetic mode", "fast voice",
                "normal voice", "help commands", "what can you do", "commands list",
                "joke", "jokes", "mazaak", "motivate", "motivation", "himmat de",
                "sos", "emergency options", "sos options", "set emergency contact",
                "who are you", "your name", "tum kaun ho", "tera naam")) {
            return "LOCAL_COMMAND";
        }

        return "UNKNOWN_OFFLINE_COMMAND";
    }

    private static boolean startsWithAny(String text, String... prefixes) {
        for (String prefix : prefixes) {
            if (text.equals(prefix) || text.startsWith(prefix + " ")) return true;
        }
        return false;
    }
}
