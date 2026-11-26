package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Owner-invoked public reference lookups. Inputs are sent only to the selected public data provider. */
public final class PublicKnowledgeStudio {
    private PublicKnowledgeStudio() {}

    private interface ResultParser { String parse(String body) throws Exception; }

    public static void show(Activity activity) {
        String[] items = {
                "Country Information • name or ISO code",
                "India PIN Code • post offices and district",
                "English Dictionary • meaning and pronunciation",
                "Wikipedia Topic Lookup • sourced summary"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Public Knowledge Studio")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) countryInput(activity);
                    else if (which == 1) pinInput(activity);
                    else if (which == 2) dictionaryInput(activity);
                    else wikipediaInput(activity);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static void wikipediaInput(Activity activity) {
        showInput(activity, "Wikipedia Topic Lookup",
                "Enter a topic or article title",
                value -> request(activity,
                        "https://en.wikipedia.org/api/rest_v1/page/summary/" + pathEncode(value),
                        PublicKnowledgeStudio::parseWikipediaSummary));
    }

    private static String parseWikipediaSummary(String body) throws Exception {
        JSONObject root = new JSONObject(body);
        String title = root.optString("title", "Topic");
        String description = root.optString("description", "");
        String extract = root.optString("extract", "");
        if (extract.trim().isEmpty()) return "No public summary was returned for this topic.";
        if (extract.length() > 1200) extract = extract.substring(0, 1200) + "…";
        JSONObject urls = root.optJSONObject("content_urls");
        JSONObject desktop = urls == null ? null : urls.optJSONObject("desktop");
        String page = desktop == null ? "" : desktop.optString("page", "");
        String timestamp = root.optString("timestamp", "Not supplied");
        return "TOPIC\n" + title
                + (description.isEmpty() ? "" : "\n" + description)
                + "\n\nSUMMARY\n" + extract
                + "\n\nSOURCE\n" + (page.isEmpty() ? "Wikipedia article URL not supplied" : page)
                + "\nProvider timestamp: " + timestamp
                + "\n\nWikipedia is a general reference source. Verify important or current facts with official and primary sources.";
    }

    private static void countryInput(Activity activity) {
        showInput(activity, "Country Information",
                "Enter a country name or ISO code (for example, India or IN)",
                value -> {
                    String encoded = pathEncode(value);
                    boolean code = value.matches("(?i)[a-z]{2,3}");
                    String url = code
                            ? "https://countries.dev/alpha/" + encoded
                            : "https://countries.dev/name/" + encoded;
                    request(activity, url, body -> parseCountries(body, code));
                });
    }

    private static String parseCountries(String body, boolean single) throws Exception {
        JSONArray countries;
        if (single) {
            JSONObject one = new JSONObject(body);
            countries = new JSONArray().put(one);
        } else {
            countries = new JSONArray(body);
        }
        if (countries.length() == 0) return "No matching country was found. Try a more specific name or ISO code.";
        StringBuilder out = new StringBuilder();
        int limit = Math.min(countries.length(), 8);
        for (int i = 0; i < limit; i++) {
            JSONObject c = countries.optJSONObject(i);
            if (c == null) continue;
            if (i > 0) out.append("\n\n--------------------\n\n");
            out.append(c.optString("flag", "")).append(" ").append(c.optString("name", "Country"))
                    .append("\nISO: ").append(c.optString("alpha2Code", "—"))
                    .append(" / ").append(c.optString("alpha3Code", "—"))
                    .append("\nCapital: ").append(c.optString("capital", "Not listed"))
                    .append("\nRegion: ").append(c.optString("region", "Not listed"))
                    .append(" • ").append(c.optString("subregion", ""))
                    .append("\nPopulation: ").append(formatNumber(c.optLong("population", -1)))
                    .append("\nArea: ").append(formatNumber(c.optLong("area", -1))).append(" km²");
            JSONArray currencies = c.optJSONArray("currencies");
            if (currencies != null && currencies.length() > 0) {
                JSONObject cur = currencies.optJSONObject(0);
                if (cur != null) out.append("\nCurrency: ").append(cur.optString("name", ""))
                        .append(" (").append(cur.optString("code", "")).append(" ")
                        .append(cur.optString("symbol", "")).append(")");
            }
            JSONArray languages = c.optJSONArray("languages");
            if (languages != null && languages.length() > 0) {
                out.append("\nLanguages: ");
                for (int k = 0; k < Math.min(languages.length(), 5); k++) {
                    JSONObject lang = languages.optJSONObject(k);
                    if (lang != null) {
                        if (k > 0) out.append(", ");
                        out.append(lang.optString("name", ""));
                    }
                }
            }
        }
        if (countries.length() > limit) out.append("\n\nShowing first ").append(limit)
                .append(" matches out of ").append(countries.length()).append(".");
        out.append("\n\nSource: countries.dev • Public reference data; verify important details with official sources.");
        return out.toString();
    }

    private static void pinInput(Activity activity) {
        showInput(activity, "India PIN Code Lookup",
                "Enter the 6-digit Indian PIN code", value -> {
                    if (!value.matches("[0-9]{6}")) {
                        Toast.makeText(activity, "Enter exactly 6 digits.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    request(activity, "https://api.postalpincode.in/pincode/" + value,
                            PublicKnowledgeStudio::parsePin);
                });
    }

    private static String parsePin(String body) throws Exception {
        JSONArray root = new JSONArray(body);
        if (root.length() == 0) return "The postal data provider returned no result.";
        JSONObject result = root.optJSONObject(0);
        if (result == null || !"Success".equalsIgnoreCase(result.optString("Status"))) {
            return "No post-office result was found for this PIN code. Check the six digits and try again.";
        }
        JSONArray offices = result.optJSONArray("PostOffice");
        if (offices == null || offices.length() == 0) return "No post offices were listed for this PIN code.";
        StringBuilder out = new StringBuilder("PIN CODE RESULTS • ")
                .append(result.optString("Message", offices.length() + " post offices"))
                .append("\n\n");
        int limit = Math.min(offices.length(), 25);
        for (int i = 0; i < limit; i++) {
            JSONObject office = offices.optJSONObject(i);
            if (office == null) continue;
            out.append(i + 1).append(". ").append(office.optString("Name", "Post Office"))
                    .append("\n   Type: ").append(office.optString("BranchType", "Not listed"))
                    .append("\n   Delivery: ").append(office.optString("DeliveryStatus", "Not listed"))
                    .append("\n   District: ").append(office.optString("District", "Not listed"))
                    .append("\n   State: ").append(office.optString("State", "Not listed"))
                    .append("\n   Division: ").append(office.optString("Division", "Not listed"))
                    .append("\n\n");
        }
        if (offices.length() > limit) out.append("Showing first ").append(limit)
                .append(" of ").append(offices.length()).append(" post offices.\n");
        out.append("\nSource: PostalPincode.in API (community-maintained wrapper over postal data).")
                .append("\nThis lookup is informational, not address verification or a delivery guarantee.");
        return out.toString();
    }

    private static void dictionaryInput(Activity activity) {
        showInput(activity, "English Dictionary",
                "Enter one English word", value -> {
                    if (!value.matches("[A-Za-z][A-Za-z'-]{0,59}")) {
                        Toast.makeText(activity, "Enter one English word (letters, apostrophe or hyphen).", Toast.LENGTH_LONG).show();
                        return;
                    }
                    request(activity, "https://api.dictionaryapi.dev/api/v2/entries/en/" + pathEncode(value),
                            PublicKnowledgeStudio::parseDictionary);
                });
    }

    private static String parseDictionary(String body) throws Exception {
        JSONArray entries = new JSONArray(body);
        if (entries.length() == 0) return "No dictionary entry was found.";
        JSONObject entry = entries.optJSONObject(0);
        if (entry == null) return "The dictionary returned an unexpected response.";
        StringBuilder out = new StringBuilder("WORD • ").append(entry.optString("word", ""))
                .append("\nPronunciation: ").append(entry.optString("phonetic", "Not available"));
        JSONArray phonetics = entry.optJSONArray("phonetics");
        if (phonetics != null) {
            for (int i = 0; i < phonetics.length(); i++) {
                JSONObject p = phonetics.optJSONObject(i);
                if (p != null && !p.optString("text", "").isEmpty()) {
                    out.append("\nPhonetic: ").append(p.optString("text"));
                    break;
                }
            }
        }
        JSONArray meanings = entry.optJSONArray("meanings");
        if (meanings != null) {
            for (int i = 0; i < Math.min(meanings.length(), 4); i++) {
                JSONObject meaning = meanings.optJSONObject(i);
                if (meaning == null) continue;
                out.append("\n\n").append(meaning.optString("partOfSpeech", "Meaning").toUpperCase())
                        .append("\n");
                JSONArray definitions = meaning.optJSONArray("definitions");
                if (definitions != null) {
                    for (int k = 0; k < Math.min(definitions.length(), 2); k++) {
                        JSONObject def = definitions.optJSONObject(k);
                        if (def != null) {
                            out.append("• ").append(def.optString("definition", ""))
                                    .append("\n");
                            String example = def.optString("example", "");
                            if (!example.isEmpty()) out.append("  Example: ").append(example).append("\n");
                        }
                    }
                }
            }
        }
        out.append("\nSource: Free Dictionary API (dictionaryapi.dev). English entries only.");
        return out.toString();
    }

    private interface InputAction { void submit(String value); }

    private static void showInput(Activity activity, String title, String hint, InputAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setPadding(24, 16, 24, 16);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Search", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) Toast.makeText(activity, "Enter a value first.", Toast.LENGTH_SHORT).show();
                    else action.submit(value);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void request(Activity activity, String address, ResultParser parser) {
        new AlertDialog.Builder(activity).setTitle("Public data lookup")
                .setMessage("JARVIS will send only the value you entered to the selected public data provider. No contacts, saved notes or conversation history are attached.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (dialog, which) -> {
                    Toast.makeText(activity, "Looking up public information…", Toast.LENGTH_SHORT).show();
                    Thread worker = new Thread(() -> {
                        HttpURLConnection connection = null;
                        String result;
                        try {
                            connection = (HttpURLConnection) new URL(address).openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(10000);
                            connection.setReadTimeout(15000);
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setRequestProperty("User-Agent", "JARVIS-Android/1.0 (public reference lookup)");
                            int status = connection.getResponseCode();
                            InputStream stream = status >= 200 && status < 300
                                    ? connection.getInputStream() : connection.getErrorStream();
                            StringBuilder body = new StringBuilder();
                            if (stream != null) {
                                try (BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                                    String line;
                                    while ((line = reader.readLine()) != null && body.length() < 500000) {
                                        body.append(line);
                                    }
                                }
                            }
                            if (status < 200 || status >= 300) {
                                result = status == 404 ? "No matching public record was found."
                                        : "The data provider returned HTTP " + status + ". Please try again later.";
                            } else {
                                result = parser.parse(body.toString());
                            }
                        } catch (Exception e) {
                            result = "The public data service is unavailable or returned an unexpected response. Check the internet connection and try again.";
                        } finally {
                            if (connection != null) connection.disconnect();
                        }
                        final String output = result;
                        activity.runOnUiThread(() -> {
                            if (activity.isFinishing() || activity.isDestroyed()) return;
                            new AlertDialog.Builder(activity)
                                    .setTitle("JARVIS • Public Knowledge Result")
                                    .setMessage(output)
                                    .setPositiveButton("Done", null)
                                    .show();
                        });
                    }, "jarvis-public-knowledge");
                    worker.start();
                }).show();
    }

    private static String pathEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }

    private static String formatNumber(long value) {
        return value < 0 ? "Not listed" : String.format(java.util.Locale.US, "%,d", value);
    }
}
