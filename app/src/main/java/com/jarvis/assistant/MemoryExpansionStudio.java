package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * M09 owner-controlled memory extensions. All records are manually created or saved
 * by the owner. The archive is encrypted with a device-bound Android Keystore key.
 */
final class MemoryExpansionStudio {
    private static final String GRAPH_KEY = "memory_graph_v1";
    private static final String IDEAS_KEY = "idea_versions_v1";
    private static final String ARCHIVE_NAME = "jarvis_memory_legacy.enc";
    private static final String KEY_ALIAS = "jarvis_memory_archive_aes_v1";
    private static final int MAX_IDEA_VERSIONS = 20;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private MemoryExpansionStudio() { }

    static void show(Activity activity, SharedPreferences prefs, File filesDir, Runnable onChanged) {
        String[] actions = {
                "Knowledge Graph — manage nodes and links",
                "Idea Evolution — save and review versions",
                "Encrypted Digital Legacy Archive"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Memory Expansion")
                .setMessage("Owner-created memory only. No background capture, cloud upload or contact/message scanning.")
                .setItems(actions, (d, which) -> {
                    if (which == 0) showGraph(activity, prefs, onChanged);
                    else if (which == 1) showIdeas(activity, prefs, onChanged);
                    else showArchive(activity, prefs, filesDir, onChanged);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // Feature 1: a small owner-curated graph of named concepts and their relationships.
    private static JSONArray readGraph(SharedPreferences prefs) {
        try { return new JSONArray(prefs.getString(GRAPH_KEY, "[]")); }
        catch (JSONException e) { return null; }
    }

    private static void showGraph(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        JSONArray graph = readGraph(prefs);
        if (graph == null) {
            message(activity, "Knowledge Graph", "Graph data is unreadable. Existing data was not changed.");
            return;
        }
        String[] actions = {"Add knowledge node", "Connect two nodes", "View graph", "Delete graph item"};
        new AlertDialog.Builder(activity).setTitle("JARVIS • Knowledge Graph")
                .setMessage(graph.length() + " owner-created nodes/links. This is a manual knowledge map, not automatic profiling.")
                .setItems(actions, (d, which) -> {
                    if (which == 0) addNode(activity, prefs, onChanged);
                    else if (which == 1) addEdge(activity, prefs, onChanged);
                    else if (which == 2) viewGraph(activity, prefs, onChanged);
                    else deleteGraphItem(activity, prefs, onChanged);
                }).setNegativeButton("Back", (d, w) -> show(activity, prefs, activity.getFilesDir(), onChanged))
                .show();
    }

    private static void addNode(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        LinearLayout form = form(activity);
        EditText name = field(activity, "Concept / person / project name", false);
        EditText detail = field(activity, "Short note (optional)", true);
        form.addView(name); form.addView(detail);
        new AlertDialog.Builder(activity).setTitle("Add knowledge node").setView(form)
                .setPositiveButton("Save", (d, w) -> {
                    String value = name.getText().toString().trim();
                    JSONArray graph = readGraph(prefs);
                    if (value.isEmpty() || graph == null) {
                        toast(activity, graph == null ? "Graph unavailable; nothing changed." : "A name is required.");
                        return;
                    }
                    JSONObject item = new JSONObject();
                    try {
                        item.put("kind", "node"); item.put("name", value);
                        item.put("detail", detail.getText().toString().trim());
                        item.put("createdAt", System.currentTimeMillis());
                        graph.put(item);
                        prefs.edit().putString(GRAPH_KEY, graph.toString()).apply();
                        toast(activity, "Knowledge node saved on this device.");
                        if (onChanged != null) onChanged.run();
                    } catch (JSONException e) { toast(activity, "Could not save node."); }
                }).setNegativeButton("Cancel", null).show();
    }

    private static void addEdge(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        JSONArray graph = readGraph(prefs);
        if (graph == null) { message(activity, "Knowledge Graph", "Graph data is unreadable."); return; }
        if (countNodes(graph) < 2) { message(activity, "Connect nodes", "Add at least two nodes first."); return; }
        LinearLayout form = form(activity);
        EditText from = field(activity, "First node name", false);
        EditText relation = field(activity, "Relationship (e.g. belongs to)", false);
        EditText to = field(activity, "Second node name", false);
        form.addView(from); form.addView(relation); form.addView(to);
        new AlertDialog.Builder(activity).setTitle("Connect knowledge nodes").setView(form)
                .setMessage("Enter the exact names of two existing nodes.")
                .setPositiveButton("Save link", (d, w) -> {
                    String a = from.getText().toString().trim();
                    String rel = relation.getText().toString().trim();
                    String b = to.getText().toString().trim();
                    JSONArray latest = readGraph(prefs);
                    if (latest == null || a.isEmpty() || rel.isEmpty() || b.isEmpty()
                            || a.equalsIgnoreCase(b) || !hasNode(latest, a) || !hasNode(latest, b)) {
                        toast(activity, "Check both node names and relationship. Nothing changed.");
                        return;
                    }
                    JSONObject edge = new JSONObject();
                    try {
                        edge.put("kind", "link"); edge.put("from", a); edge.put("relation", rel);
                        edge.put("to", b); edge.put("createdAt", System.currentTimeMillis());
                        latest.put(edge);
                        prefs.edit().putString(GRAPH_KEY, latest.toString()).apply();
                        toast(activity, "Relationship saved.");
                        if (onChanged != null) onChanged.run();
                    } catch (JSONException e) { toast(activity, "Could not save relationship."); }
                }).setNegativeButton("Cancel", null).show();
    }

    private static void viewGraph(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        JSONArray graph = readGraph(prefs);
        if (graph == null) { message(activity, "Knowledge Graph", "Graph data is unreadable."); return; }
        String[] rows = new String[graph.length()];
        for (int i = 0; i < graph.length(); i++) {
            JSONObject item = graph.optJSONObject(i);
            if (item == null) rows[i] = "Unavailable item";
            else if ("link".equals(item.optString("kind")))
                rows[i] = item.optString("from") + " — " + item.optString("relation") + " → " + item.optString("to");
            else rows[i] = "● " + item.optString("name") + (item.optString("detail").isEmpty() ? "" : " — " + item.optString("detail"));
        }
        new AlertDialog.Builder(activity).setTitle("Knowledge Graph • " + rows.length + " items")
                .setItems(rows, (d, which) -> {
                    JSONObject item = graph.optJSONObject(which);
                    if (item != null) message(activity, "Graph item",
                            "Created: " + item.optLong("createdAt") + "\n\n"
                                    + ("link".equals(item.optString("kind"))
                                    ? item.optString("from") + " — " + item.optString("relation") + " → " + item.optString("to")
                                    : item.optString("name") + "\n" + item.optString("detail")));
                }).setNegativeButton("Back", (d, w) -> showGraph(activity, prefs, onChanged)).show();
    }

    private static void deleteGraphItem(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        JSONArray graph = readGraph(prefs);
        if (graph == null || graph.length() == 0) { message(activity, "Delete graph item", "No graph items to delete."); return; }
        String[] rows = new String[graph.length()];
        for (int i = 0; i < graph.length(); i++) {
            JSONObject item = graph.optJSONObject(i);
            rows[i] = item == null ? "Unavailable item" : ("link".equals(item.optString("kind"))
                    ? item.optString("from") + " → " + item.optString("to")
                    : item.optString("name"));
        }
        new AlertDialog.Builder(activity).setTitle("Select item to delete").setItems(rows, (d, which) ->
                new AlertDialog.Builder(activity).setTitle("Delete selected graph item?")
                        .setMessage("Only this manually created item will be removed.")
                        .setNegativeButton("Cancel", null).setPositiveButton("Delete", (x, y) -> {
                            JSONArray latest = readGraph(prefs);
                            if (latest != null && which < latest.length()) {
                                JSONObject removed = latest.optJSONObject(which);
                                String removedNode = removed != null && "node".equals(removed.optString("kind"))
                                        ? removed.optString("name") : null;
                                latest.remove(which);
                                if (removedNode != null) {
                                    for (int i = latest.length() - 1; i >= 0; i--) {
                                        JSONObject edge = latest.optJSONObject(i);
                                        if (edge != null && "link".equals(edge.optString("kind"))
                                                && (removedNode.equals(edge.optString("from"))
                                                || removedNode.equals(edge.optString("to")))) {
                                            latest.remove(i);
                                        }
                                    }
                                }
                                prefs.edit().putString(GRAPH_KEY, latest.toString()).apply();
                                if (onChanged != null) onChanged.run();
                                showGraph(activity, prefs, onChanged);
                            }
                        }).show()).setNegativeButton("Cancel", null).show();
    }

    private static int countNodes(JSONArray graph) {
        int count = 0;
        for (int i = 0; i < graph.length(); i++) {
            JSONObject item = graph.optJSONObject(i);
            if (item != null && "node".equals(item.optString("kind"))) count++;
        }
        return count;
    }

    private static boolean hasNode(JSONArray graph, String name) {
        for (int i = 0; i < graph.length(); i++) {
            JSONObject item = graph.optJSONObject(i);
            if (item != null && "node".equals(item.optString("kind"))
                    && name.equalsIgnoreCase(item.optString("name"))) return true;
        }
        return false;
    }

    // Feature 2: explicit snapshots of the existing notes text, with a bounded history.
    private static JSONArray readIdeas(SharedPreferences prefs) {
        try { return new JSONArray(prefs.getString(IDEAS_KEY, "[]")); }
        catch (JSONException e) { return null; }
    }

    private static void showIdeas(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        JSONArray versions = readIdeas(prefs);
        if (versions == null) { message(activity, "Idea Evolution", "Version history is unreadable; no data changed."); return; }
        String[] actions = {"Save current notes as a version", "Review saved versions"};
        new AlertDialog.Builder(activity).setTitle("JARVIS • Idea Evolution")
                .setMessage(versions.length() + " saved versions • maximum " + MAX_IDEA_VERSIONS
                        + ". Versions are created only when you choose Save.")
                .setItems(actions, (d, which) -> {
                    if (which == 0) saveIdeaVersion(activity, prefs, onChanged);
                    else reviewIdeaVersions(activity, prefs, onChanged);
                }).setNegativeButton("Back", (d, w) -> show(activity, prefs, activity.getFilesDir(), onChanged)).show();
    }

    private static void saveIdeaVersion(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        String notes = prefs.getString("notes", prefs.getString("jarvis_notes", ""));
        if (notes == null || notes.trim().isEmpty()) {
            message(activity, "Save idea version", "There are no saved notes to snapshot. Add or edit notes first.");
            return;
        }
        JSONArray versions = readIdeas(prefs);
        if (versions == null) { message(activity, "Save idea version", "History is unreadable; nothing changed."); return; }
        JSONObject version = new JSONObject();
        try {
            version.put("createdAt", System.currentTimeMillis());
            version.put("content", notes);
            versions.put(version);
            while (versions.length() > MAX_IDEA_VERSIONS) versions.remove(0);
            prefs.edit().putString(IDEAS_KEY, versions.toString()).apply();
            toast(activity, "Idea version saved locally.");
            if (onChanged != null) onChanged.run();
        } catch (JSONException e) { toast(activity, "Could not save version."); }
    }

    private static void reviewIdeaVersions(Activity activity, SharedPreferences prefs, Runnable onChanged) {
        JSONArray versions = readIdeas(prefs);
        if (versions == null) { message(activity, "Idea Evolution", "History is unreadable."); return; }
        String[] rows = new String[versions.length()];
        for (int i = 0; i < versions.length(); i++) {
            JSONObject item = versions.optJSONObject(i);
            long time = item == null ? 0L : item.optLong("createdAt", 0L);
            rows[i] = (i + 1) + ". " + (time > 0
                    ? new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date(time))
                    : "Date unavailable");
        }
        new AlertDialog.Builder(activity).setTitle("Saved idea versions").setItems(rows, (d, which) -> {
            JSONObject item = versions.optJSONObject(which);
            if (item == null) return;
            String content = item.optString("content", "");
            new AlertDialog.Builder(activity).setTitle("Idea version " + (which + 1))
                    .setMessage(content.length() > 2500 ? content.substring(0, 2500) + "\n\n[Preview shortened]" : content)
                    .setPositiveButton("Restore to notes", (x, y) ->
                            new AlertDialog.Builder(activity).setTitle("Replace current saved notes?")
                                    .setMessage("This replaces only the current notes text. A new version is not created automatically.")
                                    .setNegativeButton("Cancel", null).setPositiveButton("Restore", (a, b) -> {
                                        prefs.edit().putString("notes", content).putString("jarvis_notes", content).apply();
                                        toast(activity, "Selected version restored to notes.");
                                        if (onChanged != null) onChanged.run();
                                    }).show())
                    .setNegativeButton("Close", null).show();
        }).setNegativeButton("Back", (d, w) -> showIdeas(activity, prefs, onChanged)).show();
    }

    // Feature 3: AES-GCM encrypted, device-bound archive in private app storage.
    private static void showArchive(Activity activity, SharedPreferences prefs, File filesDir, Runnable onChanged) {
        File archive = new File(filesDir, ARCHIVE_NAME);
        String info = archive.exists() ? "An encrypted archive exists on this device."
                : "No encrypted archive has been created yet.";
        String[] actions = {"Create / replace encrypted archive", "Verify and preview archive", "Delete encrypted archive"};
        new AlertDialog.Builder(activity).setTitle("JARVIS • Digital Legacy Archive")
                .setMessage(info + "\n\nAES-256-GCM where supported by Android Keystore. The key is device-bound; this archive is not a portable password backup. Recent conversation text is excluded.")
                .setItems(actions, (d, which) -> {
                    if (which == 0) createArchive(activity, prefs, archive);
                    else if (which == 1) verifyArchive(activity, archive);
                    else deleteArchive(activity, archive, onChanged);
                }).setNegativeButton("Back", (d, w) -> show(activity, prefs, filesDir, onChanged)).show();
    }

    private static void createArchive(Activity activity, SharedPreferences prefs, File archive) {
        try {
            JSONObject snapshot = new JSONObject();
            snapshot.put("createdAt", System.currentTimeMillis());
            snapshot.put("notes", prefs.getString("notes", prefs.getString("jarvis_notes", "")));
            snapshot.put("timeline", prefs.getString("memory_timeline_v1", "[]"));
            snapshot.put("knowledgeGraph", prefs.getString(GRAPH_KEY, "[]"));
            snapshot.put("ideaVersions", prefs.getString(IDEAS_KEY, "[]"));
            byte[] encrypted = encrypt(snapshot.toString().getBytes(UTF8));
            File temp = new File(archive.getParentFile(), ARCHIVE_NAME + ".tmp");
            FileOutputStream out = new FileOutputStream(temp);
            try { out.write(encrypted); out.flush(); out.getFD().sync(); } finally { out.close(); }
            File backup = new File(archive.getParentFile(), ARCHIVE_NAME + ".bak");
            if (backup.exists() && !backup.delete()) throw new Exception("Could not clear temporary backup.");
            boolean hadArchive = archive.exists();
            if (hadArchive && !archive.renameTo(backup)) throw new Exception("Could not stage previous archive safely.");
            if (!temp.renameTo(archive)) {
                if (hadArchive) backup.renameTo(archive);
                throw new Exception("Could not finalize archive file.");
            }
            if (backup.exists()) backup.delete();
            toast(activity, "Encrypted archive created on this device.");
        } catch (Exception e) {
            toast(activity, "Archive creation failed. Existing memory was not changed.");
        }
    }

    private static void verifyArchive(Activity activity, File archive) {
        if (!archive.exists()) { message(activity, "Archive verification", "No archive found."); return; }
        try {
            byte[] encrypted = readFile(archive);
            JSONObject snapshot = new JSONObject(new String(decrypt(encrypted), UTF8));
            message(activity, "Archive verified",
                    "Encryption and authentication check passed.\n\nCreated: " + snapshot.optLong("createdAt")
                            + "\nTimeline characters: " + snapshot.optString("timeline", "[]").length()
                            + "\nKnowledge graph characters: " + snapshot.optString("knowledgeGraph", "[]").length()
                            + "\nIdea versions characters: " + snapshot.optString("ideaVersions", "[]").length()
                            + "\n\nNo data was restored or exported.");
        } catch (Exception e) {
            message(activity, "Archive verification failed",
                    "The archive could not be authenticated/decrypted with this device key. No stored memory was changed.");
        }
    }

    private static void deleteArchive(Activity activity, File archive, Runnable onChanged) {
        if (!archive.exists()) { message(activity, "Delete archive", "No archive exists."); return; }
        new AlertDialog.Builder(activity).setTitle("Delete encrypted archive?")
                .setMessage("This removes only the encrypted archive file. Notes, timeline, graph and idea versions remain unchanged.")
                .setNegativeButton("Cancel", null).setPositiveButton("Delete", (d, w) -> {
                    if (archive.delete()) toast(activity, "Encrypted archive deleted.");
                    else toast(activity, "Could not delete archive.");
                    if (onChanged != null) onChanged.run();
                }).show();
    }

    private static SecretKey getOrCreateKey() throws Exception {
        if (Build.VERSION.SDK_INT < 23) throw new Exception("Android Keystore AES-GCM requires API 23 or newer.");
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        java.security.Key existing = store.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setKeySize(256)
                .build());
        return generator.generateKey();
    }

    private static byte[] encrypt(byte[] plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] iv = cipher.getIV();
        byte[] body = cipher.doFinal(plain);
        byte[] result = new byte[1 + iv.length + body.length];
        result[0] = (byte) iv.length;
        System.arraycopy(iv, 0, result, 1, iv.length);
        System.arraycopy(body, 0, result, 1 + iv.length, body.length);
        return result;
    }

    private static byte[] decrypt(byte[] input) throws Exception {
        if (input == null || input.length < 30) throw new Exception("Invalid archive length.");
        int ivLength = input[0] & 0xff;
        if (ivLength < 12 || ivLength > 16 || input.length <= 1 + ivLength + 16)
            throw new Exception("Invalid archive header.");
        byte[] iv = new byte[ivLength];
        byte[] body = new byte[input.length - 1 - ivLength];
        System.arraycopy(input, 1, iv, 0, ivLength);
        System.arraycopy(input, 1 + ivLength, body, 0, body.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        return cipher.doFinal(body);
    }

    private static byte[] readFile(File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) != -1) {
                if (out.size() + count > 5 * 1024 * 1024) throw new Exception("Archive too large.");
                out.write(buffer, 0, count);
            }
        } finally { in.close(); }
        return out.toByteArray();
    }

    private static LinearLayout form(Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 8, 24, 8);
        return layout;
    }

    private static EditText field(Activity activity, String hint, boolean multiline) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setSingleLine(!multiline);
        if (multiline) {
            input.setMinLines(2);
            input.setGravity(Gravity.TOP | Gravity.START);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }
        return input;
    }

    private static void message(Activity activity, String title, String text) {
        new AlertDialog.Builder(activity).setTitle(title).setMessage(text).setPositiveButton("Close", null).show();
    }

    private static void toast(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
