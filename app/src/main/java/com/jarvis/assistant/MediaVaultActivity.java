package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MediaVaultActivity extends Activity {
    private static final int REQUEST_BACKUP = 7101;
    private static final int REQUEST_RESTORE = 7102;
    private static final int REQUEST_EXPORT = 7103;
    private static final int REQUEST_IMPORT = 7104;
    private static final String VAULT_DIR = "media_vault";

    private LinearLayout content;
    private TextView status;
    private File pendingRestore;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF080509);
        getWindow().setNavigationBarColor(0xFF080509);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(24), dp(20), dp(24));
        content.setBackgroundColor(0xFF080509);
        scroll.addView(content);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S.  •  PERSONAL MEDIA VAULT");
        title.setTextColor(0xFFFF4652);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView explanation = new TextView(this);
        explanation.setText("Choose photos, videos or audio files to make a separate copy inside JARVIS. Restore a saved copy whenever you need it. JARVIS never scans your gallery automatically.");
        explanation.setTextColor(0xFFE5E0E5);
        explanation.setTextSize(14);
        explanation.setPadding(0, dp(18), 0, dp(18));
        content.addView(explanation);

        addButton("BACK UP SELECTED MEDIA", v -> chooseMediaForBackup());
        addButton("VIEW VAULT • RESTORE / EXPORT / DELETE", v -> showVaultItems());
        addButton("IMPORT BACKUP FROM SD CARD / CLOUD", v -> chooseBackupToImport());

        status = new TextView(this);
        status.setTextColor(0xFFBDB5BD);
        status.setTextSize(13);
        status.setPadding(0, dp(18), 0, dp(12));
        content.addView(status);
        updateStatus();

        TextView warning = new TextView(this);
        warning.setText("Storage note: this first version stores copies in JARVIS's private app storage on this phone. It is separate from the gallery, but it is not cloud backup and can be lost if JARVIS is uninstalled, its app data is cleared, or the phone storage fails. Export/off-device encrypted backup is a later step. Keep enough free storage available.");
        warning.setTextColor(0xFFFFC7A8);
        warning.setTextSize(12);
        warning.setPadding(0, dp(12), 0, dp(12));
        content.addView(warning);

        addButton("CLOSE", v -> finish());
    }

    private void addButton(String label, android.view.View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF9E2438));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(10);
        content.addView(button, params);
        button.setOnClickListener(listener);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private File getVaultDirectory() {
        File dir = new File(getFilesDir(), VAULT_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private void updateStatus() {
        File[] files = getVaultDirectory().listFiles();
        int count = files == null ? 0 : files.length;
        long bytes = 0L;
        if (files != null) {
            for (File file : files) if (file.isFile()) bytes += file.length();
        }
        status.setText("Vault copies: " + count + "  •  Stored: " + android.text.format.Formatter.formatShortFileSize(this, bytes));
    }

    private void chooseMediaForBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*", "audio/*"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_BACKUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQUEST_BACKUP) {
            ArrayList<Uri> selected = new ArrayList<>();
            ClipData clip = data.getClipData();
            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i++) selected.add(clip.getItemAt(i).getUri());
            } else if (data.getData() != null) {
                selected.add(data.getData());
            }
            int saved = 0;
            int failed = 0;
            for (Uri uri : selected) {
                try {
                    copyUriIntoVault(uri);
                    saved++;
                } catch (Exception e) {
                    failed++;
                }
            }
            Toast.makeText(this, "Backup complete: " + saved + " saved, " + failed + " failed", Toast.LENGTH_LONG).show();
            updateStatus();
        } else if ((requestCode == REQUEST_RESTORE || requestCode == REQUEST_EXPORT) && pendingRestore != null) {
            try {
                copyFileToUri(pendingRestore, data.getData());
                Toast.makeText(this, requestCode == REQUEST_EXPORT
                        ? "Backup exported to the selected location"
                        : "Restore copy completed", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, requestCode == REQUEST_EXPORT
                        ? "Export failed. Check the destination and try again."
                        : "Restore failed. Check available storage and try again.", Toast.LENGTH_LONG).show();
            } finally {
                pendingRestore = null;
            }
        } else if (requestCode == REQUEST_IMPORT) {
            ArrayList<Uri> selected = new ArrayList<>();
            ClipData clip = data.getClipData();
            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i++) selected.add(clip.getItemAt(i).getUri());
            } else if (data.getData() != null) {
                selected.add(data.getData());
            }
            int imported = 0;
            int failed = 0;
            for (Uri uri : selected) {
                try {
                    copyUriIntoVault(uri);
                    imported++;
                } catch (Exception e) {
                    failed++;
                }
            }
            Toast.makeText(this, "Import complete: " + imported + " added, " + failed + " failed", Toast.LENGTH_LONG).show();
            updateStatus();
        }
    }

    private void copyUriIntoVault(Uri source) throws Exception {
        String displayName = queryDisplayName(source);
        String safeName = displayName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.trim().isEmpty()) safeName = "media_file";
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        File destination = new File(getVaultDirectory(), stamp + "_" + safeName);

        try (InputStream input = getContentResolver().openInputStream(source);
             OutputStream output = new FileOutputStream(destination)) {
            if (input == null) throw new IllegalStateException("Unable to read selected file");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            output.flush();
        } catch (Exception e) {
            if (destination.exists()) destination.delete();
            throw e;
        }
    }

    private String queryDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        String last = uri.getLastPathSegment();
        return last == null ? "media_file" : last;
    }

    private void chooseBackupToImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*", "audio/*", "application/octet-stream"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    private void showVaultItems() {
        File[] files = getVaultDirectory().listFiles();
        if (files == null || files.length == 0) {
            new AlertDialog.Builder(this).setTitle("JARVIS Media Vault")
                    .setMessage("No backed-up media yet. Select media and create a backup first.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        String[] labels = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            labels[i] = files[i].getName() + "  •  " + android.text.format.Formatter.formatShortFileSize(this, files[i].length());
        }
        new AlertDialog.Builder(this).setTitle("Vault • Choose a file")
                .setItems(labels, (dialog, which) -> showFileActions(files[which]))
                .setNegativeButton("Close", null).show();
    }

    private void showFileActions(File file) {
        String[] actions = {"Restore copy to phone", "Export backup to SD card / cloud", "Delete vault copy"};
        new AlertDialog.Builder(this).setTitle(file.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) beginRestore(file);
                    else if (which == 1) beginExport(file);
                    else confirmVaultDelete(file);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void beginExport(File file) {
        pendingRestore = file;
        String name = originalDisplayName(file);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(getMimeType(name));
        intent.putExtra(Intent.EXTRA_TITLE, name);
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    private String originalDisplayName(File file) {
        String name = file.getName();
        if (name.length() > 20 && name.charAt(8) == '_' && name.charAt(15) == '_') return name.substring(20);
        return name;
    }

    private void beginRestore(File file) {
        pendingRestore = file;
        String name = originalDisplayName(file);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(getMimeType(name));
        intent.putExtra(Intent.EXTRA_TITLE, name);
        startActivityForResult(intent, REQUEST_RESTORE);
    }

    private String getMimeType(String name) {
        String lower = name.toLowerCase(Locale.US);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".m4a")) return "audio/mp4";
        if (lower.endsWith(".wav")) return "audio/wav";
        return "*/*";
    }

    private void copyFileToUri(File source, Uri destination) throws Exception {
        if (destination == null) throw new IllegalArgumentException("No restore destination selected");
        try (InputStream input = new FileInputStream(source);
             OutputStream output = getContentResolver().openOutputStream(destination, "w")) {
            if (output == null) throw new IllegalStateException("Unable to open destination");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            output.flush();
        }
    }

    private void confirmVaultDelete(File file) {
        new AlertDialog.Builder(this).setTitle("Delete vault copy?")
                .setMessage("This deletes only the JARVIS Vault copy. It does not delete or change the original phone file. This cannot be undone.")
                .setNegativeButton("Keep copy", null)
                .setPositiveButton("Delete vault copy", (dialog, which) -> {
                    boolean deleted = file.delete();
                    Toast.makeText(this, deleted ? "Vault copy deleted" : "Could not delete vault copy", Toast.LENGTH_LONG).show();
                    updateStatus();
                }).show();
    }
}
