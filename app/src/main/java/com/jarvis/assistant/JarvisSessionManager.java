package com.jarvis.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Base64;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Local owner gate. Every app launch starts as Guest; owner access requires the configured PIN. */
public final class JarvisSessionManager {
    private static final String PREFS = "jarvis_owner_security";
    private static final String SALT = "owner_pin_salt";
    private static final String HASH = "owner_pin_hash";
    private static final String FAILED_ATTEMPTS = "owner_pin_failed_attempts";
    private static final String LOCKOUT_UNTIL_WALL_MS = "owner_pin_lockout_until_wall_ms";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 60_000L;
    private static final long OWNER_IDLE_TIMEOUT_MS = 15 * 60_000L;
    private static volatile boolean ownerSession = false;
    private static volatile long lastOwnerActivityElapsedMs = 0L;

    private JarvisSessionManager() {}

    public static boolean hasOwnerPin(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return p.contains(SALT) && p.contains(HASH);
    }

    /**
     * Checks and refreshes an active owner session. A session expires after 15 minutes
     * without an owner-gated interaction. The app still starts in Guest after process restart.
     */
    /** Backward-compatible context overload used by existing app components. */
    public static boolean isOwner(Context context) {
        return isOwner();
    }

    public static synchronized boolean isOwner() {
        if (!ownerSession) return false;
        long now = SystemClock.elapsedRealtime();
        if (lastOwnerActivityElapsedMs <= 0L
                || now - lastOwnerActivityElapsedMs >= OWNER_IDLE_TIMEOUT_MS) {
            ownerSession = false;
            lastOwnerActivityElapsedMs = 0L;
            return false;
        }
        lastOwnerActivityElapsedMs = now;
        return true;
    }

    public static synchronized void enterGuest() {
        ownerSession = false;
        lastOwnerActivityElapsedMs = 0L;
    }

    public static synchronized boolean setOwnerPin(Context context, char[] pin) {
        if (!valid(pin)) return false;
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = derive(pin, salt);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                    .putString(HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                    .putInt(FAILED_ATTEMPTS, 0)
                    .remove(LOCKOUT_UNTIL_WALL_MS)
                    .apply();
            ownerSession = true;
            lastOwnerActivityElapsedMs = SystemClock.elapsedRealtime();
            return true;
        } catch (Exception e) {
            enterGuest();
            return false;
        }
    }

    /** Returns zero when PIN entry is not currently rate-limited. */
    public static long getRemainingLockoutMillis(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long until = p.getLong(LOCKOUT_UNTIL_WALL_MS, 0L);
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public static synchronized boolean unlockOwner(Context context, char[] pin) {
        if (!valid(pin) || getRemainingLockoutMillis(context) > 0L) {
            enterGuest();
            return false;
        }
        try {
            SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String saltText = p.getString(SALT, null);
            String hashText = p.getString(HASH, null);
            if (saltText == null || hashText == null) {
                enterGuest();
                return false;
            }
            byte[] expected = Base64.decode(hashText, Base64.NO_WRAP);
            byte[] actual = derive(pin, Base64.decode(saltText, Base64.NO_WRAP));
            if (expected.length != actual.length) {
                recordFailedAttempt(p);
                enterGuest();
                return false;
            }
            int diff = 0;
            for (int i = 0; i < expected.length; i++) diff |= expected[i] ^ actual[i];
            if (diff == 0) {
                p.edit().putInt(FAILED_ATTEMPTS, 0).remove(LOCKOUT_UNTIL_WALL_MS).apply();
                ownerSession = true;
                lastOwnerActivityElapsedMs = SystemClock.elapsedRealtime();
                return true;
            }
            recordFailedAttempt(p);
            enterGuest();
            return false;
        } catch (Exception e) {
            enterGuest();
            return false;
        }
    }

    private static void recordFailedAttempt(SharedPreferences prefs) {
        int failures = prefs.getInt(FAILED_ATTEMPTS, 0) + 1;
        SharedPreferences.Editor edit = prefs.edit().putInt(FAILED_ATTEMPTS, failures);
        if (failures >= MAX_FAILED_ATTEMPTS) {
            edit.putLong(LOCKOUT_UNTIL_WALL_MS, System.currentTimeMillis() + LOCKOUT_DURATION_MS)
                    .putInt(FAILED_ATTEMPTS, 0);
        }
        edit.apply();
    }

    private static boolean valid(char[] pin) {
        return pin != null && pin.length >= 6 && pin.length <= 12;
    }

    private static byte[] derive(char[] pin, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, 120000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
