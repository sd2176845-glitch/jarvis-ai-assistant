package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class SecureEvidenceVaultRulesTest {
    @Test public void acceptsOnlyVersionOneTwelveByteGcmHeader() {
        assertTrue(SecureEvidenceVaultRules.isValidEnvelopeHeader(1, 12));
        assertFalse(SecureEvidenceVaultRules.isValidEnvelopeHeader(2, 12));
        assertFalse(SecureEvidenceVaultRules.isValidEnvelopeHeader(1, 16));
    }

    @Test public void acceptsOnlyUuidStyleVaultIds() {
        assertTrue(SecureEvidenceVaultRules.isSafeId("123e4567-e89b-12d3-a456-426614174000"));
        assertFalse(SecureEvidenceVaultRules.isSafeId("../private"));
        assertFalse(SecureEvidenceVaultRules.isSafeId(null));
    }

    @Test public void metadataMustMatchDigestAndPlaintextByteCount() {
        String digest = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        assertTrue(SecureEvidenceVaultRules.matchesMetadata(digest, digest.toUpperCase(), 12, 12));
        assertFalse(SecureEvidenceVaultRules.matchesMetadata(digest, digest, 12, 13));
        assertFalse(SecureEvidenceVaultRules.matchesMetadata(digest, "bad", 12, 12));
    }
}
