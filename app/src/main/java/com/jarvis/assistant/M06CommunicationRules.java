package com.jarvis.assistant;

/** Safe parsing and validation for owner-reviewed M06 communication handoffs. */
public final class M06CommunicationRules {
    private M06CommunicationRules() { }

    public static final class AddressedDraft {
        public final String recipient;
        public final String body;
        AddressedDraft(String recipient, String body) {
            this.recipient = recipient;
            this.body = body;
        }
    }

    private static String stripWakePrefix(String command) {
        if (command == null) return "";
        return command.trim().replaceFirst(
                "(?i)^(?:(?:hey|hi|hello|oye|ok|okay|please)\\s+)*(?:jarvis|jarvesh|jarvish)\\s*", "");
    }

    public static String removeCommandPrefix(String command, String[] aliases) {
        String value = stripWakePrefix(command);
        if (aliases == null) return value;
        for (String alias : aliases) {
            if (alias != null && value.regionMatches(true, 0, alias, 0, alias.length())
                    && value.length() > alias.length()
                    && Character.isWhitespace(value.charAt(alias.length()))) {
                return value.substring(alias.length()).trim();
            }
        }
        return value;
    }

    public static AddressedDraft parseAddressedDraft(String command, String[] aliases) {
        String value = removeCommandPrefix(command, aliases);
        int to = value.toLowerCase(java.util.Locale.ROOT).indexOf("to ");
        if (to < 0) return null;
        int colon = value.indexOf(':', to + 3);
        if (colon < 0) return null;
        String recipient = value.substring(to + 3, colon).trim();
        String body = value.substring(colon + 1).trim();
        return new AddressedDraft(recipient, body);
    }

    public static boolean isValidEmailAddress(String address) {
        if (address == null || address.length() > 254) return false;
        String value = address.trim();
        return value.matches("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$");
    }

    /** Normalizes an SMS destination without guessing a country code. */
    public static String normalizeSmsNumber(String phone) {
        if (phone == null) return null;
        String value = phone.trim().replaceAll("[^0-9+]", "");
        if (value.startsWith("+")) return "+" + value.substring(1).replace("+", "");
        return value.replace("+", "");
    }

    public static boolean isValidSmsNumber(String number) {
        if (number == null || !number.matches("\\+?[0-9]+")) return false;
        String digits = number.replaceAll("[^0-9]", "");
        return digits.length() >= 5 && digits.length() <= 15;
    }

    public static boolean canSendSms(boolean ownerConsent, boolean androidPermission,
                                     String number, boolean validDraft) {
        return ownerConsent && androidPermission && isValidSmsNumber(number) && validDraft;
    }

    /** Returns a wa.me-compatible number, or null when it is not usable. */
    public static String normalizeWhatsAppNumber(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() >= 8 && digits.length() <= 15 ? digits : null;
    }
}
