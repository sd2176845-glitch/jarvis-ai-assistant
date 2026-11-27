package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * M01 owner-controlled feature consent. These choices are JARVIS app preferences;
 * they do not grant Android permissions and do not replace runtime permission checks.
 */
public final class OwnerConsentStudio {
    public static final int VOICE_PROCESSING = 0;
    public static final int CONTACT_LOOKUP = 1;
    public static final int CALL_EVENT_ANNOUNCEMENTS = 2;
    public static final int SMS_EVENT_ANNOUNCEMENTS = 3;
    public static final int LOCAL_DIAGNOSTICS = 4;
    private static final String[] KEYS = {
            "owner_consent_voice_processing_v1",
            "owner_consent_contacts_v1",
            "owner_consent_call_event_notices_v1",
            "owner_consent_sms_event_notices_v1",
            "owner_consent_diagnostics_v1"
    };
    private static final String[] LABELS = {
            "Voice processing",
            "Contact lookup",
            "Call event announcements",
            "SMS event announcements",
            "Local diagnostics"
    };
    private static final String[] EXPLANATIONS = {
            "Allows JARVIS voice features to process speech when you explicitly start listening. Android microphone permission is still required.",
            "Allows supported contact lookup after you grant Android Contacts permission. It does not authorize uploading your contacts.",
            "Allows JARVIS to announce supported call-state events. Android phone permissions and system restrictions still apply.",
            "Allows supported SMS event announcements only where Android/provider rules permit. It does not authorize reading OTPs or silently accessing messages.",
            "Allows local, owner-visible diagnostic summaries. It does not enable stealth monitoring or automatic uploads."
    };

    private OwnerConsentStudio() { }

    public static void show(Activity activity, SharedPreferences prefs) {
        boolean[] checked = new boolean[KEYS.length];
        for (int i = 0; i < KEYS.length; i++) checked[i] = prefs.getBoolean(KEYS[i], false);
        new AlertDialog.Builder(activity)
                .setTitle("M01 • Owner consent controls")
                .setMultiChoiceItems(LABELS, checked, (dialog, which, isChecked) -> {
                    prefs.edit().putBoolean(KEYS[which], isChecked)
                            .putLong(KEYS[which] + "_updated_at", System.currentTimeMillis()).apply();
                })
                .setPositiveButton("Done", null)
                .setNeutralButton("Explain choices", (dialog, which) -> showExplanations(activity))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void showExplanations(Activity activity) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < LABELS.length; i++) {
            text.append(LABELS[i]).append("\n")
                    .append(EXPLANATIONS[i]).append("\n\n");
        }
        TextView view = new TextView(activity);
        view.setText(text.toString());
        view.setTextSize(14);
        int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);
        view.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(activity).setTitle("What each choice means")
                .setView(view).setPositiveButton("Back", null).show();
    }

    public static boolean isAllowed(SharedPreferences prefs, int consentIndex) {
        if (prefs == null || consentIndex < 0 || consentIndex >= KEYS.length) return false;
        return prefs.getBoolean(KEYS[consentIndex], false);
    }

    public static String[] labels() { return LABELS.clone(); }
}
