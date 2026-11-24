package com.jarvis.assistant;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Password-based authenticated encryption for owner-selected asset archives. */
public final class AssetInventoryArchive {
    private AssetInventoryArchive() { }
    public static final int MIN_PASSWORD_CHARS = 12;
    public static final int MAX_PASSWORD_CHARS = 128;
    public static final int MAX_PLAINTEXT_BYTES = 1024 * 1024;
    public static final int MAX_ARCHIVE_BYTES = MAX_PLAINTEXT_BYTES + 64;
    private static final byte[] MAGIC = new byte[]{'J','V','A','1'};
    private static final int ITERATIONS = 150000;
    private static final int HEADER_BYTES = 36;

    public static byte[] encrypt(byte[] plaintext, char[] password) throws GeneralSecurityException {
        validatePassword(password);
        if (plaintext == null || plaintext.length > MAX_PLAINTEXT_BYTES)
            throw new GeneralSecurityException("Invalid archive payload.");
        byte[] salt = new byte[16], iv = new byte[12], header = new byte[HEADER_BYTES];
        new SecureRandom().nextBytes(salt);
        new SecureRandom().nextBytes(iv);
        ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN)
                .put(MAGIC).putInt(ITERATIONS).put(salt).put(iv);
        byte[] key = null, ciphertext = null;
        try {
            key = deriveKey(password, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, iv));
            cipher.updateAAD(header);
            ciphertext = cipher.doFinal(plaintext);
            byte[] result = new byte[header.length + ciphertext.length];
            System.arraycopy(header, 0, result, 0, header.length);
            System.arraycopy(ciphertext, 0, result, header.length, ciphertext.length);
            return result;
        } finally {
            wipe(salt); wipe(iv); wipe(key); wipe(ciphertext);
        }
    }

    public static byte[] decrypt(byte[] archive, char[] password) throws GeneralSecurityException {
        validatePassword(password);
        if (archive == null || archive.length < HEADER_BYTES + 16
                || archive.length > MAX_ARCHIVE_BYTES)
            throw new GeneralSecurityException("Invalid archive length.");
        byte[] header = Arrays.copyOfRange(archive, 0, HEADER_BYTES);
        ByteBuffer input = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN);
        byte[] magic = new byte[4];
        input.get(magic);
        if (!Arrays.equals(MAGIC, magic)) {
            wipe(header); wipe(magic);
            throw new GeneralSecurityException("Unsupported archive format.");
        }
        int iterations = input.getInt();
        if (iterations != ITERATIONS) {
            wipe(header); wipe(magic);
            throw new GeneralSecurityException("Unsupported archive parameters.");
        }
        byte[] salt = new byte[16], iv = new byte[12];
        input.get(salt); input.get(iv);
        byte[] ciphertext = Arrays.copyOfRange(archive, HEADER_BYTES, archive.length);
        byte[] key = null;
        try {
            key = deriveKey(password, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, iv));
            cipher.updateAAD(header);
            byte[] plaintext = cipher.doFinal(ciphertext);
            if (plaintext.length > MAX_PLAINTEXT_BYTES) {
                wipe(plaintext);
                throw new GeneralSecurityException("Archive payload is too large.");
            }
            return plaintext;
        } finally {
            wipe(header); wipe(magic); wipe(salt); wipe(iv); wipe(ciphertext); wipe(key);
        }
    }

    private static void validatePassword(char[] password) throws GeneralSecurityException {
        if (password == null || password.length < MIN_PASSWORD_CHARS
                || password.length > MAX_PASSWORD_CHARS)
            throw new GeneralSecurityException("Use a passphrase between 12 and 128 characters.");
        boolean nonWhitespace = false;
        for (char ch : password) if (!Character.isWhitespace(ch)) { nonWhitespace = true; break; }
        if (!nonWhitespace) throw new GeneralSecurityException("Passphrase cannot contain only spaces.");
    }

    /** PBKDF2-HMAC-SHA256 built on Mac to support Android API 23. */
    private static byte[] deriveKey(char[] password, byte[] salt) throws GeneralSecurityException {
        ByteBuffer encoded;
        try {
            encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        } catch (Exception e) {
            throw new GeneralSecurityException("Passphrase encoding failed.", e);
        }
        byte[] passwordBytes = new byte[encoded.remaining()];
        encoded.get(passwordBytes);
        if (encoded.hasArray()) Arrays.fill(encoded.array(), (byte) 0);
        byte[] block = new byte[salt.length + 4];
        System.arraycopy(salt, 0, block, 0, salt.length);
        block[block.length - 1] = 1;
        byte[] u = null, aggregate = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(passwordBytes, "HmacSHA256"));
            u = mac.doFinal(block);
            aggregate = u.clone();
            for (int i = 1; i < ITERATIONS; i++) {
                byte[] next = mac.doFinal(u);
                wipe(u);
                u = next;
                for (int j = 0; j < aggregate.length; j++) aggregate[j] ^= u[j];
            }
            return Arrays.copyOf(aggregate, 32);
        } finally {
            wipe(passwordBytes); wipe(block); wipe(u); wipe(aggregate);
        }
    }

    private static void wipe(byte[] value) {
        if (value != null) Arrays.fill(value, (byte) 0);
    }
}
