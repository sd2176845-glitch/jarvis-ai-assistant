package com.jarvis.assistant;

import org.junit.Test;

import static org.junit.Assert.*;

public class SachetAlertRulesTest {
    @Test
    public void capLinksMustUseExactOfficialHttpsHostAndPath() {
        assertTrue(SachetAlertRules.isTrustedCapUrl(
                "https://sachet.ndma.gov.in/cap_public_website/FetchXMLFile?identifier=ABC123"));
        assertFalse(SachetAlertRules.isTrustedCapUrl(
                "http://sachet.ndma.gov.in/cap_public_website/FetchXMLFile?identifier=ABC123"));
        assertFalse(SachetAlertRules.isTrustedCapUrl(
                "https://sachet.ndma.gov.in.evil.example/cap_public_website/FetchXMLFile?identifier=ABC123"));
        assertFalse(SachetAlertRules.isTrustedCapUrl(
                "https://example.com/cap_public_website/FetchXMLFile?identifier=ABC123"));
        assertFalse(SachetAlertRules.isTrustedCapUrl(
                "https://sachet.ndma.gov.in/other?identifier=ABC123"));
        assertFalse(SachetAlertRules.isTrustedCapUrl(
                "https://sachet.ndma.gov.in/cap_public_website/FetchXMLFile"));
        assertFalse(SachetAlertRules.isTrustedCapUrl(null));
    }

    @Test
    public void alertDetailsAreSourceLabelledAndPreserveOfficialFields() {
        String detail = SachetAlertRules.formatAlert(
                "RSS headline", "Tue, 24 Nov 2026 12:00:00 GMT",
                "https://sachet.ndma.gov.in/cap_public_website/FetchXMLFile?identifier=ABC123",
                "West-Bengal-SDMA", "2026-11-24T12:00:00+05:30", "Actual", "Alert",
                "Flood", "Immediate", "Severe", "Observed", "2026-11-24T12:00:00+05:30",
                "2026-11-24T18:00:00+05:30", "River flood warning", "Flooding expected in low-lying areas",
                "Follow local authority instructions", "District A, West Bengal");
        assertTrue(detail.contains("OFFICIAL SACHET CAP ALERT"));
        assertTrue(detail.contains("River flood warning"));
        assertTrue(detail.contains("Severity: Severe"));
        assertTrue(detail.contains("Urgency: Immediate"));
        assertTrue(detail.contains("District A, West Bengal"));
        assertTrue(detail.contains("NDMA SACHET Common Alerting Protocol feed"));
        assertTrue(detail.contains("official SACHET portal"));
    }

    @Test
    public void absentAlertFieldsAreShownAsUnavailableAndTextIsSanitized() {
        String detail = SachetAlertRules.formatAlert(
                "Feed\\nheadline", "", "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "");
        assertTrue(detail.contains("Feed headline"));
        assertTrue(detail.contains("Not supplied by the alert"));
        assertFalse(detail.contains("Feed\\nheadline"));
    }
}
