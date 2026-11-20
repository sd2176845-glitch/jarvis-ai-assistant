package com.jarvis.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/** Local owner/guest session gate. This is an app-level privacy guard, not device encryption. */
public final class JarvisAccessController {
    private static final String PREFS = "jarvis_access";
    private static final String SALT = "owner_pin_salt";
    private static final String HASH = "owner_pin_hash";
    private static final String ROLE = "active_role";
    private JarvisAccessController() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    public static boolean hasOwnerPin(Context c) { return !prefs(c).getString(HASH, "").isEmpty(); }
    public static boolean isOwner(Context c) {
        return !hasOwnerPin(c) || "owner".equals(prefs(c).getString(ROLE, "owner"));
    }
    public static void enterGuest(Context c) { prefs(c).edit().putString(ROLE, "guest").apply(); }
    public static void setOwnerPin(Context c, String pin) {
        if (pin == null || !pin.matches("\\d{6,8}")) throw new IllegalArgumentException("PIN must be 6 to 8 digits");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        prefs(c).edit().putString(SALT, hex(salt)).putString(HASH, hash(salt, pin)).putString(ROLE, "owner").apply();
    }
    public static boolean unlockOwner(Context c, String pin) {
        SharedPreferences p = prefs(c);
        String stored = p.getString(HASH, "");
        if (stored.isEmpty()) return false;
        byte[] salt = unhex(p.getString(SALT, ""));
        if (salt.length == 0) return false;
        boolean ok = MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8),
                hash(salt, pin == null ? "" : pin).getBytes(StandardCharsets.UTF_8));
        if (ok) p.edit().putString(ROLE, "owner").apply();
        return ok;
    }
    private static String hash(byte[] salt, String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return hex(md.digest(pin.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    private static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte v : b) s.append(String.format(java.util.Locale.ROOT, "%02x", v & 255));
        return s.toString();
    }
    private static byte[] unhex(String s) {
        try {
            if (s.length() % 2 != 0) return new byte[0];
            byte[] out = new byte[s.length()/2];
            for (int i=0;i<out.length;i++) out[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);
            return out;
        } catch (Exception e) { return new byte[0]; }
    }
}
