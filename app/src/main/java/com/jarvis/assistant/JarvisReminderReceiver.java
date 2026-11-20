package com.jarvis.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Displays a local notification when a user-created reminder is due. */
public class JarvisReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "jarvis_reminders";
    @Override public void onReceive(Context context, Intent intent) {
        String title = intent == null ? "" : intent.getStringExtra("title");
        if (title == null || title.trim().isEmpty()) title = "Your scheduled reminder";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL,
                    "JARVIS reminders", NotificationManager.IMPORTANCE_DEFAULT));
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL) : new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("JARVIS • Reminder")
                .setContentText(title)
                .setAutoCancel(true);
        try { manager.notify(intent.getIntExtra("reminder_id", (int) System.currentTimeMillis()), builder.build()); }
        catch (SecurityException ignored) { /* Notifications may be disabled by Android settings. */ }
    }
}
