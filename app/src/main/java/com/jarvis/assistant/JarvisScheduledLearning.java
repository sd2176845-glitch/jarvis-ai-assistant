package com.jarvis.assistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Opt-in RSS learning from owner-approved HTTPS feeds. */
public final class JarvisScheduledLearning {
    private static final String PREFS = "jarvis_scheduled_learning_owner";
    private static final String FEEDS = "approved_feeds";
    private static final String ITEMS = "saved_items";
    private static final String ENABLED = "enabled";
    private static final int MAX_FEEDS = 5;
    private static final int MAX_ITEMS_PER_FEED = 4;
    private static final int MAX_BYTES = 100 * 1024;

    private JarvisScheduledLearning() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized boolean addApprovedFeed(Context c, String rawUrl) {
        if (c == null || rawUrl == null) return false;
        String url = rawUrl.trim();
        if (!isSafeFeedUrl(url)) return false;
        try {
            JSONArray feeds = new JSONArray(prefs(c).getString(FEEDS, "[]"));
            for (int i = 0; i < feeds.length(); i++) {
                if (url.equals(feeds.getString(i))) return true;
            }
            if (feeds.length() >= MAX_FEEDS) return false;
            feeds.put(url);
            prefs(c).edit().putString(FEEDS, feeds.toString()).apply();
            return true;
        } catch (Exception e) { return false; }
    }

    public static boolean isEnabled(Context c) {
        return prefs(c).getBoolean(ENABLED, false);
    }

    public static int feedCount(Context c) {
        try { return new JSONArray(prefs(c).getString(FEEDS, "[]")).length(); }
        catch (Exception e) { return 0; }
    }

    public static synchronized void setEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(ENABLED, enabled).apply();
        AlarmManager alarms = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;
        PendingIntent pi = refreshIntent(c);
        if (!enabled) {
            alarms.cancel(pi);
            pi.cancel();
            return;
        }
        if (feedCount(c) == 0) return;
        long first = System.currentTimeMillis() + 60_000L;
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, first,
                AlarmManager.INTERVAL_DAY, pi);
    }

    public static PendingIntent refreshIntent(Context c) {
        Intent i = new Intent(c, JarvisScheduledLearningReceiver.class);
        return PendingIntent.getBroadcast(c, 48117, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static String summary(Context c) {
        try {
            JSONArray feeds = new JSONArray(prefs(c).getString(FEEDS, "[]"));
            JSONArray items = new JSONArray(prefs(c).getString(ITEMS, "[]"));
            StringBuilder b = new StringBuilder();
            b.append("Status: ").append(isEnabled(c) ? "ON" : "PAUSED").append("\n");
            b.append("Approved RSS sources: ").append(feeds.length()).append("/").append(MAX_FEEDS);
            b.append("\nSaved learning updates: ").append(items.length()).append("\n\n");
            for (int i = Math.max(0, items.length() - 8); i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                b.append("• ").append(item.optString("title", "Untitled")).append("\n");
                b.append(item.optString("source", "")).append("\n");
                b.append(item.optString("published", "Date not supplied")).append("\n\n");
            }
            if (items.length() == 0) b.append("\nNo updates saved yet. Add an RSS feed and enable scheduled learning.");
            return b.toString();
        } catch (Exception e) { return "Learning data could not be read."; }
    }

    static void refresh(Context c) {
        if (!isEnabled(c)) return;
        JSONArray feeds;
        try { feeds = new JSONArray(prefs(c).getString(FEEDS, "[]")); }
        catch (Exception e) { return; }
        JSONArray saved;
        try { saved = new JSONArray(prefs(c).getString(ITEMS, "[]")); }
        catch (Exception e) { saved = new JSONArray(); }
        for (int i = 0; i < feeds.length() && i < MAX_FEEDS; i++) {
            String feed = feeds.optString(i, "");
            try {
                List<JSONObject> found = readFeed(feed);
                for (JSONObject item : found) {
                    item.put("source", Uri.parse(feed).getHost());
                    boolean duplicate = false;
                    String link = item.optString("link", "");
                    for (int j = 0; j < saved.length(); j++) {
                        if (!link.isEmpty() && link.equals(saved.getJSONObject(j).optString("link", ""))) {
                            duplicate = true; break;
                        }
                    }
                    if (!duplicate) saved.put(item);
                }
            } catch (Exception ignored) { /* One unavailable feed must not stop other approved feeds. */ }
        }
        JSONArray compact = new JSONArray();
        int start = Math.max(0, saved.length() - 40);
        for (int i = start; i < saved.length(); i++) compact.put(saved.opt(i));
        prefs(c).edit().putString(ITEMS, compact.toString())
                .putLong("last_refresh_ms", System.currentTimeMillis()).apply();
    }

    private static List<JSONObject> readFeed(String feed) throws Exception {
        URL url = new URL(feed);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(7000);
        conn.setRequestProperty("User-Agent", "JARVIS-ScheduledLearning/1.0");
        conn.setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml");
        try {
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) return new ArrayList<>();
            int length = conn.getContentLength();
            if (length > MAX_BYTES) return new ArrayList<>();
            try (InputStream in = conn.getInputStream()) {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
                parser.setInput(in, "UTF-8");
                List<JSONObject> result = new ArrayList<>();
                String currentTag = "";
                String title = "", link = "", published = "";
                boolean inItem = false;
                int event;
                while ((event = parser.next()) != XmlPullParser.END_DOCUMENT && result.size() < MAX_ITEMS_PER_FEED) {
                    if (event == XmlPullParser.START_TAG) {
                        currentTag = parser.getName().toLowerCase(java.util.Locale.ROOT);
                        if ("item".equals(currentTag) || "entry".equals(currentTag)) {
                            inItem = true; title = ""; link = ""; published = "";
                        } else if (inItem && "link".equals(currentTag)) {
                            String href = parser.getAttributeValue(null, "href");
                            if (href != null) link = href;
                        }
                    } else if (event == XmlPullParser.TEXT && inItem) {
                        String value = parser.getText() == null ? "" : parser.getText().trim();
                        if ("title".equals(currentTag) && title.isEmpty()) title = value;
                        else if (("link".equals(currentTag) || "guid".equals(currentTag)) && link.isEmpty()) link = value;
                        else if (("pubdate".equals(currentTag) || "published".equals(currentTag)
                                || "updated".equals(currentTag)) && published.isEmpty()) published = value;
                    } else if (event == XmlPullParser.END_TAG) {
                        String end = parser.getName().toLowerCase(java.util.Locale.ROOT);
                        if (inItem && ("item".equals(end) || "entry".equals(end))) {
                            if (!title.isEmpty()) {
                                JSONObject obj = new JSONObject();
                                obj.put("title", title.substring(0, Math.min(240, title.length())));
                                obj.put("link", link.substring(0, Math.min(1000, link.length())));
                                obj.put("published", published.substring(0, Math.min(100, published.length())));
                                obj.put("saved_at", System.currentTimeMillis());
                                result.add(obj);
                            }
                            inItem = false;
                        }
                        currentTag = "";
                    }
                }
                return result;
            }
        } finally { conn.disconnect(); }
    }

    private static boolean isSafeFeedUrl(String value) {
        try {
            Uri u = Uri.parse(value);
            if (!"https".equalsIgnoreCase(u.getScheme()) || u.getHost() == null
                    || u.getUserInfo() != null || u.getPort() != -1 && u.getPort() != 443) return false;
            String host = u.getHost().toLowerCase(java.util.Locale.ROOT);
            if (host.equals("localhost") || host.endsWith(".local") || host.matches("[0-9.]+")
                    || host.contains(":")) return false;
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) return false;
            }
            return true;
        } catch (Exception e) { return false; }
    }
}
