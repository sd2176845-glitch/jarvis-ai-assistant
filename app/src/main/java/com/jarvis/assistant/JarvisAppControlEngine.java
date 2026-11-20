package com.jarvis.assistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Finds launchable apps by their visible label; it never reads app-private data. */
public final class JarvisAppControlEngine {
    public static final class AppEntry {
        public final String label;
        public final String packageName;
        AppEntry(String label, String packageName) { this.label = label; this.packageName = packageName; }
    }
    private JarvisAppControlEngine() {}

    public static List<AppEntry> search(Context context, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return Collections.emptyList();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = context.getPackageManager().queryIntentActivities(launcher, 0);
        List<AppEntry> matches = new ArrayList<>();
        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null) continue;
            String label;
            try { label = info.loadLabel(context.getPackageManager()).toString(); }
            catch (Exception e) { continue; }
            if (label.toLowerCase(Locale.ROOT).contains(q)) {
                String pkg = info.activityInfo.packageName;
                boolean duplicate = false;
                for (AppEntry entry : matches) if (entry.packageName.equals(pkg)) duplicate = true;
                if (!duplicate) matches.add(new AppEntry(label, pkg));
            }
        }
        Collections.sort(matches, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return matches;
    }
}
