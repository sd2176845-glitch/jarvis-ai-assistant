package com.jarvis.assistant;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import static org.junit.Assert.*;

public class AssetInventoryArchiveTest {
    @Test
    public void encryptionRoundTripsAndRejectsWrongPasswordAndTampering() throws Exception {
        byte[] plaintext = "JARVIS archive test with private inventory text".getBytes(StandardCharsets.UTF_8);
        char[] password = "correct horse battery".toCharArray();
        byte[] archive = AssetInventoryArchive.encrypt(plaintext, password);
        assertTrue(archive.length > plaintext.length);
        assertFalse(new String(archive, StandardCharsets.ISO_8859_1).contains("private inventory text"));
        assertArrayEquals(plaintext, AssetInventoryArchive.decrypt(archive, password));

        try {
            AssetInventoryArchive.decrypt(archive, "incorrect horse battery".toCharArray());
            fail("Wrong passphrase must not decrypt the archive.");
        } catch (GeneralSecurityException expected) {
            // Expected authenticated-decryption failure.
        }

        byte[] changed = archive.clone();
        changed[changed.length - 1] ^= 0x01;
        try {
            AssetInventoryArchive.decrypt(changed, password);
            fail("Modified ciphertext must fail authentication.");
        } catch (GeneralSecurityException expected) {
            // Expected GCM tag failure.
        }
        java.util.Arrays.fill(password, (char) 0);
    }

    @Test
    public void invalidPasswordAndMalformedArchiveAreRejected() throws Exception {
        try {
            AssetInventoryArchive.encrypt(new byte[]{1, 2, 3}, "short".toCharArray());
            fail("Short passphrases must be rejected.");
        } catch (GeneralSecurityException expected) {
            // Expected.
        }
        try {
            AssetInventoryArchive.decrypt(new byte[]{1, 2, 3},
                    "long enough passphrase".toCharArray());
            fail("Malformed archive must be rejected.");
        } catch (GeneralSecurityException expected) {
            // Expected.
        }
    }
}
