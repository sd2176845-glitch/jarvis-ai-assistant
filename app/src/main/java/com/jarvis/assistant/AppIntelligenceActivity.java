package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * M04 first implementation slice:
 * owner-visible catalogue of launcher apps, searchable app profiles,
 * version metadata, declared permissions and explicit app launch.
 */
public class AppIntelligenceActivity extends Activity {
    private final int background = Color.rgb(8, 5, 9);
    private final int accent = Color.rgb(255, 70, 82);
    private final List<AppEntry> apps = new ArrayList<>();
    private LinearLayout results;
    private TextView count;
    private EditText search;
    private SharedPreferences appPrefs;
    private String activeFilter = "ALL";
    private static final String[] CATEGORIES = {"Uncategorised", "Social", "Communication", "Productivity", "Media", "Games", "Tools", "Education"};

    private static class AppEntry {
        String label;
        String packageName;
        AppEntry(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        appPrefs = getSharedPreferences("m04_app_intelligence", MODE_PRIVATE);
        buildScreen();
        loadApps();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(background);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        scroll.addView(root);

        TextView heading = text("UNIVERSAL APP INTELLIGENCE", 18, Color.WHITE, true);
        heading.setGravity(Gravity.CENTER);
        root.addView(heading, new LinearLayout.LayoutParams(-1, -2));

        TextView note = text("M04 • On-device launcher app catalogue", 12,
                Color.rgb(190, 125, 135), false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(8), 0, dp(18));
        root.addView(note);

        LinearLayout filters = new LinearLayout(this);
        filters.setOrientation(LinearLayout.HORIZONTAL);
        filters.setGravity(Gravity.CENTER);
        String[] filterLabels = {"ALL APPS", "WATCHLIST", "CATEGORIES"};
        String[] filterValues = {"ALL", "WATCHLIST", "CATEGORIES"};
        for (int i = 0; i < filterLabels.length; i++) {
            final String value = filterValues[i];
            Button filterButton = new Button(this);
            filterButton.setText(filterLabels[i]);
            filterButton.setTextSize(10);
            filterButton.setAllCaps(false);
            filterButton.setTextColor(Color.WHITE);
            filterButton.setOnClickListener(v -> {
                if ("CATEGORIES".equals(value)) showCategoryFilter();
                else { activeFilter = value; renderApps(search == null ? "" : search.getText().toString()); }
            });
            filters.addView(filterButton, new LinearLayout.LayoutParams(0, dp(44), 1f));
        }
        root.addView(filters, new LinearLayout.LayoutParams(-1, -2));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(Color.GRAY);
        search.setHint("Search apps or package names");
        search.setPadding(dp(14), dp(10), dp(14), dp(10));
        search.setBackground(panel());
        root.addView(search, new LinearLayout.LayoutParams(-1, dp(50)));

        count = text("Scanning apps…", 12, accent, true);
        count.setPadding(dp(2), dp(14), 0, dp(10));
        root.addView(count);

        results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        root.addView(results, new LinearLayout.LayoutParams(-1, -2));

        TextView privacy = text(
                "Privacy: JARVIS lists apps exposed by Android's launcher visibility. " +
                "It does not read private in-app content. Permission details show declarations only, " +
                "not whether a permission is granted.", 11, Color.LTGRAY, false);
        privacy.setPadding(dp(4), dp(18), dp(4), dp(8));
        root.addView(privacy);

        setContentView(scroll);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderApps(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable e) {}
        });
    }

    private GradientDrawable panel() {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.rgb(25, 10, 15));
        shape.setCornerRadius(dp(12));
        shape.setStroke(dp(1), Color.rgb(115, 35, 48));
        return shape;
    }

    private void loadApps() {
        apps.clear();
        Intent launcher = new Intent(Intent.ACTION_MAIN);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getPackageManager().queryIntentActivities(launcher, 0);
        for (ResolveInfo info : found) {
            if (info.activityInfo == null) continue;
            String packageName = info.activityInfo.packageName;
            String label = String.valueOf(info.loadLabel(getPackageManager()));
            boolean duplicate = false;
            for (AppEntry entry : apps) {
                if (entry.packageName.equals(packageName)) { duplicate = true; break; }
            }
            if (!duplicate) apps.add(new AppEntry(label, packageName));
        }
        Collections.sort(apps, new Comparator<AppEntry>() {
            @Override public int compare(AppEntry a, AppEntry b) {
                return a.label.toLowerCase(Locale.ROOT).compareTo(b.label.toLowerCase(Locale.ROOT));
            }
        });
        for (AppEntry app : apps) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(app.packageName, 0);
                String versionName = info.versionName == null ? "Not provided" : info.versionName;
                long code = Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
                trackVersion(app.packageName, versionName, code);
            } catch (PackageManager.NameNotFoundException ignored) { }
        }
        renderApps("");
    }

    private void renderApps(String filter) {
        if (results == null) return;
        results.removeAllViews();
        String needle = filter.toLowerCase(Locale.ROOT).trim();
        int shown = 0;
        for (AppEntry app : apps) {
            if (!app.label.toLowerCase(Locale.ROOT).contains(needle)
                    && !app.packageName.toLowerCase(Locale.ROOT).contains(needle)) continue;
            if ("WATCHLIST".equals(activeFilter) && !isWatched(app.packageName)) continue;
            if ("CATEGORIES".equals(activeFilter)
                    && "Uncategorised".equals(getCategory(app.packageName))) continue;
            String category = getCategory(app.packageName);
            String suffix = isWatched(app.packageName) ? "  •  ★ WATCHLIST" : "";
            TextView item = text(app.label + "\n" + app.packageName
                    + "\n" + category + suffix, 13, Color.WHITE, true);
            item.setPadding(dp(14), dp(13), dp(14), dp(13));
            item.setBackground(panel());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(8);
            results.addView(item, params);
            item.setOnClickListener(v -> showAppProfile(app));
            shown++;
        }
        count.setText(shown + " visible app(s) • " + apps.size()
                + " catalogued • " + activeFilter);
        if (shown == 0) {
            TextView empty = text("No matching apps in this view.", 13, Color.LTGRAY, false);
            empty.setPadding(dp(8), dp(20), dp(8), dp(20));
            results.addView(empty);
        }
    }

    private void showAppProfile(AppEntry app) {
        String version = "Not available";
        long versionCode = -1L;
        String[] requested = null;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    app.packageName, PackageManager.GET_PERMISSIONS);
            version = info.versionName == null ? "Not provided" : info.versionName;
            if (Build.VERSION.SDK_INT >= 28) versionCode = info.getLongVersionCode();
            else versionCode = info.versionCode;
            requested = info.requestedPermissions;
            trackVersion(app.packageName, version, versionCode);
        } catch (PackageManager.NameNotFoundException ignored) { }

        StringBuilder message = new StringBuilder();
        message.append("App: ").append(app.label)
                .append("\nPackage: ").append(app.packageName)
                .append("\nVersion: ").append(version)
                .append("\n\nDeclared permissions (not grant status):");
        if (requested == null || requested.length == 0) {
            message.append("\nNo declared permissions returned by Android.");
        } else {
            int limit = Math.min(requested.length, 12);
            for (int i = 0; i < limit; i++) message.append("\n• ").append(requested[i]);
            if (requested.length > limit) message.append("\n…and ")
                    .append(requested.length - limit).append(" more.");
        }
        message.append("\nCategory: ").append(getCategory(app.packageName))
                .append("\nWatchlist: ").append(isWatched(app.packageName) ? "Yes" : "No");
        String versionChange = appPrefs.getString("change_" + app.packageName, null);
        if (versionChange != null) {
            message.append("\nLast detected version change: ").append(versionChange);
        }
        message.append("\n\nProfile source: Android package metadata on this device.");
        new AlertDialog.Builder(this)
                .setTitle("JARVIS • App profile")
                .setMessage(message.toString())
                .setNeutralButton(isWatched(app.packageName) ? "Remove watch" : "Add to watchlist",
                        (dialog, which) -> {
                            appPrefs.edit().putBoolean(watchKey(app.packageName),
                                    !isWatched(app.packageName)).apply();
                            renderApps(search.getText().toString());
                            Toast.makeText(this, isWatched(app.packageName)
                                    ? "Added to watchlist" : "Removed from watchlist",
                                    Toast.LENGTH_SHORT).show();
                        })
                .setPositiveButton("Open app", (dialog, which) -> {
                    Intent launch = getPackageManager().getLaunchIntentForPackage(app.packageName);
                    if (launch != null) {
                        try { startActivity(launch); }
                        catch (Exception e) {
                            new AlertDialog.Builder(this).setMessage("Android could not open this app.")
                                    .setPositiveButton("OK", null).show();
                        }
                    } else {
                        new AlertDialog.Builder(this).setMessage("No launch activity is available.")
                                .setPositiveButton("OK", null).show();
                    }
                })
                .setNegativeButton("More options", (dialog, which) -> showAppOptions(app))
                .show();
    }

    private String watchKey(String packageName) { return "watch_" + packageName; }
    private String categoryKey(String packageName) { return "category_" + packageName; }
    private boolean isWatched(String packageName) {
        return appPrefs.getBoolean(watchKey(packageName), false);
    }
    private String getCategory(String packageName) {
        return appPrefs.getString(categoryKey(packageName), "Uncategorised");
    }

    private void showAppOptions(AppEntry app) {
        new AlertDialog.Builder(this).setTitle("App organisation")
                .setItems(new String[]{"Set category", "Toggle watchlist"}, (dialog, which) -> {
                    if (which == 0) showCategoryPicker(app);
                    else {
                        boolean next = !isWatched(app.packageName);
                        appPrefs.edit().putBoolean(watchKey(app.packageName), next).apply();
                        renderApps(search.getText().toString());
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private void showCategoryPicker(AppEntry app) {
        new AlertDialog.Builder(this).setTitle("Category • " + app.label)
                .setItems(CATEGORIES, (dialog, which) -> {
                    appPrefs.edit().putString(categoryKey(app.packageName), CATEGORIES[which]).apply();
                    renderApps(search.getText().toString());
                    Toast.makeText(this, "Category saved", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancel", null).show();
    }

    private void showCategoryFilter() {
        String[] choices = {"All categories", "Social", "Communication", "Productivity",
                "Media", "Games", "Tools", "Education", "Uncategorised"};
        new AlertDialog.Builder(this).setTitle("Show category")
                .setItems(choices, (dialog, which) -> {
                    activeFilter = "CATEGORIES";
                    String selected = choices[which];
                    if (which == 0) activeFilter = "ALL";
                    else appPrefs.edit().putString("last_category_filter", selected).apply();
                    renderCategorySelection(selected);
                }).setNegativeButton("Cancel", null).show();
    }

    private void renderCategorySelection(String selected) {
        if ("All categories".equals(selected)) {
            renderApps(search.getText().toString());
            return;
        }
        results.removeAllViews();
        int shown = 0;
        String needle = search.getText().toString().toLowerCase(Locale.ROOT).trim();
        for (AppEntry app : apps) {
            if (!getCategory(app.packageName).equals(selected)) continue;
            if (!app.label.toLowerCase(Locale.ROOT).contains(needle)
                    && !app.packageName.toLowerCase(Locale.ROOT).contains(needle)) continue;
            TextView item = text(app.label + "\n" + app.packageName, 13, Color.WHITE, true);
            item.setPadding(dp(14), dp(13), dp(14), dp(13));
            item.setBackground(panel());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(8);
            results.addView(item, params);
            item.setOnClickListener(v -> showAppProfile(app));
            shown++;
        }
        count.setText(shown + " app(s) • " + selected);
    }

    private void trackVersion(String packageName, String versionName, long versionCode) {
        String key = "version_" + packageName;
        String current = versionName + "|" + versionCode;
        String previous = appPrefs.getString(key, null);
        if (previous != null && !previous.equals(current)) {
            appPrefs.edit().putString("change_" + packageName,
                    previous + " -> " + current).apply();
        }
        if (previous == null || !previous.equals(current)) {
            appPrefs.edit().putString(key, current)
                    .putLong("version_checked_" + packageName, System.currentTimeMillis())
                    .apply();
        }
    }
}
