package com.jarvis.assistant;

import java.util.Calendar;
import java.util.TimeZone;

/** Quiet-hour rules for JARVIS-owned reminders only. */
public final class SmartNotificationRules {
    private SmartNotificationRules() { }

    public static boolean isValidWindow(int startMinute, int endMinute) {
        return startMinute >= 0 && startMinute < 1440
                && endMinute >= 0 && endMinute < 1440
                && startMinute != endMinute;
    }

    public static boolean isQuietHours(long nowMillis, int startMinute, int endMinute, TimeZone zone) {
        if (!isValidWindow(startMinute, endMinute) || zone == null) return false;
        Calendar now = Calendar.getInstance(zone);
        now.setTimeInMillis(nowMillis);
        int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        if (startMinute < endMinute) return minute >= startMinute && minute < endMinute;
        return minute >= startMinute || minute < endMinute;
    }

    public static long nextAllowedTime(long nowMillis, int startMinute, int endMinute, TimeZone zone) {
        if (!isQuietHours(nowMillis, startMinute, endMinute, zone)) return nowMillis;
        Calendar next = Calendar.getInstance(zone);
        next.setTimeInMillis(nowMillis);
        if (startMinute > endMinute) {
            int minute = next.get(Calendar.HOUR_OF_DAY) * 60 + next.get(Calendar.MINUTE);
            if (minute >= startMinute) next.add(Calendar.DAY_OF_MONTH, 1);
        }
        next.set(Calendar.HOUR_OF_DAY, endMinute / 60);
        next.set(Calendar.MINUTE, endMinute % 60);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= nowMillis) next.add(Calendar.DAY_OF_MONTH, 1);
        return next.getTimeInMillis();
    }
}
