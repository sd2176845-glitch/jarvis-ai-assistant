package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public final class CwcFloodForecastStudio {
    private CwcFloodForecastStudio() { }

    public static void show(Activity activity) {
        String[] actions = {
                "CWC station-specific flood forecasts",
                "CWC 7-day basin advisory",
                "Interpret levels from a CWC station"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • CWC Flood Forecast Companion")
                .setMessage("Use the official Central Water Commission sources for current station forecasts and basin advisories. JARVIS does not scrape an undocumented feed or invent live river levels.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) open(activity, CwcFloodForecastRules.FORECAST_URL);
                    else if (which == 1) open(activity, CwcFloodForecastRules.SEVEN_DAY_URL);
                    else showLevelInterpreter(activity);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static void showLevelInterpreter(Activity activity) {
        EditText station = field(activity, "CWC station name", false);
        EditText current = field(activity, "Current water level", true);
        EditText warning = field(activity, "Warning Level", true);
        EditText danger = field(activity, "Danger Level", true);
        EditText hfl = field(activity, "Highest Flood Level (HFL)", true);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 8, 24, 8);
        layout.addView(station);
        layout.addView(current);
        layout.addView(warning);
        layout.addView(danger);
        layout.addView(hfl);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Interpret CWC station levels")
                .setMessage("Copy the current level and Warning / Danger / HFL values from the same official CWC station record. Enter all values in exactly the same unit. These values are not fetched by JARVIS.")
                .setView(layout)
                .setPositiveButton("Interpret", null)
                .setNeutralButton("Open CWC source", (d, w) ->
                        open(activity, CwcFloodForecastRules.FORECAST_URL))
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                    String name = station.getText().toString().trim();
                    String now = current.getText().toString().trim();
                    String warn = warning.getText().toString().trim();
                    String dangerValue = danger.getText().toString().trim();
                    String highest = hfl.getText().toString().trim();
                    String error = CwcFloodForecastRules.validateLevels(
                            name, now, warn, dangerValue, highest);
                    if (error != null) {
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    dialog.dismiss();
                    new AlertDialog.Builder(activity)
                            .setTitle("CWC station interpretation")
                            .setMessage(CwcFloodForecastRules.buildResult(
                                    name, now, warn, dangerValue, highest))
                            .setPositiveButton("Done", null)
                            .setNeutralButton("Open CWC source", (d, w) ->
                                    open(activity, CwcFloodForecastRules.FORECAST_URL))
                            .show();
                }));
        dialog.show();
    }

    private static EditText field(Activity activity, String hint, boolean decimal) {
        EditText input = new EditText(activity);
        input.setSingleLine(true);
        input.setHint(hint);
        input.setInputType(decimal
                ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                : InputType.TYPE_CLASS_TEXT);
        return input;
    }

    private static void open(Activity activity, String url) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(activity, "No browser is available to open the official CWC source.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
