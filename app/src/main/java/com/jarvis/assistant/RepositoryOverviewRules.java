package com.jarvis.assistant;

/** Restricts repository overview input to public GitHub owner/repository identifiers. */
public final class RepositoryOverviewRules {
    private RepositoryOverviewRules() { }

    public static String normalize(String input) {
        if (input == null) return null;
        String value = input.trim();
        value = value.replaceFirst("(?i)^https?://github\\.com/", "");
        value = value.replaceFirst("^github\\.com/", "");
        value = value.replaceAll("[?#].*$", "");
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        if (value.endsWith(".git")) value = value.substring(0, value.length() - 4);
        if (!value.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) return null;
        return value;
    }
}
