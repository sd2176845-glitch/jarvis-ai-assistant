package com.jarvis.assistant;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.List;
import java.util.Calendar;
import java.io.ByteArrayOutputStream;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import android.util.Base64;

/**
 * Local, permission-conscious building blocks for JARVIS.
 * This class does not claim to provide cloud authentication, calendar sync,
 * smart-home control, or live web intelligence without provider setup.
 */
public final class JarvisFeatureKit {
    private static final String PREFS = "jarvis_feature_kit";
    private static final String KEY_ALIAS = "jarvis_local_v1";
    private static final int MAX_BACKUP_BYTES = 1024 * 1024;

    public static final class RestoreResult {
        public final int restoredEntries;
        public final int skippedDeviceBoundEntries;
        RestoreResult(int restoredEntries, int skippedDeviceBoundEntries) {
            this.restoredEntries = restoredEntries;
            this.skippedDeviceBoundEntries = skippedDeviceBoundEntries;
        }
    }
    private final Context context;
    private final SharedPreferences prefs;

    public JarvisFeatureKit(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // Feature switches: each optional integration can be independently disabled.
    public boolean isEnabled(String feature, boolean defaultValue) {
        return prefs.getBoolean("feature." + feature, defaultValue);
    }

    public void setEnabled(String feature, boolean enabled) {
        prefs.edit().putBoolean("feature." + feature, enabled).apply();
    }

    // Owner-approved local preferences, encrypted with an Android Keystore key.
    public void savePrivateValue(String key, String value) throws Exception {
        if (key == null || key.trim().isEmpty() || value == null) {
            throw new IllegalArgumentException("A non-empty key and value are required");
        }
        prefs.edit().putString("secure." + key, encryptPrivateValue(value)).apply();
    }

    public String readPrivateValue(String key) throws Exception {
        if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("Key is required");
        String packed = prefs.getString("secure." + key, null);
        return packed == null ? null : decryptPrivateValue(packed);
    }

    private String encryptPrivateValue(String value) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP) + ":"
                + Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    private String decryptPrivateValue(String packed) throws Exception {
        if (packed == null) throw new IllegalArgumentException("Encrypted value is missing");
        String[] parts = packed.split(":", 2);
        if (parts.length != 2) throw new IllegalStateException("Encrypted value is invalid");
        byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] data = Base64.decode(parts[1], Base64.NO_WRAP);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        java.security.Key key = store.getKey(KEY_ALIAS, null);
        if (key instanceof SecretKey) return (SecretKey) key;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    // Backup contains this namespace only. Android Keystore ciphertext is device-bound:
    // it is re-encrypted on restore when the original key is available, otherwise skipped.
    public File exportBackup(File destination) throws Exception {
        if (destination == null) throw new IllegalArgumentException("Backup destination is required");
        JSONObject root = new JSONObject();
        root.put("schema", 1);
        root.put("createdAt", System.currentTimeMillis());
        root.put("secureValuesDeviceBound", true);
        JSONObject values = new JSONObject();
        for (String key : prefs.getAll().keySet()) {
            Object value = prefs.getAll().get(key);
            if (value instanceof String || value instanceof Boolean || value instanceof Integer
                    || value instanceof Long || value instanceof Float) values.put(key, value);
        }
        root.put("preferences", values);
        byte[] bytes = root.toString(2).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BACKUP_BYTES) throw new IllegalArgumentException("Backup exceeds 1 MB");
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Could not create backup folder");
        }
        File temporary = new File(destination.getAbsolutePath() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(temporary)) {
            out.write(bytes);
            out.getFD().sync();
        }
        if (destination.exists() && !destination.delete()) {
            temporary.delete();
            throw new IllegalStateException("Could not replace existing backup");
        }
        if (!temporary.renameTo(destination)) {
            temporary.delete();
            throw new IllegalStateException("Could not finalize backup file");
        }
        return destination;
    }

    public RestoreResult restoreBackup(File source) throws Exception {
        if (source == null || !source.isFile()) throw new IllegalArgumentException("Backup file not found");
        long size = source.length();
        if (size <= 0 || size > MAX_BACKUP_BYTES) throw new IllegalArgumentException("Backup must be between 1 byte and 1 MB");
        byte[] bytes;
        try (FileInputStream in = new FileInputStream(source);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[8192];
            int count;
            while ((count = in.read(chunk)) != -1) {
                if (buffer.size() + count > MAX_BACKUP_BYTES) {
                    throw new IllegalArgumentException("Backup exceeds 1 MB");
                }
                buffer.write(chunk, 0, count);
            }
            bytes = buffer.toByteArray();
        }
        JSONObject root = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
        if (root.optInt("schema", -1) != 1) throw new IllegalArgumentException("Unsupported backup version");
        JSONObject values = root.getJSONObject("preferences");
        SharedPreferences.Editor edit = prefs.edit();
        JSONArray names = values.names();
        int restored = 0;
        int skippedDeviceBound = 0;
        if (names != null) for (int i = 0; i < names.length(); i++) {
            String name = names.getString(i);
            Object value = values.get(name);
            if (name.startsWith("secure.")) {
                if (!(value instanceof String)) { skippedDeviceBound++; continue; }
                try {
                    String plain = decryptPrivateValue((String) value);
                    edit.putString(name, encryptPrivateValue(plain));
                    restored++;
                } catch (Exception deviceKeyUnavailable) {
                    // Keystore keys are not portable. Never restore unusable ciphertext.
                    skippedDeviceBound++;
                }
            } else if (value instanceof String) { edit.putString(name, (String) value); restored++; }
            else if (value instanceof Boolean) { edit.putBoolean(name, (Boolean) value); restored++; }
            else if (value instanceof Integer) { edit.putInt(name, (Integer) value); restored++; }
            else if (value instanceof Long) { edit.putLong(name, (Long) value); restored++; }
            else if (value instanceof Double) { edit.putFloat(name, ((Double) value).floatValue()); restored++; }
            else if (value instanceof Float) { edit.putFloat(name, (Float) value); restored++; }
        }
        edit.apply();
        return new RestoreResult(restored, skippedDeviceBound);
    }

    // Offline, non-destructive command suggestions; sensitive actions are never executed here.
    public String classifyOfflineCommand(String spokenText) {
        return OfflineCommandClassifier.classify(spokenText);
    }

    public Intent createSettingsIntent() {
        return new Intent(Settings.ACTION_SETTINGS);
    }

    // Read-only device diagnostics. Null means the OS did not expose the value.
    public JSONObject diagnostics() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("android", Build.VERSION.RELEASE);
        result.put("sdk", Build.VERSION.SDK_INT);
        result.put("manufacturer", Build.MANUFACTURER);
        result.put("model", Build.MODEL);
        BatteryManager battery = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (battery != null) result.put("batteryPercent",
                battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        result.put("storageAvailableBytes", stat.getAvailableBytes());
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean online = false;
        if (cm != null) {
            android.net.Network network = cm.getActiveNetwork();
            NetworkCapabilities caps = network == null ? null : cm.getNetworkCapabilities(network);
            online = caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        result.put("networkTransportConnected", online);
        return result;
    }

    // Screen-time overview is available only after the owner grants Usage Access in Settings.
    public boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public Intent usageAccessSettingsIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

    public long todayForegroundMillis() {
        if (!hasUsageAccess()) return -1L;
        UsageStatsManager manager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) return -1L;
        long now = System.currentTimeMillis();
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                start.getTimeInMillis(), now);
        long total = 0L;
        if (stats != null) for (UsageStats item : stats) total += item.getTotalTimeInForeground();
        return total;
    }

    // Calendar integration hands off to the user's calendar app for review.
    public Intent createCalendarEventIntent(String title, long startMillis, long endMillis) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Event title is required");
        if (startMillis <= 0L || endMillis <= startMillis) throw new IllegalArgumentException("Event time range is invalid");
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(android.provider.CalendarContract.Events.CONTENT_URI);
        intent.putExtra(android.provider.CalendarContract.Events.TITLE, title);
        intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
        intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
        return intent;
    }
}
