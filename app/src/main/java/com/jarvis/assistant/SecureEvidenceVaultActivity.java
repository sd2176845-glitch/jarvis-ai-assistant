package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import javax.crypto.CipherInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

/**
 * M13 first implementation slice: owner-selected documents are encrypted locally
 * with an Android Keystore AES-GCM key. No background collection or network upload.
 * This is not a tamper-proof chain-of-custody system or a substitute for legal evidence handling.
 */
public final class SecureEvidenceVaultActivity extends Activity {
    private static final int PICK_EVIDENCE = 8301;
    private static final int MAX_BYTES = 15 * 1024 * 1024;
    private static final String KEY_ALIAS = "jarvis_m13_evidence_aes_v1";
    private static final String PREFS = "jarvis_secure_evidence_v1";
    private static final String INDEX = "items";
    private TextView status;
    private LinearLayout list;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 24);
        root.setBackgroundColor(0xFF080509);
        TextView title = new TextView(this);
        title.setText("JARVIS • SECURE EVIDENCE VAULT");
        title.setTextColor(0xFFFF4652);
        title.setTextSize(20);
        root.addView(title);
        status = new TextView(this);
        status.setText("Files stay in app-private storage and are encrypted before being written.");
        status.setTextColor(0xFFE7C9CE);
        status.setPadding(0, 14, 0, 14);
        root.addView(status);
        Button add = new Button(this);
        add.setText("ADD OWNER-SELECTED FILE");
        add.setOnClickListener(v -> chooseFile());
        root.addView(add);
        Button refresh = new Button(this);
        refresh.setText("REFRESH VAULT");
        refresh.setOnClickListener(v -> renderItems());
        root.addView(refresh);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);
        setContentView(root);
        renderItems();
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, PICK_EVIDENCE);
        } catch (Exception e) {
            status.setText("Android file picker is unavailable.");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_EVIDENCE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) { }
        new AlertDialog.Builder(this)
                .setTitle("Encrypt selected file?")
                .setMessage("JARVIS will read only the file you selected, encrypt it with an Android Keystore key, and save it inside app-private storage. A 15 MB limit applies. The original file is not changed.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Encrypt and save", (d, w) -> encryptSelected(uri))
                .show();
    }

    private void encryptSelected(Uri uri) {
        String id = UUID.randomUUID().toString();
        File folder = new File(getFilesDir(), "secure_evidence_v1");
        if (!folder.exists() && !folder.mkdirs()) {
            status.setText("Could not create the private vault folder.");
            return;
        }
        File target = new File(folder, id + ".jve");
        byte[] iv = new byte[12];
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            iv = cipher.getIV();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long total = 0;
            try (InputStream in = new BufferedInputStream(getContentResolver().openInputStream(uri));
                 FileOutputStream fileOut = new FileOutputStream(target);
                 DataOutputStream header = new DataOutputStream(fileOut)) {
                if (in == null) throw new IllegalStateException("Selected file could not be opened.");
                header.writeInt(1);
                header.writeInt(iv.length);
                header.write(iv);
                header.flush();
                try (CipherOutputStream encrypted = new CipherOutputStream(new BufferedOutputStream(fileOut), cipher)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        total += read;
                        if (total > MAX_BYTES) throw new IllegalArgumentException("File exceeds the 15 MB limit.");
                        digest.update(buffer, 0, read);
                        encrypted.write(buffer, 0, read);
                    }
                }
            }
            JSONObject item = new JSONObject();
            item.put("id", id);
            item.put("created_at", System.currentTimeMillis());
            item.put("bytes", total);
            item.put("sha256", hex(digest.digest()));
            JSONArray items = readItems();
            items.put(item);
            if (!prefs.edit().putString(INDEX, items.toString()).commit()) throw new IllegalStateException("Vault index could not be saved.");
            appendAudit("ADD", id, item.optString("sha256", ""));
            status.setText("Encrypted file saved. Original file was not changed.");
            renderItems();
        } catch (Exception e) {
            // A failed/oversized import must not leave a partial ciphertext or index entry.
            if (target.exists()) target.delete();
            status.setText("Vault import failed: " + safeMessage(e));
        }
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(KEY_ALIAS)) return (SecretKey) store.getKey(KEY_ALIAS, null);
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

    private JSONArray readItems() {
        try { return new JSONArray(prefs.getString(INDEX, "[]")); }
        catch (Exception e) { return new JSONArray(); }
    }

    private void renderItems() {
        if (list == null) return;
        list.removeAllViews();
        JSONArray items = readItems();
        if (items.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("Vault is empty. Add a file only when you intentionally choose it.");
            empty.setTextColor(0xFFFFFFFF);
            list.addView(empty);
            return;
        }
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            String id = item.optString("id", "");
            TextView row = new TextView(this);
            row.setText("Encrypted evidence • " + (i + 1) + "\n"
                    + "Saved: " + new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                    .format(new java.util.Date(item.optLong("created_at", 0)))
                    + "\nSize: " + item.optLong("bytes", 0) + " bytes"
                    + "\nSHA-256: " + item.optString("sha256", "unavailable"));
            row.setTextColor(0xFFFFFFFF);
            row.setPadding(0, 18, 0, 10);
            row.setOnClickListener(v -> showEntryOptions(id));
            list.addView(row);
        }
        TextView hint = new TextView(this);
        hint.setText("Tap an entry to remove it. The vault stores encrypted copies; it does not verify authenticity or establish legal chain of custody.");
        hint.setTextColor(0xFFE7C9CE);
        list.addView(hint);
    }

    private void showEntryOptions(String id) {
        new AlertDialog.Builder(this).setTitle("Encrypted vault entry")
                .setItems(new String[]{"Verify integrity", "Delete encrypted copy"}, (dialog, which) -> {
                    if (which == 0) verifyItem(id);
                    else confirmDelete(id);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void verifyItem(String id) {
        if (!SecureEvidenceVaultRules.isSafeId(id)) {
            status.setText("INTEGRITY CHECK FAILED • INVALID ENTRY ID");
            return;
        }
        JSONObject metadata = null;
        JSONArray items = readItems();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item != null && id.equals(item.optString("id"))) { metadata = item; break; }
        }
        if (metadata == null) {
            status.setText("INTEGRITY CHECK FAILED • ENTRY NOT IN INDEX");
            return;
        }
        File folder = new File(getFilesDir(), "secure_evidence_v1");
        File encryptedFile = new File(folder, id + ".jve");
        long actualBytes = 0;
        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(new java.io.FileInputStream(encryptedFile)))) {
            int version = input.readInt();
            int ivLength = input.readInt();
            if (!SecureEvidenceVaultRules.isValidEnvelopeHeader(version, ivLength)) {
                throw new IllegalStateException("Unsupported encrypted file header.");
            }
            byte[] iv = new byte[ivLength];
            input.readFully(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getExistingKey(), new GCMParameterSpec(128, iv));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (CipherInputStream decrypted = new CipherInputStream(input, cipher)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = decrypted.read(buffer)) != -1) {
                    actualBytes += count;
                    if (actualBytes > MAX_BYTES) throw new IllegalStateException("Decrypted content exceeds the vault size limit.");
                    digest.update(buffer, 0, count);
                }
            }
            String actualDigest = hex(digest.digest());
            boolean valid = SecureEvidenceVaultRules.matchesMetadata(
                    metadata.optString("sha256", ""), actualDigest,
                    metadata.optLong("bytes", -1), actualBytes);
            status.setText(valid
                    ? "INTEGRITY VERIFIED • AES-GCM authentication and SHA-256 metadata match"
                    : "INTEGRITY CHECK FAILED • CONTENT OR METADATA DOES NOT MATCH");
        } catch (Exception e) {
            status.setText("INTEGRITY CHECK FAILED • " + safeMessage(e));
        }
    }

    private SecretKey getExistingKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (!store.containsAlias(KEY_ALIAS)) {
            throw new IllegalStateException("Original Android Keystore key is unavailable; no replacement key was created.");
        }
        return (SecretKey) store.getKey(KEY_ALIAS, null);
    }

    private void confirmDelete(String id) {
        new AlertDialog.Builder(this).setTitle("Delete encrypted entry?")
                .setMessage("This removes the selected encrypted copy and its local index record. It does not delete the original file.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> deleteItem(id)).show();
    }

    private void deleteItem(String id) {
        if (!SecureEvidenceVaultRules.isSafeId(id)) {
            status.setText("Delete blocked • invalid vault entry ID.");
            return;
        }
        JSONArray old = readItems();
        JSONArray updated = new JSONArray();
        String digest = "";
        for (int i = 0; i < old.length(); i++) {
            JSONObject item = old.optJSONObject(i);
            if (item == null) continue;
            if (id.equals(item.optString("id"))) digest = item.optString("sha256", "");
            else updated.put(item);
        }
        File file = new File(new File(getFilesDir(), "secure_evidence_v1"), id + ".jve");
        boolean removed = !file.exists() || file.delete();
        if (!removed || !prefs.edit().putString(INDEX, updated.toString()).commit()) {
            status.setText("Delete failed; vault index was not intentionally cleared.");
            return;
        }
        appendAudit("DELETE", id, digest);
        status.setText("Encrypted copy deleted. Original file was not changed.");
        renderItems();
    }

    private void appendAudit(String action, String id, String digest) {
        try {
            String previous = prefs.getString("audit_head", "GENESIS");
            String record = System.currentTimeMillis() + "|" + action + "|" + id + "|" + digest + "|" + previous;
            String next = hex(MessageDigest.getInstance("SHA-256").digest(record.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            prefs.edit().putString("audit_head", next).putInt("audit_count", prefs.getInt("audit_count", 0) + 1).apply();
        } catch (Exception ignored) { }
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format(java.util.Locale.ROOT, "%02x", b & 0xff));
        return out.toString();
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) return "Please check the selected file and available storage.";
        return message.length() > 120 ? message.substring(0, 120) : message;
    }
}
