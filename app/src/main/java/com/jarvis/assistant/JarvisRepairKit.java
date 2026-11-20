package com.jarvis.assistant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.SpeechRecognizer;
import android.provider.Settings;

/** Read-only diagnostics and user-approved repair entry points for common Android restrictions. */
public final class JarvisRepairKit {
    private JarvisRepairKit() {}

    public static String diagnose(Context context, boolean ttsReady) {
        StringBuilder report = new StringBuilder("JARVIS • AUTO REPAIR DIAGNOSTICS\n\n");
        boolean mic = Build.VERSION.SDK_INT < 23 ||
                context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        report.append(mic ? "✓ Microphone permission available\n" : "⚠ Microphone permission missing\n");
        boolean speech = SpeechRecognizer.isRecognitionAvailable(context);
        report.append(speech ? "✓ Android speech recognition service available\n" : "⚠ No speech recognition service found\n");
        report.append(ttsReady ? "✓ Text-to-speech engine initialized\n" : "⚠ Text-to-speech engine not ready\n");
        if (Build.VERSION.SDK_INT >= 33) {
            boolean notifications = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            report.append(notifications ? "✓ Notifications permission available\n" : "⚠ Notifications permission missing\n");
        } else report.append("✓ Notification runtime permission not required on this Android version\n");
        String enabled = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean accessibility = enabled != null && enabled.contains(context.getPackageName());
        report.append(accessibility ? "✓ JARVIS accessibility service enabled\n"
                : "• Accessibility service is off (only needed for explicitly enabled call controls)\n");
        report.append("\nNo settings or permissions were changed automatically. Choose a repair action yourself.");
        return report.toString();
    }
}
