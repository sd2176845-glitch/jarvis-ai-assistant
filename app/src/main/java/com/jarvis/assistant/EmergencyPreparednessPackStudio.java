package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Owner-opened, fully local emergency guide with optional official-source browser handoff. */
public final class EmergencyPreparednessPackStudio {
    private EmergencyPreparednessPackStudio() { }

    public static void show(Activity activity) {
        String[] topics = EmergencyPreparednessPackRules.topics();
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Offline emergency pack")
                .setMessage("Choose a topic. The saved guide works without internet and does not use location. It contains no live warning or forecast; follow current official instructions.")
                .setItems(topics, (dialog, which) -> showTopic(activity, topics[which]))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void showTopic(Activity activity, String topic) {
        TextView text = new TextView(activity);
        text.setText(EmergencyPreparednessPackRules.buildPack(topic));
        text.setTextIsSelectable(true);
        text.setTextSize(15);
        text.setGravity(Gravity.START);
        text.setPadding(28, 20, 28, 20);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(text);

        new AlertDialog.Builder(activity)
                .setTitle("Emergency pack • " + topic)
                .setView(scroll)
                .setPositiveButton("Close", null)
                .setNeutralButton("NDMA / SACHET alerts", (dialog, which) ->
                        openOfficialPage(activity, EmergencyPreparednessPackRules.SACHET_URL))
                .setNegativeButton("IMD weather", (dialog, which) ->
                        openOfficialPage(activity, EmergencyPreparednessPackRules.IMD_URL))
                .show();
    }

    private static void openOfficialPage(Activity activity, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "No browser is available. The offline guide is still available.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
