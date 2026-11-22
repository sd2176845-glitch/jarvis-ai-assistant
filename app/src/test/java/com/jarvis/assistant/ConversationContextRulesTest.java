package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConversationContextRulesTest {
    @Test public void contextExpiresAfterConfiguredTtl() {
        long now = 10_000_000L;
        assertTrue(ConversationContextRules.isFresh(now - 1000L, now, 60_000L));
        assertFalse(ConversationContextRules.isFresh(now - 60_001L, now, 60_000L));
    }

    @Test public void invalidOrFutureTimestampsAreNotFresh() {
        assertFalse(ConversationContextRules.isFresh(0L, 1000L, 60_000L));
        assertFalse(ConversationContextRules.isFresh(2000L, 1000L, 60_000L));
    }
}
