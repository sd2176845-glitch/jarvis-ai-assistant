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


    /** Backs up the current profile before restoring safe TTS preference defaults. */
    public static boolean repairVoicePreferences(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("jarvis_preferences", Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor backup = context.getSharedPreferences("jarvis_repair_backup", Context.MODE_PRIVATE).edit();
        backup.putBoolean("has_backup", true)
                .putString("voice_language", prefs.getString("voice_language", "en-IN"))
                .putString("voice_gender", prefs.getString("voice_gender", "system"))
                .putFloat("voice_rate", prefs.getFloat("voice_rate", 0.92f))
                .putFloat("voice_pitch", prefs.getFloat("voice_pitch", 1.0f))
                .apply();
        String language = prefs.getString("voice_language", "en-IN");
        String gender = prefs.getString("voice_gender", "system");
        float rate = prefs.getFloat("voice_rate", 0.92f);
        float pitch = prefs.getFloat("voice_pitch", 1.0f);
        if (!"en-IN".equals(language) && !"hi-IN".equals(language)) language = "en-IN";
        if (!"system".equals(gender) && !"male".equals(gender) && !"female".equals(gender)) gender = "system";
        rate = Math.max(0.65f, Math.min(1.35f, rate));
        pitch = Math.max(0.75f, Math.min(1.25f, pitch));
        return prefs.edit().putString("voice_language", language).putString("voice_gender", gender)
                .putFloat("voice_rate", rate).putFloat("voice_pitch", pitch).commit();
    }

    /** Restores the last saved voice-profile backup if one exists. */
    public static boolean rollbackVoiceRepair(Context context) {
        android.content.SharedPreferences backup = context.getSharedPreferences("jarvis_repair_backup", Context.MODE_PRIVATE);
        if (!backup.getBoolean("has_backup", false)) return false;
        return context.getSharedPreferences("jarvis_preferences", Context.MODE_PRIVATE).edit()
                .putString("voice_language", backup.getString("voice_language", "en-IN"))
                .putString("voice_gender", backup.getString("voice_gender", "system"))
                .putFloat("voice_rate", backup.getFloat("voice_rate", 0.92f))
                .putFloat("voice_pitch", backup.getFloat("voice_pitch", 1.0f)).commit();
    }

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
