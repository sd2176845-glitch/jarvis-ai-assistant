package com.jarvis.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Delivers a user-created, one-time JARVIS reminder notification. */
public class ReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_TITLE = "reminder_title";
    public static final String EXTRA_ID = "reminder_id";
    private static final String CHANNEL_ID = "jarvis_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent == null ? null : intent.getStringExtra(EXTRA_TITLE);
        if (title == null || title.trim().isEmpty()) title = "Your scheduled reminder";
        int id = intent == null ? 2100 : intent.getIntExtra(EXTRA_ID, 2100);
        // A delivered one-time reminder is no longer pending restoration.
        ReminderStore.remove(context, id);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

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

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        Notification notification = builder
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("JARVIS reminder")
                .setContentText(title)
                .setStyle(new Notification.BigTextStyle().bigText(title))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();
        manager.notify(id, notification);
    }
}
