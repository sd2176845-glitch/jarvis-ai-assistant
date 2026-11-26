package com.jarvis.assistant;

import java.util.Locale;

/** Search helpers for owner-controlled local memory records. */
public final class MemorySearchRules {
    private MemorySearchRules() { }

    public static boolean matches(String query, String... fields) {
        String needle = normalize(query);
        if (needle.isEmpty() || fields == null) return false;
        for (String field : fields) {
            if (!normalize(field).isEmpty() && normalize(field).contains(needle)) return true;
        }
        return false;
    }

    public static String preview(String value, int maxLength) {
        if (value == null) return "";
        String clean = value.trim().replaceAll("\\s+", " ");
        if (maxLength <= 0 || clean.length() <= maxLength) return clean;
        return clean.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
