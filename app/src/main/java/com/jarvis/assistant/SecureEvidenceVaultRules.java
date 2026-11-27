package com.jarvis.assistant;

import java.security.MessageDigest;
import java.util.Locale;

/** Pure validation rules for the local encrypted evidence envelope and its metadata. */
public final class SecureEvidenceVaultRules {
    private SecureEvidenceVaultRules() { }

    public static boolean isSafeId(String id) {
        return id != null && id.matches("[0-9a-fA-F-]{36}");
    }

    public static boolean isValidEnvelopeHeader(int version, int ivLength) {
        return version == 1 && ivLength == 12;
    }

    public static boolean matchesMetadata(String expectedDigest, String actualDigest,
                                          long expectedBytes, long actualBytes) {
        if (expectedDigest == null || actualDigest == null
                || !expectedDigest.matches("(?i)[0-9a-f]{64}")
                || !actualDigest.matches("(?i)[0-9a-f]{64}")
                || expectedBytes < 0 || actualBytes < 0
                || expectedBytes != actualBytes) return false;
        return MessageDigest.isEqual(
                expectedDigest.toLowerCase(Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                actualDigest.toLowerCase(Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }
}
