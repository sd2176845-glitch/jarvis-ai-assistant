package com.jarvis.assistant;

/** Pure local intent classifier. It never performs device actions. */
public final class OfflineCommandClassifier {
    private OfflineCommandClassifier() {}

    public static String classify(String spokenText) {
        String text = spokenText == null ? "" : spokenText.trim().toLowerCase(java.util.Locale.ROOT);
        if (text.contains("battery")) return "DEVICE_BATTERY";
        if (text.contains("time") || text.contains("samay")) return "DEVICE_TIME";
        if (text.contains("date") || text.contains("tarikh")) return "DEVICE_DATE";
        if (text.contains("note")) return "LOCAL_NOTE";
        if (text.contains("settings")) return "OPEN_SETTINGS";
        if (text.contains("call") || text.contains("message") || text.contains("payment"))
            return "NEEDS_ONLINE_OR_OWNER_CONFIRMATION";
        return "UNKNOWN_OFFLINE_COMMAND";
    }
}
