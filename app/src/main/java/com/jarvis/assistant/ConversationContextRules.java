package com.jarvis.assistant;

/** Time-bounded reuse rules for recent conversation context. */
public final class ConversationContextRules {
    private ConversationContextRules() { }

    public static boolean isFresh(long savedAtMillis, long nowMillis, long ttlMillis) {
        if (savedAtMillis <= 0L || nowMillis < savedAtMillis || ttlMillis <= 0L) return false;
        return nowMillis - savedAtMillis <= ttlMillis;
    }
}
