package com.jarvis.assistant;

import org.junit.Test;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class SmartNotificationRulesTest {
    private final TimeZone utc = TimeZone.getTimeZone("UTC");

    private long at(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance(utc);
        c.clear();
        c.set(year, month - 1, day, hour, minute, 0);
        return c.getTimeInMillis();
    }

    @Test public void snoozeAddsTenMinutes() {
        long now = 123456789L;
        assertEquals(now + 10L * 60L * 1000L, SmartNotificationRules.snoozeUntil(now));
    }

    @Test public void validatesQuietHourWindow() {
        assertTrue(SmartNotificationRules.isValidWindow(22 * 60, 7 * 60));
        assertFalse(SmartNotificationRules.isValidWindow(480, 480));
        assertFalse(SmartNotificationRules.isValidWindow(-1, 480));
        assertFalse(SmartNotificationRules.isValidWindow(0, 1440));
    }

    @Test public void recognizesOvernightQuietHours() {
        assertTrue(SmartNotificationRules.isQuietHours(at(2026, 11, 23, 23, 30), 22 * 60, 7 * 60, utc));
        assertTrue(SmartNotificationRules.isQuietHours(at(2026, 11, 23, 6, 30), 22 * 60, 7 * 60, utc));
        assertFalse(SmartNotificationRules.isQuietHours(at(2026, 11, 23, 12, 0), 22 * 60, 7 * 60, utc));
    }

    @Test public void recognizesSameDayQuietHours() {
        assertTrue(SmartNotificationRules.isQuietHours(at(2026, 11, 23, 13, 0), 12 * 60, 14 * 60, utc));
        assertFalse(SmartNotificationRules.isQuietHours(at(2026, 11, 23, 14, 0), 12 * 60, 14 * 60, utc));
    }

    @Test public void defersToCorrectEndAcrossMidnight() {
        long lateNight = at(2026, 11, 23, 23, 30);
        assertEquals(at(2026, 11, 24, 7, 0),
                SmartNotificationRules.nextAllowedTime(lateNight, 22 * 60, 7 * 60, utc));
        long earlyMorning = at(2026, 11, 23, 6, 30);
        assertEquals(at(2026, 11, 23, 7, 0),
                SmartNotificationRules.nextAllowedTime(earlyMorning, 22 * 60, 7 * 60, utc));
    }
}
