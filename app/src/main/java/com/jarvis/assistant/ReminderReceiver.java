package com.jarvis.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.TimeZone;

/** Delivers and snoozes local notifications for reminders entered by the owner. */
public class ReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_TITLE = "reminder_title";
    public static final String EXTRA_ID = "reminder_id";
    public static final String ACTION_SNOOZE = "com.jarvis.assistant.action.SNOOZE_REMINDER";
    private static final String CHANNEL_ID = "jarvis_owner_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        String title = intent == null ? "" : ReminderRules.normalizeTitle(
                intent.getStringExtra(EXTRA_TITLE));
        int id = intent == null ? 2100 : intent.getIntExtra(EXTRA_ID, 2100);
        if (title.isEmpty() || id <= 0) return;

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (ACTION_SNOOZE.equals(intent.getAction())) {
            long snoozeUntil = SmartNotificationRules.snoozeUntil(System.currentTimeMillis());
            if (ReminderStore.updateTrigger(context, id, snoozeUntil)
                    && ReminderStore.schedule(context, id, title, snoozeUntil)) {
                manager.cancel(id);
            }
            return;
        }

        android.content.SharedPreferences settings = context.getSharedPreferences(
                "jarvis_notification_intelligence", Context.MODE_PRIVATE);
        if (settings.getBoolean("quiet_hours_enabled", false)) {
            int start = settings.getInt("quiet_start_minute", 22 * 60);
            int end = settings.getInt("quiet_end_minute", 7 * 60);
            long now = System.currentTimeMillis();
            long nextAllowed = SmartNotificationRules.nextAllowedTime(
                    now, start, end, TimeZone.getDefault());
            if (nextAllowed > now
                    && ReminderStore.updateTrigger(context, id, nextAllowed)
                    && ReminderStore.schedule(context, id, title, nextAllowed)) {
                return;
            }
        }

        ReminderStore.remove(context, id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "JARVIS reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for reminders created inside JARVIS.");
            manager.createNotificationChannel(channel);
        }

        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, id, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra(EXTRA_TITLE, title);
        snoozeIntent.putExtra(EXTRA_ID, id);
        PendingIntent snoozePending = PendingIntent.getBroadcast(
                context, id + 1000000, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        Notification.Action snoozeAction = new Notification.Action.Builder(
                android.R.drawable.ic_lock_idle_alarm, "Snooze 10 min", snoozePending).build();
        Notification notification = builder
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("JARVIS reminder")
                .setContentText(title)
                .setStyle(new Notification.BigTextStyle().bigText(title))
                .setContentIntent(contentIntent)
                .addAction(snoozeAction)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();
        manager.notify(id, notification);
    }
}
