package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Read-only installed-app knowledge; never opens or inspects another app's private data. */
public final class AppIntelligenceStudio {
    private AppIntelligenceStudio() {}

    private static final class AppEntry {
        final String label;
        final String packageName;
        AppEntry(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
    }

    public static void show(Activity activity) {
        String[] options = {
                "Installed App Catalogue",
                "App Details & Update Information",
                "Permission Transparency • Requested only"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • App Intelligence Studio")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showCatalogue(activity);
                    else if (which == 1) chooseApp(activity, "App details", false);
                    else chooseApp(activity, "Permission transparency", true);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static List<AppEntry> getVisibleApps(Activity activity) {
        Intent launcher = new Intent(Intent.ACTION_MAIN);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> matches = pm.queryIntentActivities(launcher, 0);
        Map<String, AppEntry> unique = new LinkedHashMap<>();
        for (ResolveInfo info : matches) {
            if (info.activityInfo == null) continue;
            String packageName = info.activityInfo.packageName;
            CharSequence label = info.loadLabel(pm);
            if (packageName == null || packageName.trim().isEmpty()) continue;
            String name = label == null ? packageName : label.toString();
            if (!unique.containsKey(packageName)) unique.put(packageName, new AppEntry(name, packageName));
        }
        ArrayList<AppEntry> apps = new ArrayList<>(unique.values());
        Collections.sort(apps, Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
        return apps;
    }

    private static void showCatalogue(Activity activity) {
        List<AppEntry> apps = getVisibleApps(activity);
        if (apps.isEmpty()) {
            new AlertDialog.Builder(activity).setTitle("Installed App Catalogue")
                    .setMessage("Android did not return any visible launcher apps. Package visibility may be restricted by the device.")
                    .setPositiveButton("Close", null).show();
            return;
        }
        String[] labels = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) {
            labels[i] = apps.get(i).label + "\n" + apps.get(i).packageName;
        }
        new AlertDialog.Builder(activity)
                .setTitle("Visible installed apps • " + apps.size())
                .setItems(labels, (d, which) -> showAppDetails(activity, apps.get(which)))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void chooseApp(Activity activity, String title, boolean permissions) {
        List<AppEntry> apps = getVisibleApps(activity);
        if (apps.isEmpty()) {
            Toast.makeText(activity, "No visible launcher apps were returned by Android.", Toast.LENGTH_LONG).show();
            return;
        }
        String[] labels = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) labels[i] = apps.get(i).label;
        new AlertDialog.Builder(activity)
                .setTitle(title + " • Select app")
                .setItems(labels, (d, which) -> {
                    if (permissions) showRequestedPermissions(activity, apps.get(which));
                    else showAppDetails(activity, apps.get(which));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static PackageInfo readPackage(Activity activity, String packageName) throws PackageManager.NameNotFoundException {
        return activity.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
    }

    private static void showAppDetails(Activity activity, AppEntry app) {
        try {
            PackageInfo info = readPackage(activity, app.packageName);
            String version = info.versionName == null ? "Not supplied" : info.versionName;
            long versionCode = Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
            String firstInstall = DateFormat.getDateTimeInstance().format(new Date(info.firstInstallTime));
            String lastUpdate = DateFormat.getDateTimeInstance().format(new Date(info.lastUpdateTime));
            String details = "APP\n" + app.label
                    + "\n\nPACKAGE\n" + app.packageName
                    + "\n\nVERSION\n" + version + " (code " + versionCode + ")"
                    + "\n\nTARGET ANDROID API\n" + info.applicationInfo.targetSdkVersion
                    + "\n\nFIRST INSTALLED\n" + firstInstall
                    + "\n\nLAST UPDATED ON THIS DEVICE\n" + lastUpdate
                    + "\nStore update comparison: not checked. This is local Android package metadata."
                    + "\n\nJARVIS reads public package metadata only. It does not open the app, inspect private files, or change its settings.";
            new AlertDialog.Builder(activity).setTitle("App Details")
                    .setMessage(details).setPositiveButton("Close", null)
                    .setNeutralButton("Open app", (d, w) -> launchSelectedApp(activity, app.packageName))
                    .setNegativeButton("Store listing", (d, w) -> openStoreListing(activity, app.packageName))
                    .show();
        } catch (Exception e) {
            new AlertDialog.Builder(activity).setTitle("App Details unavailable")
                    .setMessage("Android did not provide package metadata for " + app.label + ". No app data was changed.")
                    .setPositiveButton("Close", null).show();
        }
    }

    private static void launchSelectedApp(Activity activity, String packageName) {
        try {
            Intent launch = activity.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launch == null) {
                Toast.makeText(activity, "No launch activity is available for this app.", Toast.LENGTH_LONG).show();
                return;
            }
            activity.startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(activity, "This app could not be opened from JARVIS.", Toast.LENGTH_LONG).show();
        }
    }

    private static void openStoreListing(Activity activity, String packageName) {
        try {
            Intent market = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("market://details?id=" + android.net.Uri.encode(packageName)));
            activity.startActivity(market);
        } catch (Exception e) {
            try {
                Intent web = new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://play.google.com/store/apps/details?id="
                                + android.net.Uri.encode(packageName)));
                activity.startActivity(web);
            } catch (Exception ignored) {
                Toast.makeText(activity, "No compatible app store or browser was found.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void showRequestedPermissions(Activity activity, AppEntry app) {
        try {
            PackageInfo info = readPackage(activity, app.packageName);
            String[] permissions = info.requestedPermissions;
            StringBuilder report = new StringBuilder();
            report.append(app.label).append("\n").append(app.packageName).append("\n\n");
            if (permissions == null || permissions.length == 0) {
                report.append("No requested permissions were listed in the package manifest.");
            } else {
                report.append("REQUESTED PERMISSIONS (manifest declarations only)\n");
                for (String permission : permissions) {
                    report.append("• ").append(permission).append("\n");
                }
            }
            report.append("\nThis is not a granted-permission report. Android runtime grants and special access are not inferred here.");
            new AlertDialog.Builder(activity).setTitle("Permission Transparency")
                    .setMessage(report.toString()).setPositiveButton("Close", null).show();
        } catch (Exception e) {
            new AlertDialog.Builder(activity).setTitle("Permission details unavailable")
                    .setMessage("Android did not provide permission declarations for this app. No app data was changed.")
                    .setPositiveButton("Close", null).show();
        }
    }
}
