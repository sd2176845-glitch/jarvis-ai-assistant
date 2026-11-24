package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Owner-invoked, read-only NDMA SACHET alert reader.
 * It performs no location access, polling, notification delivery or emergency action.
 */
public final class SachetAlertStudio {
    private SachetAlertStudio() { }

    private static final String PREFS = "jarvis_preferences";
    private static final String CACHE_PREFIX = "sachet_cap_xml_v1_";
    private static final int MAX_FEED_ITEMS = 12;
    private static final int MAX_XML_CHARS = 300000;

    private static final class FeedItem {
        final String title;
        final String publishedAt;
        final String link;

        FeedItem(String title, String publishedAt, String link) {
            this.title = title;
            this.publishedAt = publishedAt;
            this.link = link;
        }
    }

    private static final class AlertItem {
        String title;
        String publishedAt;
        String link;
        String detail;
        String area;
    }

    private static final class HttpResult {
        int status;
        String body;
        String etag;
    }

    public static void show(Activity activity) {
        EditText filter = new EditText(activity);
        filter.setSingleLine(true);
        filter.setHint("Optional state / district keyword (blank = All India)");
        filter.setMaxLines(1);
        new AlertDialog.Builder(activity)
                .setTitle("NDMA SACHET • Official Disaster Alerts")
                .setMessage("Fetch the latest entries from the official All-India CAP feed. A state/district filter is applied locally after CAP details are read. No GPS or background monitoring is used.")
                .setView(filter)
                .setPositiveButton("Load alerts", (dialog, which) -> {
                    String query = filter.getText().toString().trim();
                    if (query.length() > 60 || query.contains("://")
                            || query.indexOf('\n') >= 0 || query.indexOf('\r') >= 0) {
                        Toast.makeText(activity, "Enter a short state or district name.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    loadAlerts(activity, query);
                })
                .setNeutralButton("Open SACHET", (dialog, which) -> openOfficialPortal(activity))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void loadAlerts(Activity activity, String filter) {
        Toast.makeText(activity, "Fetching official SACHET alerts…", Toast.LENGTH_SHORT).show();
        Thread worker = new Thread(() -> {
            List<AlertItem> alerts = new ArrayList<>();
            String error = null;
            try {
                HttpResult feedResponse = request(SachetAlertRules.FEED_URL, null, null);
                if (feedResponse.status < 200 || feedResponse.status >= 300) {
                    throw new IllegalStateException("SACHET feed HTTP " + feedResponse.status);
                }
                List<FeedItem> feedItems = parseRss(feedResponse.body);
                int checked = Math.min(MAX_FEED_ITEMS, feedItems.size());
                for (int i = 0; i < checked; i++) {
                    FeedItem feedItem = feedItems.get(i);
                    if (!SachetAlertRules.isTrustedCapUrl(feedItem.link)) continue;

                    String cachedKey = cacheKey(feedItem.link);
                    SharedPreferences preferences = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
                    String previousEtag = preferences.getString(CACHE_PREFIX + cachedKey + "_etag", null);
                    String previousXml = preferences.getString(CACHE_PREFIX + cachedKey + "_xml", null);
                    HttpResult capResponse = request(feedItem.link, previousEtag, previousXml);
                    String capXml;
                    if (capResponse.status == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        capXml = previousXml;
                    } else if (capResponse.status >= 200 && capResponse.status < 300) {
                        capXml = capResponse.body;
                        SharedPreferences.Editor edit = preferences.edit()
                                .putString(CACHE_PREFIX + cachedKey + "_xml", capXml);
                        if (capResponse.etag != null && !capResponse.etag.trim().isEmpty()) {
                            edit.putString(CACHE_PREFIX + cachedKey + "_etag", capResponse.etag);
                        }
                        edit.apply();
                    } else {
                        continue;
                    }
                    if (capXml == null || capXml.trim().isEmpty()) continue;
                    AlertItem parsed = parseCap(capXml, feedItem);
                    if (!filter.isEmpty()) {
                        String search = filter.toLowerCase(Locale.ROOT);
                        String area = parsed.area == null ? "" : parsed.area.toLowerCase(Locale.ROOT);
                        if (!area.contains(search)) continue;
                    }
                    alerts.add(parsed);
                }
                if (alerts.isEmpty()) {
                    error = filter.isEmpty()
                            ? "No readable alerts were returned in the latest " + checked
                                    + " feed entries. Check the official SACHET portal for the live status."
                            : "No matching alert was found among the latest " + checked
                                    + " entries whose CAP details could be read. This does not prove that no alert exists for this area.";
                }
            } catch (Exception e) {
                error = "The official SACHET feed could not be read (" + e.getClass().getSimpleName()
                        + "). Open the portal to verify current alerts. No alert state was changed.";
            }
            final List<AlertItem> result = alerts;
            final String message = error;
            activity.runOnUiThread(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (result.isEmpty()) {
                    new AlertDialog.Builder(activity)
                            .setTitle("SACHET • Alert lookup")
                            .setMessage(message == null ? "No readable alerts were returned." : message)
                            .setPositiveButton("Open SACHET", (d, w) -> openOfficialPortal(activity))
                            .setNegativeButton("Close", null).show();
                } else {
                    showAlertList(activity, result);
                }
            });
        }, "jarvis-sachet-alerts");
        worker.start();
    }

    private static void showAlertList(Activity activity, List<AlertItem> alerts) {
        String[] labels = new String[alerts.size()];
        for (int i = 0; i < alerts.size(); i++) {
            AlertItem alert = alerts.get(i);
            String title = SachetAlertRules.clean(alert.title, 100);
            String area = SachetAlertRules.clean(alert.area, 80);
            labels[i] = title + (area.isEmpty() ? "" : " • " + area);
        }
        new AlertDialog.Builder(activity)
                .setTitle("SACHET • Latest readable alerts (" + alerts.size() + ")")
                .setItems(labels, (dialog, which) -> showAlertDetail(activity, alerts.get(which)))
                .setPositiveButton("Open SACHET", (dialog, which) -> openOfficialPortal(activity))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void showAlertDetail(Activity activity, AlertItem alert) {
        new AlertDialog.Builder(activity)
                .setTitle(SachetAlertRules.clean(alert.title, 120))
                .setMessage(alert.detail)
                .setPositiveButton("Done", null)
                .setNeutralButton("Open official alert", (dialog, which) -> openAlertLink(activity, alert.link))
                .show();
    }

    private static List<FeedItem> parseRss(String xml) throws Exception {
        Document document = parseXml(xml);
        NodeList nodes = elements(document, "item");
        List<FeedItem> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength() && result.size() < 50; i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element)) continue;
            Element item = (Element) node;
            String title = childText(item, "title");
            String published = childText(item, "pubDate");
            String link = childText(item, "link");
            if (!title.isEmpty() && SachetAlertRules.isTrustedCapUrl(link)) {
                result.add(new FeedItem(title, published, link));
            }
        }
        return result;
    }

    private static AlertItem parseCap(String xml, FeedItem feedItem) throws Exception {
        Document document = parseXml(xml);
        AlertItem alert = new AlertItem();
        alert.title = firstText(document, "headline");
        if (alert.title.isEmpty()) alert.title = feedItem.title;
        alert.publishedAt = feedItem.publishedAt;
        alert.link = feedItem.link;

        String sender = firstText(document, "sender");
        String sent = firstText(document, "sent");
        String status = firstText(document, "status");
        String messageType = firstText(document, "msgType");

        NodeList infos = elements(document, "info");
        Element info = infos.getLength() == 0 ? null : (Element) infos.item(0);
        String event = childText(info, "event");
        String urgency = childText(info, "urgency");
        String severity = childText(info, "severity");
        String certainty = childText(info, "certainty");
        String effective = childText(info, "effective");
        String expires = childText(info, "expires");
        String headline = childText(info, "headline");
        String description = childText(info, "description");
        String instruction = childText(info, "instruction");

        List<String> areas = new ArrayList<>();
        if (info != null) {
            NodeList areaNodes = elements(info, "area");
            for (int i = 0; i < areaNodes.getLength(); i++) {
                NodeList descriptions = elements((Element) areaNodes.item(i), "areaDesc");
                for (int j = 0; j < descriptions.getLength(); j++) {
                    String value = descriptions.item(j).getTextContent();
                    value = SachetAlertRules.clean(value, 180);
                    if (!value.isEmpty() && !areas.contains(value)) areas.add(value);
                }
            }
        }
        alert.area = join(areas, "; ");
        alert.detail = SachetAlertRules.formatAlert(feedItem.title, feedItem.publishedAt,
                feedItem.link, sender, sent, status, messageType, event, urgency,
                severity, certainty, effective, expires, headline, description,
                instruction, alert.area);
        return alert;
    }

    private static Document parseXml(String xml) throws Exception {
        if (xml == null || xml.length() > MAX_XML_CHARS) {
            throw new IllegalArgumentException("XML size limit exceeded");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        try { factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true); } catch (Exception ignored) { }
        try { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) { }
        try { factory.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Exception ignored) { }
        try { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Exception ignored) { }
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static NodeList elements(Document document, String name) {
        NodeList nodes = document.getElementsByTagNameNS("*", name);
        if (nodes.getLength() == 0) nodes = document.getElementsByTagName(name);
        return nodes;
    }

    private static NodeList elements(Element element, String name) {
        NodeList nodes = element.getElementsByTagNameNS("*", name);
        if (nodes.getLength() == 0) nodes = element.getElementsByTagName(name);
        return nodes;
    }

    private static String firstText(Document document, String name) {
        NodeList nodes = elements(document, name);
        return nodes.getLength() == 0 ? "" : SachetAlertRules.clean(nodes.item(0).getTextContent(), 700);
    }

    private static String childText(Element parent, String name) {
        if (parent == null) return "";
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                String local = element.getLocalName();
                String tag = local == null ? element.getTagName() : local;
                if (name.equals(tag)) return SachetAlertRules.clean(element.getTextContent(), 700);
            }
        }
        return "";
    }

    private static String join(List<String> values, String separator) {
        StringBuilder out = new StringBuilder();
        for (String value : values) {
            if (out.length() > 0) out.append(separator);
            out.append(value);
        }
        return out.toString();
    }

    private static HttpResult request(String address, String etag, String cachedBody) throws Exception {
        if (SachetAlertRules.isTrustedCapUrl(address)) {
            // CAP XML requests must honor SACHET's published ETag / 304 caching contract.
        } else if (!SachetAlertRules.FEED_URL.equals(address)) {
            throw new SecurityException("Untrusted SACHET URL");
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(address).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml");
            if (etag != null && !etag.trim().isEmpty()) {
                connection.setRequestProperty("If-None-Match", etag);
            }
            HttpResult result = new HttpResult();
            result.status = connection.getResponseCode();
            result.etag = connection.getHeaderField("ETag");
            if (result.status == HttpURLConnection.HTTP_NOT_MODIFIED) {
                result.body = cachedBody;
                return result;
            }
            InputStream stream = result.status >= 200 && result.status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            if (stream != null) {
                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null && body.length() <= MAX_XML_CHARS) {
                        body.append(line).append('\n');
                    }
                }
                result.body = body.toString();
            } else {
                result.body = "";
            }
            return result;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String cacheKey(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < Math.min(12, digest.length); i++) {
            out.append(String.format(Locale.US, "%02x", digest[i] & 0xff));
        }
        return out.toString();
    }

    private static void openOfficialPortal(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SachetAlertRules.PORTAL_URL)));
        } catch (Exception e) {
            Toast.makeText(activity, "No browser is available. Try the official SACHET portal later.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private static void openAlertLink(Activity activity, String link) {
        if (!SachetAlertRules.isTrustedCapUrl(link)) {
            Toast.makeText(activity, "The alert link could not be verified.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {
            Toast.makeText(activity, "No browser is available. The alert details remain visible.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
