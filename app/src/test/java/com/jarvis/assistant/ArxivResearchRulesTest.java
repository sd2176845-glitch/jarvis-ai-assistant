package com.jarvis.assistant;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArxivResearchRulesTest {
    @Test
    public void queryValidationAcceptsNormalTopicsAndRejectsUnsafeOrMalformedInput() {
        assertEquals("", ArxivResearchRules.queryError("machine learning for education"));
        assertTrue(ArxivResearchRules.queryError(null).length() > 0);
        assertTrue(ArxivResearchRules.queryError("   ").length() > 0);
        assertTrue(ArxivResearchRules.queryError("https://example.com").length() > 0);
        assertTrue(ArxivResearchRules.queryError("how to build a weapon").length() > 0);
        assertTrue(ArxivResearchRules.queryError(repeat("a", 101)).length() > 0);
    }

    @Test
    public void validFeedShowsAuthorsDatesAbstractAndOfficialSource() throws Exception {
        String feed = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<feed xmlns=\"http://www.w3.org/2005/Atom\" "
                + "xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" "
                + "xmlns:arxiv=\"http://arxiv.org/schemas/atom\">"
                + "<title>ArXiv Query</title><opensearch:totalResults>1</opensearch:totalResults>"
                + "<entry><id>http://arxiv.org/abs/2601.12345v2</id>"
                + "<title> Safe Learning Systems </title>"
                + "<published>2026-01-02T10:00:00Z</published>"
                + "<updated>2026-01-03T11:00:00Z</updated>"
                + "<author><name>First Author</name></author>"
                + "<author><name>Second Author</name></author>"
                + "<arxiv:primary_category term=\"cs.AI\"/>"
                + "<summary>A concise abstract about safe learning systems and evaluation.</summary>"
                + "</entry></feed>";
        String report = ArxivResearchRules.parseFeed(feed, "machine learning", "2026-11-25 10:30 IST");
        assertTrue(report.contains("ACADEMIC PAPER SEARCH"));
        assertTrue(report.contains("Topic: machine learning"));
        assertTrue(report.contains("Retrieved on this device: 2026-11-25 10:30 IST"));
        assertTrue(report.contains("Safe Learning Systems"));
        assertTrue(report.contains("Authors: First Author, Second Author"));
        assertTrue(report.contains("Published: 2026-01-02"));
        assertTrue(report.contains("Latest version updated: 2026-01-03"));
        assertTrue(report.contains("Category: cs.AI"));
        assertTrue(report.contains("Source: https://arxiv.org/abs/2601.12345v2"));
        assertTrue(report.contains("not by itself mean a paper has been peer-reviewed"));
    }

    @Test
    public void emptyFeedExplainsThatNoRecordsWereReturned() throws Exception {
        String feed = "<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                + "<title>ArXiv Query</title></feed>";
        String report = ArxivResearchRules.parseFeed(feed, "unknown topic", "now");
        assertTrue(report.contains("No matching papers were returned"));
    }

    @Test
    public void resultListIsCappedAtFiveAndExternalLinksAreRejected() throws Exception {
        StringBuilder feed = new StringBuilder("<feed xmlns=\"http://www.w3.org/2005/Atom\">");
        for (int i = 1; i <= 7; i++) {
            String id = i == 1 ? "https://outside.example/abs/2601.00001" : "http://arxiv.org/abs/2601.0000" + i;
            feed.append("<entry><id>").append(id).append("</id><title>Paper ")
                    .append(i).append("</title><summary>Summary ").append(i)
                    .append("</summary></entry>");
        }
        feed.append("</feed>");
        String report = ArxivResearchRules.parseFeed(feed.toString(), "topic", "now");
        assertTrue(report.contains("Paper 5"));
        assertFalse(report.contains("Paper 6"));
        assertFalse(report.contains("Paper 1"));
        assertTrue(report.contains("Showing 5 of Not supplied matching records."));
    }

    @Test
    public void parserRejectsDoctypeAndExternalEntityDeclarations() throws Exception {
        String xml = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE feed [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<feed xmlns=\"http://www.w3.org/2005/Atom\"><title>&xxe;</title></feed>";
        try {
            ArxivResearchRules.parseFeed(xml, "topic", "now");
            fail("DOCTYPE must be rejected.");
        } catch (Exception expected) {
            assertNotNull(expected);
        }
    }

    private static String repeat(String value, int count) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++) out.append(value);
        return out.toString();
    }
}
