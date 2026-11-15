package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class OfflineCommandClassifierTest {
    @Test public void classifiesBatteryCommand() {
        assertEquals("DEVICE_BATTERY", OfflineCommandClassifier.classify("battery kitna hai"));
    }

    @Test public void classifiesHindiTimeCommand() {
        assertEquals("DEVICE_TIME", OfflineCommandClassifier.classify("samay batao"));
    }

    @Test public void classifiesHindiDateCommand() {
        assertEquals("DEVICE_DATE", OfflineCommandClassifier.classify("aaj ki tarikh"));
    }

    @Test public void classifiesLocalNoteCommand() {
        assertEquals("LOCAL_NOTE", OfflineCommandClassifier.classify("show my note"));
    }

    @Test public void classifiesSettingsCommand() {
        assertEquals("OPEN_SETTINGS", OfflineCommandClassifier.classify("open settings"));
    }

    @Test public void sensitiveCommandsRequireConfirmation() {
        assertEquals("NEEDS_ONLINE_OR_OWNER_CONFIRMATION",
                OfflineCommandClassifier.classify("call maa"));
        assertEquals("NEEDS_ONLINE_OR_OWNER_CONFIRMATION",
                OfflineCommandClassifier.classify("make a payment"));
    }

    @Test public void normalizesPunctuationAndHindiScript() {
        assertEquals("DEVICE_BATTERY", OfflineCommandClassifier.classify("बैटरी, कितनी है?"));
        assertEquals("DEVICE_TIME", OfflineCommandClassifier.classify("समय बताओ!"));
        assertEquals("DEVICE_DATE", OfflineCommandClassifier.classify("तारीख बताओ"));
    }

    @Test public void normalizesRepeatedWhitespace() {
        assertEquals("DEVICE_BATTERY", OfflineCommandClassifier.classify("  battery   kitna   hai  "));
    }

    @Test public void unknownAndNullCommandsStayUnknown() {
        assertEquals("UNKNOWN_OFFLINE_COMMAND", OfflineCommandClassifier.classify("play a song"));
        assertEquals("UNKNOWN_OFFLINE_COMMAND", OfflineCommandClassifier.classify(null));
    }
}
