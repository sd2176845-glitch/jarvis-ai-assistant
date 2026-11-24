package com.jarvis.assistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import java.util.Map;

/** Persists only reminders explicitly created by the owner for reboot recovery. */
public final class ReminderStore {
    private static final String PREFS = "jarvis_scheduled_reminders_v1";

    private ReminderStore() { }

    public static boolean save(Context context, int id, String title, long triggerAtMillis) {
        String safeTitle = ReminderRules.normalizeTitle(title);
        if (id <= 0 || safeTitle.isEmpty() || triggerAtMillis <= 0L) return false;
        try {
            JSONObject record = new JSONObject();
            record.put("id", id);
            record.put("title", safeTitle);
            record.put("triggerAt", triggerAtMillis);
            return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(String.valueOf(id), record.toString()).commit();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean updateTrigger(Context context, int id, long triggerAtMillis) {
        if (id <= 0 || triggerAtMillis <= 0L) return false;
        try {
            android.content.SharedPreferences prefs =
                    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String raw = prefs.getString(String.valueOf(id), null);
            if (raw == null) return false;
            JSONObject record = new JSONObject(raw);
            record.put("triggerAt", triggerAtMillis);
            return prefs.edit().putString(String.valueOf(id), record.toString()).commit();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean schedule(Context context, int id, String title, long triggerAtMillis) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null || id <= 0 || ReminderRules.normalizeTitle(title).isEmpty()) return false;
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, ReminderRules.normalizeTitle(title));
        intent.putExtra(ReminderReceiver.EXTRA_ID, id);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void remove(Context context, int id) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(String.valueOf(id)).apply();
    }

    public static void restorePending(Context context) {
        Map<String, ?> records = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getAll();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;

        long now = System.currentTimeMillis();
        for (Map.Entry<String, ?> entry : records.entrySet()) {
            if (!(entry.getValue() instanceof String)) continue;
            try {
                JSONObject record = new JSONObject((String) entry.getValue());
                int id = record.getInt("id");
                String title = ReminderRules.normalizeTitle(record.getString("title"));
                long triggerAt = record.getLong("triggerAt");
                if (id <= 0 || title.isEmpty() || !ReminderRules.shouldRestore(triggerAt, now)) {
                    remove(context, id);
                    continue;
                }
                Intent intent = new Intent(context, ReminderReceiver.class);
                intent.putExtra(ReminderReceiver.EXTRA_TITLE, title);
                intent.putExtra(ReminderReceiver.EXTRA_ID, id);
                PendingIntent pending = PendingIntent.getBroadcast(
                        context, id, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
            } catch (Exception invalidRecord) {
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().remove(entry.getKey()).apply();
            }
        }
    }
}
