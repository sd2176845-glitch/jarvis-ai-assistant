package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/** Owner-invoked language and text-learning tools. Only the text entered in this studio is sent. */
public final class LanguageStudio {
    private LanguageStudio() {}

    public static void show(Activity activity, TextView status, TextView transcript) {
        String[] tools = {
                "Translate text • Hindi / Bengali / English",
                "Text clarity • Summary or simple explanation",
                "Vocabulary builder • Meaning and examples"
        };
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Language & Learning Studio")
                .setItems(tools, (dialog, which) -> {
                    if (which == 0) chooseTranslationLanguage(activity, status, transcript);
                    else if (which == 1) showTextClarity(activity, status, transcript);
                    else showVocabulary(activity, status, transcript);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static void chooseTranslationLanguage(Activity activity, TextView status, TextView transcript) {
        String[] languages = {"Hindi", "Bengali", "English"};
        new AlertDialog.Builder(activity)
                .setTitle("Translate into")
                .setItems(languages, (dialog, which) ->
                        showInput(activity, status, transcript, "Translation • " + languages[which],
                                "Enter or paste the text you want translated", text ->
                                runPrompt(activity, status, transcript,
                                        "Translate the following user-provided text into " + languages[which]
                                                + ". Preserve meaning and tone. Return only the translation.\n\nTEXT:\n" + text,
                                        "Translation ready • " + languages[which], languages[which])))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void showTextClarity(Activity activity, TextView status, TextView transcript) {
        String[] modes = {"Summarize key points", "Explain in simple language"};
        new AlertDialog.Builder(activity)
                .setTitle("Text clarity")
                .setItems(modes, (dialog, which) ->
                        showInput(activity, status, transcript, modes[which],
                                "Paste only the text you want JARVIS to process", text -> {
                                    String instruction = which == 0
                                            ? "Summarize the following user-provided text in concise bullet points. Keep important facts and uncertainty; do not invent details."
                                            : "Explain the following user-provided text in simple, clear language. Define difficult terms and distinguish facts from assumptions.";
                                    runPrompt(activity, status, transcript,
                                            instruction + "\n\nTEXT:\n" + text,
                                            which == 0 ? "Summary ready" : "Explanation ready", null);
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void showVocabulary(Activity activity, TextView status, TextView transcript) {
        showInput(activity, status, transcript, "Vocabulary builder",
                "Enter one word or short phrase", text ->
                        runPrompt(activity, status, transcript,
                                "Create a short vocabulary learning card for this word or phrase: " + text
                                        + ". Give a simple meaning, one natural example sentence, and (if clear) a pronunciation guide. If it has multiple meanings, mention the common ones. Do not invent an etymology.",
                                "Vocabulary card ready", null));
    }

    private interface TextAction { void run(String text); }

    private static void showInput(Activity activity, TextView status, TextView transcript,
                                  String title, String hint, TextAction action) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setMinLines(3);
        input.setMaxLines(7);
        input.setGravity(android.view.Gravity.TOP);
        input.setPadding(24, 18, 24, 18);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage("Your entered text will be sent to the JARVIS AI backend for processing. This studio does not attach saved conversation history or store this text as a memory.")
                .setView(layout)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Please enter some text first.", Toast.LENGTH_SHORT).show();
                    } else if (value.length() > 5000) {
                        Toast.makeText(activity, "Please keep the text under 5,000 characters.", Toast.LENGTH_LONG).show();
                    } else {
                        action.run(value);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void runPrompt(Activity activity, TextView status, TextView transcript,
                                  String prompt, String successLabel, String language) {
        status.setText("LANGUAGE STUDIO • PROCESSING");
        transcript.setText("Processing your selected text…");
        JarvisAiClient.askStandalone(activity, prompt, reply -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            transcript.setText(reply);
            status.setText(successLabel.toUpperCase(Locale.ROOT));
            AlertDialog.Builder result = new AlertDialog.Builder(activity)
                    .setTitle("JARVIS • " + successLabel)
                    .setMessage(reply)
                    .setPositiveButton("Done", null);
            if (language != null) {
                result.setNeutralButton("Speak", (d, w) -> speak(activity, reply, language));
            }
            result.show();
        });
    }

    private static void speak(Activity activity, String text, String language) {
        TextToSpeech speech = new TextToSpeech(activity, result -> {
            if (result != TextToSpeech.SUCCESS) {
                Toast.makeText(activity, "Speech output is unavailable on this device.", Toast.LENGTH_SHORT).show();
                return;
            }
            Locale locale = "Hindi".equals(language) ? new Locale("hi", "IN")
                    : ("Bengali".equals(language) ? new Locale("bn", "IN") : Locale.ENGLISH);
            int languageResult = speech.setLanguage(locale);
            if (languageResult == TextToSpeech.LANG_MISSING_DATA
                    || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(activity, "This language voice is not installed. Install it in Android Text-to-Speech settings.", Toast.LENGTH_LONG).show();
                speech.shutdown();
                return;
            }
            speech.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) {}
                @Override public void onDone(String utteranceId) { speech.shutdown(); }
                @Override public void onError(String utteranceId) { speech.shutdown(); }
            });
            speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "language-studio-translation");
        });
    }
}
