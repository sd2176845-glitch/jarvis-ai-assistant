package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.widget.Toast;

/** Sends standard media keys to Android's active media session; does not inspect other apps. */
public final class MediaControlStudio {
    private MediaControlStudio() { }

    public static void show(Activity activity) {
        String[] controls = {"Play / Pause", "Next track", "Previous track", "Stop"};
        new AlertDialog.Builder(activity).setTitle("JARVIS • Media Controls")
                .setMessage("These are standard Android media-key requests. The active player may ignore them if it has no media session. JARVIS does not read notifications or media-app data.")
                .setItems(controls, (dialog, which) -> {
                    int key = which == 0 ? KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                            : which == 1 ? KeyEvent.KEYCODE_MEDIA_NEXT
                            : which == 2 ? KeyEvent.KEYCODE_MEDIA_PREVIOUS
                            : KeyEvent.KEYCODE_MEDIA_STOP;
                    AudioManager manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                    if (manager == null) {
                        Toast.makeText(activity, "Android media controls are unavailable.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    long now = System.currentTimeMillis();
                    manager.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, key, 0));
                    manager.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, key, 0));
                    Toast.makeText(activity, "Media control request sent. Check the active player.", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Close", null).show();
    }
}
