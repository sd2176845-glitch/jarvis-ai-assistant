package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;

/** Owner-selected image OCR. Processing stays on-device; extracted text is not saved or uploaded. */
public final class ImageOcrStudio {
    public static final int REQUEST_IMAGE = 7314;

    private static boolean useDevanagari;

    private ImageOcrStudio() { }

    public static void chooseImage(Activity activity) {
        String[] scripts = {"Latin script", "Devanagari script • Hindi"};
        new AlertDialog.Builder(activity).setTitle("Choose text script")
                .setMessage("OCR runs locally on this phone. Bengali and other scripts are not currently supported by this reader.")
                .setItems(scripts, (dialog, which) -> {
                    useDevanagari = which == 1;
                    openImagePicker(activity);
                }).setNegativeButton("Cancel", null).show();
    }

    private static void openImagePicker(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Image text reader")
                .setMessage("Choose one image. JARVIS will not upload or save the image or extracted text.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Choose image", (d, w) -> {
                    Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pick.addCategory(Intent.CATEGORY_OPENABLE);
                    pick.setType("image/*");
                    pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        activity.startActivityForResult(pick, REQUEST_IMAGE);
                    } catch (Exception e) {
                        Toast.makeText(activity, "Image picker is not available.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    public static boolean handleResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_IMAGE) return false;
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(activity, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
            return true;
        }
        Uri uri = data.getData();
        try {
            InputImage image = InputImage.fromFilePath(activity, uri);
            com.google.mlkit.vision.text.TextRecognizer recognizer = useDevanagari
                    ? TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build())
                    : TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        recognizer.close();
                        showPreview(activity, result);
                    })
                    .addOnFailureListener(error -> {
                        recognizer.close();
                        Toast.makeText(activity, "Could not read text from this image.", Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            Toast.makeText(activity, "Selected image could not be opened.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private static void showPreview(Activity activity, Text result) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        String text = result == null ? "" : result.getText();
        if (text == null || text.trim().isEmpty()) text = "No readable text was detected.";
        TextView preview = new TextView(activity);
        preview.setText(text);
        preview.setTextIsSelectable(true);
        preview.setTextSize(15);
        preview.setPadding(28, 20, 28, 20);
        final String extracted = text;
        new AlertDialog.Builder(activity).setTitle("JARVIS • OCR preview")
                .setMessage("LOCAL RESULT • REVIEW BEFORE USING")
                .setView(preview)
                .setPositiveButton("Close", null)
                .setNeutralButton("Explain with AI", (dialog, which) ->
                        new AlertDialog.Builder(activity).setTitle("Send extracted text to JARVIS AI?")
                                .setMessage("Only the text shown in this preview will be sent to the configured JARVIS AI backend. Review it for private information first. The image itself is not sent.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Send text", (d, w) -> {
                                    String prompt = OcrPromptRules.explanationPrompt(extracted);
                                    JarvisAiClient.askStandalone(activity, prompt, reply -> {
                                        if (activity.isFinishing() || activity.isDestroyed()) return;
                                        new AlertDialog.Builder(activity).setTitle("JARVIS • Text explanation")
                                                .setMessage(reply).setPositiveButton("Close", null).show();
                                    });
                                }).show()).show();
    }
}
