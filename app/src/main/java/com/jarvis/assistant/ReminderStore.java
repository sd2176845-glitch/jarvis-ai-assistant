package com.jarvis.assistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import java.util.Map;

/** Stores only reminders explicitly created by the owner so Android alarms can be restored after reboot. */
public final class ReminderStore {
    private static final String PREFS = "jarvis_scheduled_reminders";

    private ReminderStore() { }

    public static void save(Context context, int id, String title, long triggerAtMillis) {
        try {
            JSONObject record = new JSONObject();
            record.put("id", id);
            record.put("title", title);
            record.put("triggerAt", triggerAtMillis);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(String.valueOf(id), record.toString()).commit();
        } catch (Exception ignored) { }
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
                String title = record.getString("title");
                long triggerAt = record.getLong("triggerAt");
                if (triggerAt <= now) {
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
