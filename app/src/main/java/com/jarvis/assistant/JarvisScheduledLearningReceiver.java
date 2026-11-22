package com.jarvis.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Performs a bounded refresh of explicitly approved RSS sources. */
public class JarvisScheduledLearningReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!JarvisScheduledLearning.isEnabled(context)) return;
        final PendingResult pending = goAsync();
        Thread worker = new Thread(() -> {
            try { JarvisScheduledLearning.refresh(context.getApplicationContext()); }
            finally { pending.finish(); }
        }, "jarvis-scheduled-learning");
        worker.start();
    }
}
