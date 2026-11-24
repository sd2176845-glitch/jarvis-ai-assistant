package com.jarvis.assistant;

import java.net.URL;

/** Safe formatting and URL validation for owner-invoked NDMA SACHET CAP alerts. */
public final class SachetAlertRules {
    private SachetAlertRules() { }

    public static final String FEED_URL =
            "https://sachet.ndma.gov.in/cap_public_website/rss/rss_india.xml";
    public static final String PORTAL_URL = "https://sachet.ndma.gov.in/";

    /** CAP links are taken from RSS but are still treated as untrusted input. */
    public static boolean isTrustedCapUrl(String value) {
        if (value == null || value.length() > 2048) return false;
        try {
            URL url = new URL(value);
            if (!"https".equalsIgnoreCase(url.getProtocol())) return false;
            if (!"sachet.ndma.gov.in".equalsIgnoreCase(url.getHost())) return false;
            if (!"/cap_public_website/FetchXMLFile".equals(url.getPath())) return false;
            String query = url.getQuery();
            return query != null && query.matches("(?i).*(^|&)identifier=[A-Za-z0-9_-]{1,160}(&|$).*");
        } catch (Exception e) {
            return false;
        }
    }

    public static String clean(String value, int maxLength) {
        if (value == null) return "";
        String cleaned = value.replace((char) 10, ' ')
                .replace((char) 13, ' ')
                .replace((char) 9, ' ')
                .replaceAll("\\s+", " ").trim();
        if (cleaned.length() > maxLength) cleaned = cleaned.substring(0, maxLength);
        return cleaned;
    }

    public static String displayValue(String value) {
        String cleaned = clean(value, 700);
        return cleaned.isEmpty() ? "Not supplied by the alert" : cleaned;
    }

    public static String formatAlert(String rssTitle, String publishedAt, String sourceLink,
                                     String sender, String sent, String status, String messageType,
                                     String event, String urgency, String severity, String certainty,
                                     String effective, String expires, String headline,
                                     String description, String instruction, String areas) {
        String title = clean(headline, 180);
        if (title.isEmpty()) title = clean(rssTitle, 180);
        StringBuilder out = new StringBuilder();
        out.append("OFFICIAL SACHET CAP ALERT\n\n");
        out.append("Headline: ").append(displayValue(title)).append("\n");
        out.append("Event: ").append(displayValue(event)).append("\n");
        out.append("Severity: ").append(displayValue(severity)).append("\n");
        out.append("Urgency: ").append(displayValue(urgency)).append("\n");
        out.append("Certainty: ").append(displayValue(certainty)).append("\n");
        out.append("Affected area: ").append(displayValue(areas)).append("\n\n");
        out.append("Issued by: ").append(displayValue(sender)).append("\n");
        out.append("Sent: ").append(displayValue(sent)).append("\n");
        out.append("Feed publication time: ").append(displayValue(publishedAt)).append("\n");
        out.append("Effective: ").append(displayValue(effective)).append("\n");
        out.append("Expires: ").append(displayValue(expires)).append("\n");
        out.append("Status / message type: ").append(displayValue(status))
                .append(" / ").append(displayValue(messageType)).append("\n\n");
        out.append("Description: ").append(displayValue(description)).append("\n\n");
        out.append("Official instruction: ").append(displayValue(instruction)).append("\n\n");
        out.append("Source: NDMA SACHET Common Alerting Protocol feed.\n");
        out.append("Open the official SACHET portal to confirm the latest status, affected locations and instructions. ");
        out.append("This app displays a read-only copy retrieved on request; it does not guarantee delivery, completeness or freshness.");
        if (isTrustedCapUrl(sourceLink)) {
            out.append("\n\nOfficial CAP record: ").append(sourceLink);
        }
        return out.toString();
    }
}
