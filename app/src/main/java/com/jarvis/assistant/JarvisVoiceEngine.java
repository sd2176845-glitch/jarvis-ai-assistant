package com.jarvis.assistant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Shared foreground/background TTS profile. A requested gender is only selected when the
 * installed engine exposes a voice whose name clearly identifies that gender. */
public final class JarvisVoiceEngine {
    private JarvisVoiceEngine() {}

    public static void apply(TextToSpeech tts, SharedPreferences prefs) {
        if (tts == null || prefs == null) return;
        String language = prefs.getString("voice_language", "en-IN");
        Locale locale = localeFor(language);
        int languageResult = tts.setLanguage(locale);
        float rate = clamp(prefs.getFloat("voice_rate", 0.92f), 0.65f, 1.35f);
        float pitch = clamp(prefs.getFloat("voice_pitch", 1.0f), 0.75f, 1.25f);
        tts.setSpeechRate(rate);
        tts.setPitch(pitch);
        if (languageResult < TextToSpeech.LANG_AVAILABLE) return;

        String gender = prefs.getString("voice_gender", "system");
        if ("system".equals(gender)) return;
        try {
            if (tts.getVoices() == null) return;
            for (Voice voice : tts.getVoices()) {
                if (voice == null || voice.isNetworkConnectionRequired()) continue;
                if (!locale.equals(voice.getLocale())) continue;
                String name = voice.getName().toLowerCase(Locale.ROOT);
                boolean match = "female".equals(gender)
                        ? name.contains("female") || name.contains("woman") || name.contains("feminine")
                        : (name.contains("male") && !name.contains("female"))
                                || (name.contains("man") && !name.contains("woman")) || name.contains("masculine");
                if (match) { tts.setVoice(voice); return; }
            }
        } catch (Exception ignored) {
            // Keep the engine's default voice if voice enumeration is unsupported.
        }
    }

    public static void showSettings(Context context, SharedPreferences prefs, TextToSpeech tts) {
        String[] genders = {"System default", "Male (if installed)", "Female (if installed)"};
        String current = prefs.getString("voice_gender", "system");
        int selected = "female".equals(current) ? 2 : ("male".equals(current) ? 1 : 0);
        LinearLayout panel = new LinearLayout(context);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(36, 12, 36, 8);
        TextView languageLabel = new TextView(context);
        languageLabel.setText("Speech language");
        panel.addView(languageLabel);
        String[] languages = {"English (India)", "Hindi (India)"};
        String language = prefs.getString("voice_language", "en-IN");
        int languageIndex = "hi-IN".equals(language) ? 1 : 0;
        android.widget.Spinner spinner = new android.widget.Spinner(context);
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(languageIndex);
        panel.addView(spinner);

        TextView rateLabel = new TextView(context);
        SeekBar rate = new SeekBar(context);
        rate.setMax(70);
        rate.setProgress(Math.round((prefs.getFloat("voice_rate", 0.92f) - 0.65f) * 100f));
        rateLabel.setText("Speaking speed");
        panel.addView(rateLabel);
        panel.addView(rate);
        TextView pitchLabel = new TextView(context);
        SeekBar pitch = new SeekBar(context);
        pitch.setMax(50);
        pitch.setProgress(Math.round((prefs.getFloat("voice_pitch", 1.0f) - 0.75f) * 100f));
        pitchLabel.setText("Voice pitch");
        panel.addView(pitchLabel);
        panel.addView(pitch);

        TextView genderLabel = new TextView(context);
        genderLabel.setText("Voice type");
        panel.addView(genderLabel);
        RadioGroup genderGroup = new RadioGroup(context);
        RadioButton system = new RadioButton(context); system.setText(genders[0]); system.setId(100);
        RadioButton male = new RadioButton(context); male.setText(genders[1]); male.setId(101);
        RadioButton female = new RadioButton(context); female.setText(genders[2]); female.setId(102);
        genderGroup.addView(system); genderGroup.addView(male); genderGroup.addView(female);
        genderGroup.check(selected == 2 ? 102 : (selected == 1 ? 101 : 100));
        panel.addView(genderGroup);
        new AlertDialog.Builder(context).setTitle("JARVIS • Voice settings")
                .setView(panel)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    int checked = genderGroup.getCheckedRadioButtonId();
                    String gender = checked == 102 ? "female" : (checked == 101 ? "male" : "system");
                    prefs.edit()
                            .putString("voice_gender", gender)
                            .putString("voice_language", spinner.getSelectedItemPosition() == 1 ? "hi-IN" : "en-IN")
                            .putFloat("voice_rate", 0.65f + rate.getProgress() / 100f)
                            .putFloat("voice_pitch", 0.75f + pitch.getProgress() / 100f)
                            .apply();
                    apply(tts, prefs);
                }).show();
    }

    private static Locale localeFor(String language) {
        return "hi-IN".equals(language) ? new Locale("hi", "IN") : new Locale("en", "IN");
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
