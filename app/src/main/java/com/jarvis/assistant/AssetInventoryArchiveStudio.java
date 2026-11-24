package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Encrypted export and restore for the existing owner-managed local asset inventory only. */
public final class AssetInventoryArchiveStudio {
    private AssetInventoryArchiveStudio() { }

    private static final String PREFS = "jarvis_preferences";
    private static final String INVENTORY_KEY = "owner_asset_inventory_v1";
    private static final String FORMAT = "JARVIS_ASSET_INVENTORY_BACKUP";
    private static final int VERSION = 1;
    private static final int MAX_ASSETS = 100;
    private static final int EXPORT_REQUEST = 7318;
    private static final int IMPORT_REQUEST = 7319;
    private static char[] pendingExportPassword;

    public static void show(Activity activity) {
        String[] actions = {"Create encrypted backup", "Restore encrypted backup"};
        new AlertDialog.Builder(activity).setTitle("JARVIS • Encrypted Asset Backup")
                .setMessage("Backs up the existing Smart Asset Inventory only. The encrypted archive includes item names, categories, purchase-date labels, condition, warranty dates and notes. Receipt/manual files, document links, Android permission grants, app credentials and unrelated JARVIS data are excluded. The passphrase cannot be recovered if lost.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) beginExport(activity);
                    else beginImport(activity);
                }).setNegativeButton("Close", null).show();
    }

    private static SharedPreferences preferences(Activity activity) {
        return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    }

    private static JSONArray readInventory(Activity activity) {
        try {
            String raw = preferences(activity).getString(INVENTORY_KEY, "[]");
            JSONArray records = new JSONArray(raw == null ? "[]" : raw);
            if (records.length() > MAX_ASSETS) {
                Toast.makeText(activity, "Inventory exceeds the supported backup limit; nothing changed.",
                        Toast.LENGTH_LONG).show();
                return null;
            }
            return records;
        } catch (Exception e) {
            Toast.makeText(activity, "Inventory data is unreadable; no backup or restore was performed.",
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static void beginExport(Activity activity) {
        EditText password = passwordField(activity, "Passphrase (12–128 characters)");
        EditText confirm = passwordField(activity, "Confirm passphrase");
        LinearLayout fields = new LinearLayout(activity);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(24, 8, 24, 8);
        fields.addView(password);
        fields.addView(confirm);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(fields);

        new AlertDialog.Builder(activity).setTitle("Protect the backup")
                .setMessage("Choose a strong passphrase of at least 12 characters. The archive uses AES-256-GCM with PBKDF2-HMAC-SHA256. JARVIS does not save the passphrase.")
                .setView(scroll).setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (dialog, which) -> {
                    char[] first = readPassword(password);
                    char[] second = readPassword(confirm);
                    if (first.length < AssetInventoryArchive.MIN_PASSWORD_CHARS
                            || first.length > AssetInventoryArchive.MAX_PASSWORD_CHARS
                            || !Arrays.equals(first, second)) {
                        wipe(first);
                        wipe(second);
                        Toast.makeText(activity, "Passphrases must match and be 12–128 characters.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    wipe(second);
                    clearPendingPassword();
                    pendingExportPassword = first;
                    Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    create.addCategory(Intent.CATEGORY_OPENABLE);
                    create.setType("application/octet-stream");
                    create.putExtra(Intent.EXTRA_TITLE, "jarvis-asset-backup.jva");
                    create.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    try {
                        activity.startActivityForResult(create, EXPORT_REQUEST);
                    } catch (Exception e) {
                        clearPendingPassword();
                        Toast.makeText(activity, "No compatible file provider is available.",
                                Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void beginImport(Activity activity) {
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("*/*");
        pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            activity.startActivityForResult(pick, IMPORT_REQUEST);
        } catch (Exception e) {
            Toast.makeText(activity, "No compatible file provider is available.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /** MainActivity delegates its existing document-picker callback here first. */
    public static boolean handlePickerResult(Activity activity, int requestCode,
                                             int resultCode, Intent data) {
        if (requestCode == EXPORT_REQUEST) {
            char[] password = takePendingPassword();
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                wipe(password);
                Toast.makeText(activity, "Backup cancelled; local inventory unchanged.",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            writeEncryptedBackup(activity, data.getData(), password);
            return true;
        }
        if (requestCode == IMPORT_REQUEST) {
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                Toast.makeText(activity, "Restore cancelled; local inventory unchanged.",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            byte[] archive = readBoundedArchive(activity, data.getData());
            if (archive != null) promptForRestorePassword(activity, archive);
            return true;
        }
        return false;
    }

    private static void writeEncryptedBackup(Activity activity, Uri destination, char[] password) {
        byte[] plaintext = null;
        byte[] encrypted = null;
        try {
            JSONArray records = readInventory(activity);
            if (records == null) return;
            JSONObject root = new JSONObject();
            root.put("format", FORMAT);
            root.put("version", VERSION);
            root.put("created_at", System.currentTimeMillis());
            root.put("records", copyAllowlistedRecords(records));
            plaintext = root.toString().getBytes(StandardCharsets.UTF_8);
            encrypted = AssetInventoryArchive.encrypt(plaintext, password);
            try (OutputStream output = activity.getContentResolver().openOutputStream(destination, "w")) {
                if (output == null) throw new IllegalStateException("Destination could not be opened.");
                output.write(encrypted);
                output.flush();
            }
            Toast.makeText(activity, "Encrypted asset backup saved. Keep the passphrase safe.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Backup failed. Local inventory data was not changed.",
                    Toast.LENGTH_LONG).show();
        } finally {
            wipe(password);
            wipe(plaintext);
            wipe(encrypted);
        }
    }

    private static JSONArray copyAllowlistedRecords(JSONArray records) throws Exception {
        if (records.length() > MAX_ASSETS) throw new IllegalArgumentException("Too many records.");
        JSONArray safe = new JSONArray();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.optJSONObject(i);
            if (item == null) throw new IllegalArgumentException("Invalid record " + (i + 1) + ".");
            String name = item.optString("name", "");
            String category = item.optString("category", "");
            String purchaseDate = item.optString("purchase_date", "");
            String condition = item.optString("condition", "");
            String warranty = item.optString("warranty_until", "");
            String notes = item.optString("notes", "");
            String error = PracticalIntelligenceRules.validateAssetEntry(
                    name, category, purchaseDate, condition, notes, warranty);
            if (error != null) throw new IllegalArgumentException("Record " + (i + 1) + ": " + error);
            JSONObject copy = new JSONObject();
            copy.put("name", name);
            copy.put("category", category);
            copy.put("purchase_date", purchaseDate);
            copy.put("condition", condition);
            copy.put("warranty_until", warranty);
            copy.put("notes", notes);
            copy.put("created_at", Math.max(1L, item.optLong("created_at", System.currentTimeMillis())));
            safe.put(copy);
        }
        return safe;
    }

    private static byte[] readBoundedArchive(Activity activity, Uri source) {
        try (InputStream input = activity.getContentResolver().openInputStream(source);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalStateException("Source could not be opened.");
            byte[] buffer = new byte[4096];
            int total = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (total > AssetInventoryArchive.MAX_ARCHIVE_BYTES) {
                    throw new IllegalArgumentException("Archive is too large.");
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } catch (Exception e) {
            Toast.makeText(activity, "Could not read the selected archive or it exceeded the size limit.",
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static void promptForRestorePassword(Activity activity, byte[] archive) {
        EditText password = passwordField(activity, "Backup passphrase");
        new AlertDialog.Builder(activity).setTitle("Unlock encrypted backup")
                .setMessage("Nothing is imported until the passphrase, authentication tag and data format are verified.")
                .setView(password).setNegativeButton("Cancel", (dialog, which) -> wipe(archive))
                .setPositiveButton("Unlock and preview", (dialog, which) -> {
                    char[] entered = readPassword(password);
                    byte[] plaintext = null;
                    try {
                        plaintext = AssetInventoryArchive.decrypt(archive, entered);
                        JSONArray imported = parsePayload(plaintext);
                        showRestorePreview(activity, imported);
                    } catch (Exception e) {
                        Toast.makeText(activity, "Backup could not be unlocked or failed integrity/format checks. Nothing changed.",
                                Toast.LENGTH_LONG).show();
                    } finally {
                        wipe(entered);
                        wipe(plaintext);
                        wipe(archive);
                    }
                }).show();
    }

    private static JSONArray parsePayload(byte[] plaintext) throws Exception {
        if (plaintext == null || plaintext.length > AssetInventoryArchive.MAX_PLAINTEXT_BYTES) {
            throw new IllegalArgumentException("Invalid archive payload.");
        }
        JSONObject root = new JSONObject(new String(plaintext, StandardCharsets.UTF_8));
        if (!FORMAT.equals(root.optString("format"))
                || root.optInt("version", -1) != VERSION
                || root.optLong("created_at", 0L) <= 0L) {
            throw new IllegalArgumentException("Unsupported archive metadata.");
        }
        JSONArray source = root.optJSONArray("records");
        if (source == null || source.length() > MAX_ASSETS) {
            throw new IllegalArgumentException("Invalid inventory record list.");
        }
        JSONArray safe = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            JSONObject item = source.optJSONObject(i);
            if (item == null) throw new IllegalArgumentException("Invalid record " + (i + 1) + ".");
            String name = requireString(item, "name");
            String category = requireString(item, "category");
            String purchaseDate = optionalString(item, "purchase_date");
            String condition = requireString(item, "condition");
            String warranty = optionalString(item, "warranty_until");
            String notes = optionalString(item, "notes");
            long createdAt = item.optLong("created_at", 0L);
            if (createdAt <= 0L) throw new IllegalArgumentException("Invalid record timestamp.");
            String error = PracticalIntelligenceRules.validateAssetEntry(
                    name, category, purchaseDate, condition, notes, warranty);
            if (error != null) throw new IllegalArgumentException("Record " + (i + 1) + ": " + error);
            JSONObject clean = new JSONObject();
            clean.put("name", name);
            clean.put("category", category);
            clean.put("purchase_date", purchaseDate);
            clean.put("condition", condition);
            clean.put("warranty_until", warranty);
            clean.put("notes", notes);
            clean.put("created_at", createdAt);
            safe.put(clean);
        }
        return safe;
    }

    private static String requireString(JSONObject item, String key) throws JSONException {
        Object value = item.opt(key);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new JSONException("Missing string field: " + key);
        }
        return ((String) value).trim();
    }

    private static String optionalString(JSONObject item, String key) throws JSONException {
        Object value = item.opt(key);
        if (value == null || value == JSONObject.NULL) return "";
        if (!(value instanceof String)) throw new JSONException("Invalid string field: " + key);
        return ((String) value).trim();
    }

    private static void showRestorePreview(Activity activity, JSONArray imported) {
        JSONArray existing = readInventory(activity);
        if (existing == null) return;
        int duplicates = countMergeDuplicates(existing, imported);
        int newItems = imported.length() - duplicates;
        String message = "Encrypted archive authenticated and validated.\n\n"
                + "Items in backup: " + imported.length() + "\n"
                + "Current local items: " + existing.length() + "\n"
                + "New items available to merge: " + newItems + "\n"
                + "Same-name/category items that merge will skip: " + duplicates + "\n\n"
                + "Merge keeps current records and adds only items without a matching name/category. "
                + "Replace overwrites the current inventory. Receipt/manual file links are not in the archive; "
                + "replaced links are detached but original files are not deleted. Existing reminders remain unchanged.";
        new AlertDialog.Builder(activity).setTitle("Review restore")
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Merge new items", (dialog, which) -> restoreInventory(activity, imported, false))
                .setPositiveButton("Replace all", (dialog, which) -> confirmReplace(activity, imported))
                .show();
    }

    private static int countMergeDuplicates(JSONArray existing, JSONArray imported) {
        int duplicates = 0;
        java.util.ArrayList<JSONObject> seen = new java.util.ArrayList<>();
        for (int i = 0; i < existing.length(); i++) {
            JSONObject item = existing.optJSONObject(i);
            if (item != null) seen.add(item);
        }
        for (int i = 0; i < imported.length(); i++) {
            JSONObject item = imported.optJSONObject(i);
            if (item == null) continue;
            boolean match = false;
            for (JSONObject previous : seen) {
                if (AssetInventoryBatchRules.isSameAsset(previous.optString("name"),
                        previous.optString("category"), item.optString("name"),
                        item.optString("category"))) {
                    match = true;
                    break;
                }
            }
            if (match) duplicates++;
            else seen.add(item);
        }
        return duplicates;
    }

    private static void confirmReplace(Activity activity, JSONArray imported) {
        new AlertDialog.Builder(activity).setTitle("Replace local inventory?")
                .setMessage("This replaces every current inventory record with the verified backup. Current receipt/manual links are removed from JARVIS, but original files are not deleted. Existing reminders are not modified. This cannot be undone without another backup.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Replace inventory", (dialog, which) ->
                        restoreInventory(activity, imported, true))
                .show();
    }

    private static void restoreInventory(Activity activity, JSONArray imported, boolean replace) {
        JSONArray current = readInventory(activity);
        if (current == null) return;
        JSONArray updated;
        try {
            if (replace) {
                updated = imported;
            } else {
                updated = new JSONArray(current.toString());
                for (int i = 0; i < imported.length(); i++) {
                    JSONObject candidate = imported.optJSONObject(i);
                    if (candidate == null) {
                        Toast.makeText(activity, "Invalid archive record; no changes were made.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    boolean duplicate = false;
                    for (int j = 0; j < updated.length(); j++) {
                        JSONObject existing = updated.optJSONObject(j);
                        if (existing != null && AssetInventoryBatchRules.isSameAsset(
                                existing.optString("name"), existing.optString("category"),
                                candidate.optString("name"), candidate.optString("category"))) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) updated.put(candidate);
                    if (updated.length() > MAX_ASSETS) {
                        Toast.makeText(activity, "Merge would exceed the 100-item limit. Nothing was changed.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Restore preparation failed; existing inventory was not changed.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        boolean saved = preferences(activity).edit().putString(INVENTORY_KEY, updated.toString()).commit();
        if (!saved) {
            Toast.makeText(activity, "Restore could not be saved. Existing inventory was left unchanged.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (replace) releaseOldDocumentGrants(activity, current);
        Toast.makeText(activity, replace
                ? "Inventory restored from encrypted backup."
                : "Backup merged. Existing matching items were left unchanged.",
                Toast.LENGTH_LONG).show();
    }

    private static void releaseOldDocumentGrants(Activity activity, JSONArray previous) {
        for (int i = 0; i < previous.length(); i++) {
            JSONObject item = previous.optJSONObject(i);
            if (item == null) continue;
            String saved = item.optString("document_uri", "");
            if (!AssetInventoryBatchRules.isValidAssetDocumentUri(saved)) continue;
            try {
                activity.getContentResolver().releasePersistableUriPermission(
                        Uri.parse(saved), Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
                // The inventory was already replaced; a missing provider grant is harmless.
            }
        }
    }

    private static EditText passwordField(Activity activity, String hint) {
        EditText field = new EditText(activity);
        field.setHint(hint);
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        field.setGravity(Gravity.CENTER_VERTICAL);
        field.setPadding(20, 16, 20, 16);
        return field;
    }

    private static char[] readPassword(EditText field) {
        char[] value = new char[field.getText().length()];
        field.getText().getChars(0, field.getText().length(), value, 0);
        return value;
    }

    private static char[] takePendingPassword() {
        char[] result = pendingExportPassword;
        pendingExportPassword = null;
        return result;
    }

    private static void clearPendingPassword() {
        wipe(pendingExportPassword);
        pendingExportPassword = null;
    }

    private static void wipe(byte[] value) {
        if (value != null) Arrays.fill(value, (byte) 0);
    }

    private static void wipe(char[] value) {
        if (value != null) Arrays.fill(value, (char) 0);
    }
}
