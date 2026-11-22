package com.jarvis.assistant;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Local-only notification priority counter.
 * Does not read, store, speak, or transmit notification title/body/person data.
 */
public class SmartNotificationService extends NotificationListenerService {
    private static final String PREFS = "jarvis_notification_intelligence";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        Notification n = sbn.getNotification();
        if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;

        String bucket = classify(n, sbn.getPackageName());
        android.content.SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);
        String key = bucket + "_count";
        long next = prefs.getLong(key, 0L) + 1L;
        prefs.edit().putLong(key, next)
                .putLong("last_seen_at", System.currentTimeMillis())
                .apply();
    }

    private String classify(Notification notification, String packageName) {
        String category = notification.category == null ? "" : notification.category;
        if (Notification.CATEGORY_CALL.equals(category)
                || Notification.CATEGORY_ALARM.equals(category)
                || Notification.CATEGORY_MESSAGE.equals(category)
                || Notification.CATEGORY_EVENT.equals(category)) {
            return "high";
        }

        if (Build.VERSION.SDK_INT >= 26 && notification.getChannelId() != null) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                android.app.NotificationChannel channel =
                        manager.getNotificationChannel(notification.getChannelId());
                if (channel != null) {
                    if (channel.getImportance() >= NotificationManager.IMPORTANCE_HIGH) return "high";
                    if (channel.getImportance() <= NotificationManager.IMPORTANCE_LOW) return "low";
                }
            }
        }

        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0
                || notification.priority <= Notification.PRIORITY_LOW) {
            return "low";
        }
        return "normal";
    }
}
