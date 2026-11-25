package com.jarvis.assistant;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/** Bounded, source-preserving parsing rules for owner-invoked arXiv paper searches. */
public final class ArxivResearchRules {
    private static final String ATOM_NS = "http://www.w3.org/2005/Atom";
    private static final String OPEN_SEARCH_NS = "http://a9.com/-/spec/opensearch/1.1/";
    private static final String ARXIV_NS = "http://arxiv.org/schemas/atom";
    private static final int MAX_XML_CHARS = 500000;
    private static final int MAX_RESULTS = 5;
    private static final int MAX_ABSTRACT_CHARS = 850;

    private ArxivResearchRules() { }

    /** Returns an empty string when the query is acceptable, otherwise a user-safe reason. */
    public static String queryError(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Enter a research topic first.";
        String query = clean(raw, 120);
        if (query.isEmpty()) return "Enter a research topic first.";
        if (query.length() > 100) return "Keep the research topic under 100 characters.";
        String lower = query.toLowerCase(Locale.ROOT);
        if (lower.contains("://") || lower.startsWith("www.")
                || query.indexOf('\n') >= 0 || query.indexOf('\r') >= 0) {
            return "Enter a topic or phrase, not a URL or multi-line text.";
        }
        if (!isAllowedResearchQuery(lower)) {
            return "This search is not available. Choose a safe educational research topic.";
        }
        return "";
    }

    private static boolean isAllowedResearchQuery(String lower) {
        String normalized = lower.replaceAll("[^a-z0-9]+", " ").trim();
        String padded = " " + normalized + " ";
        String[] blocked = {
                "weapon", "firearm", "gun", "bomb", "explosive", "grenade",
                "weaponization", "missile guidance", "drug synthesis",
                "poison preparation", "malware", "ransomware", "credential theft",
                "exploit development", "unauthorized intrusion", "stealth attack"
        };
        for (String marker : blocked) {
            if (padded.contains(" " + marker + " ")) return false;
        }
        return true;
    }

    public static String normalizeQuery(String raw) {
        return clean(raw, 100);
    }

    /**
     * Parses only the small Atom metadata response requested by the app.
     * DOCTYPE declarations and external entities are rejected to avoid XML entity expansion.
     */
    public static String parseFeed(String xml, String query, String retrievedAt) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("The arXiv response was empty.");
        }
        if (xml.length() > MAX_XML_CHARS) {
            throw new IllegalArgumentException("The arXiv response exceeded the size limit.");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) ->
                new InputSource(new StringReader("")));
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element root = document.getDocumentElement();
        if (root == null) throw new IllegalArgumentException("The arXiv feed had no root element.");

        NodeList entries = root.getElementsByTagNameNS(ATOM_NS, "entry");
        if (entries == null || entries.getLength() == 0) {
            entries = root.getElementsByTagName("entry");
        }

        String safeQuery = clean(query, 100);
        String safeRetrieved = clean(retrievedAt, 50);
        StringBuilder out = new StringBuilder("ACADEMIC PAPER SEARCH • arXiv\n");
        out.append("Topic: ").append(safeQuery.isEmpty() ? "Not supplied" : safeQuery).append("\n");
        out.append("Retrieved on this device: ")
                .append(safeRetrieved.isEmpty() ? "Time unavailable" : safeRetrieved).append("\n");

        if (entries == null || entries.getLength() == 0) {
            return out.append("\nNo matching papers were returned for this topic. Try a broader phrase.")
                    .append("\n\nSource: arXiv public API.").toString();
        }

        Element firstEntry = entries.getLength() > 0 && entries.item(0) instanceof Element
                ? (Element) entries.item(0) : null;
        if (firstEntry != null && "Error".equalsIgnoreCase(childText(firstEntry, ATOM_NS, "title"))) {
            String message = childText(firstEntry, ATOM_NS, "summary");
            return out.append("\\narXiv could not process this search.")
                    .append(message.isEmpty() ? "" : "\\nProvider message: " + clean(message, 240))
                    .toString();
        }

        String total = childText(root, OPEN_SEARCH_NS, "totalResults");
        if (!total.matches("[0-9]{1,12}")) total = "Not supplied";

        int shown = 0;
        for (int i = 0; i < entries.getLength() && shown < MAX_RESULTS; i++) {
            Node node = entries.item(i);
            if (!(node instanceof Element)) continue;
            Element entry = (Element) node;
            String title = clean(childText(entry, ATOM_NS, "title"), 220);
            String source = canonicalAbsUrl(childText(entry, ATOM_NS, "id"));
            if (title.isEmpty() || source.isEmpty()) continue;

            shown++;
            out.append("\n\n").append(shown).append(". ").append(title).append("\n");
            String authors = authors(entry);
            out.append("Authors: ").append(authors.isEmpty() ? "Not supplied" : authors).append("\n");
            out.append("Published: ").append(dateOnly(childText(entry, ATOM_NS, "published"))).append("\n");
            out.append("Latest version updated: ").append(dateOnly(childText(entry, ATOM_NS, "updated"))).append("\n");

            String category = primaryCategory(entry);
            if (!category.isEmpty()) out.append("Category: ").append(category).append("\n");

            String abstractText = clean(childText(entry, ATOM_NS, "summary"), MAX_ABSTRACT_CHARS);
            if (abstractText.isEmpty()) abstractText = "Abstract not supplied.";
            else if (abstractText.length() >= MAX_ABSTRACT_CHARS) abstractText += "…";
            out.append("Abstract: ").append(abstractText).append("\n");
            out.append("Source: ").append(source);
        }

        if (shown == 0) {
            out.append("\n\nThe provider returned entries, but no usable paper records or official arXiv links were found.");
        } else {
            out.append("\n\nShowing ").append(shown).append(" of ").append(total).append(" matching records.");
        }
        out.append("\n\nResearch note: arXiv hosts scholarly preprints. A listing or update date does not by itself mean a paper has been peer-reviewed, accepted, or independently verified.");
        out.append("\nOnly paper metadata and abstracts are shown; PDFs are not downloaded.");
        return out.toString();
    }

    private static String authors(Element entry) {
        NodeList nodes = entry.getElementsByTagNameNS(ATOM_NS, "author");
        if (nodes == null || nodes.getLength() == 0) nodes = entry.getElementsByTagName("author");
        if (nodes == null || nodes.getLength() == 0) return "";
        List<String> names = new ArrayList<>();
        int total = nodes.getLength();
        for (int i = 0; i < Math.min(total, 3); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element)) continue;
            String name = clean(childText((Element) node, ATOM_NS, "name"), 90);
            if (!name.isEmpty()) names.add(name);
        }
        StringBuilder result = new StringBuilder();
        for (String name : names) {
            if (result.length() > 0) result.append(", ");
            result.append(name);
        }
        if (total > names.size()) {
            if (result.length() > 0) result.append(" ");
            result.append("and ").append(total - names.size()).append(" more");
        }
        return clean(result.toString(), 360);
    }

    private static String primaryCategory(Element entry) {
        NodeList nodes = entry.getElementsByTagNameNS(ARXIV_NS, "primary_category");
        if (nodes == null || nodes.getLength() == 0) return "";
        Node node = nodes.item(0);
        if (!(node instanceof Element)) return "";
        Node term = node.getAttributes() == null ? null : node.getAttributes().getNamedItem("term");
        return term == null ? "" : clean(term.getNodeValue(), 50);
    }

    private static String childText(Element parent, String namespace, String name) {
        if (parent == null) return "";
        NodeList nodes = parent.getElementsByTagNameNS(namespace, name);
        if (nodes == null || nodes.getLength() == 0) nodes = parent.getElementsByTagName(name);
        if (nodes == null || nodes.getLength() == 0) return "";
        Node node = nodes.item(0);
        return node == null ? "" : node.getTextContent();
    }

    private static String canonicalAbsUrl(String raw) {
        try {
            URI uri = new URI(raw == null ? "" : raw.trim());
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null || !("arxiv.org".equalsIgnoreCase(host)
                    || "www.arxiv.org".equalsIgnoreCase(host))) return "";
            if (path == null || !path.startsWith("/abs/")) return "";
            String id = path.substring(5);
            if (id.isEmpty() || !id.matches("[A-Za-z0-9._/-]{3,100}")) return "";
            return "https://arxiv.org/abs/" + id;
        } catch (Exception e) {
            return "";
        }
    }

    private static String dateOnly(String raw) {
        String value = clean(raw, 40);
        if (value.length() >= 10 && value.substring(0, 10).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            return value.substring(0, 10);
        }
        return "Not supplied";
    }

    private static String clean(String value, int max) {
        if (value == null) return "";
        String result = value.replaceAll("[\\p{Cntrl}&&[^\\t]]", " ")
                .replaceAll("\\s+", " ").trim();
        return result.length() > max ? result.substring(0, max) : result;
    }
}
