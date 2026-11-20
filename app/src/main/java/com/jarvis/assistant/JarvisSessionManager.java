package com.jarvis.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Local owner gate. Every app launch starts as Guest; owner access requires the configured PIN. */
public final class JarvisSessionManager {
    private static final String PREFS = "jarvis_owner_security";
    private static final String SALT = "owner_pin_salt";
    private static final String HASH = "owner_pin_hash";
    private static volatile boolean ownerSession = false;
    private JarvisSessionManager() {}

    public static boolean hasOwnerPin(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return p.contains(SALT) && p.contains(HASH);
    }

    public static boolean isOwner(Context context) { return ownerSession; }
    public static void enterGuest() { ownerSession = false; }

    public static boolean setOwnerPin(Context context, char[] pin) {
        if (!valid(pin)) return false;
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = derive(pin, salt);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                    .putString(HASH, Base64.encodeToString(hash, Base64.NO_WRAP)).apply();
            ownerSession = true;
            return true;
        } catch (Exception e) { return false; }
    }

    public static boolean unlockOwner(Context context, char[] pin) {
        if (!valid(pin)) return false;
        try {
            SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String saltText = p.getString(SALT, null);
            String hashText = p.getString(HASH, null);
            if (saltText == null || hashText == null) return false;
            byte[] expected = Base64.decode(hashText, Base64.NO_WRAP);
            byte[] actual = derive(pin, Base64.decode(saltText, Base64.NO_WRAP));
            if (expected.length != actual.length) return false;
            int diff = 0;
            for (int i = 0; i < expected.length; i++) diff |= expected[i] ^ actual[i];
            ownerSession = diff == 0;
            return ownerSession;
        } catch (Exception e) { return false; }
    }

    private static boolean valid(char[] pin) { return pin != null && pin.length >= 6 && pin.length <= 12; }

    private static byte[] derive(char[] pin, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, 120000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        } finally { spec.clearPassword(); }
    }
}
