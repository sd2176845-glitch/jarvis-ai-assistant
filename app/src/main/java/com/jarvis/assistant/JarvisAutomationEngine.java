package com.jarvis.assistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/** User-approved, local reminder scheduling. No autonomous external actions are performed. */
public final class JarvisAutomationEngine {
    private JarvisAutomationEngine() {}

    public static boolean scheduleReminder(Context context, String title, int minutesFromNow) {
        if (context == null || title == null || title.trim().isEmpty()
                || minutesFromNow < 1 || minutesFromNow > 43200) return false;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return false;
        int id = (int) (System.currentTimeMillis() & 0x7fffffff);
        Intent intent = new Intent(context, JarvisReminderReceiver.class)
                .putExtra("title", title.trim()).putExtra("reminder_id", id);
        PendingIntent pending = PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long trigger = SystemClock.elapsedRealtime() + minutesFromNow * 60_000L;
        try {
            alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, pending);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
}
