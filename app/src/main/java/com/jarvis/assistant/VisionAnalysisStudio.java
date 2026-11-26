package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * M10 owner-invoked, one-image vision analysis.
 * Images are processed on-device; no gallery scanning, background camera, upload or persistence.
 */
public final class VisionAnalysisStudio {
    public static final int REQUEST_IMAGE = 7316;
    public static final int REQUEST_CAMERA = 7317;

    private VisionAnalysisStudio() { }

    public static void showOptions(Activity activity) {
        String[] actions = {
                "Analyze one selected image",
                "One-shot camera analysis"
        };
        new AlertDialog.Builder(activity).setTitle("JARVIS • Vision & spatial analysis")
                .setMessage("On-device scene labels and rough object positions. Analysis runs only after you choose an image or start a single camera capture. Results are estimates, not proof.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) chooseImage(activity);
                    else captureOneShot(activity);
                }).setNegativeButton("Close", null).show();
    }

    public static void chooseImage(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Select one image")
                .setMessage("Only the image you select is processed. It is not saved by JARVIS or sent to the cloud.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Choose image", (dialog, which) -> {
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

    public static void captureOneShot(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Start one-shot camera analysis?")
                .setMessage("This opens the camera app once. JARVIS analyzes only the returned preview thumbnail. No continuous camera session, background monitoring, image upload or image saving.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Open camera", (dialog, which) -> {
                    try {
                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        activity.startActivityForResult(camera, REQUEST_CAMERA);
                    } catch (Exception e) {
                        Toast.makeText(activity, "Camera app is not available.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    public static boolean handleResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_IMAGE && requestCode != REQUEST_CAMERA) return false;
        if (resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(activity, "Vision analysis cancelled.", Toast.LENGTH_SHORT).show();
            return true;
        }
        try {
            InputImage image;
            if (requestCode == REQUEST_IMAGE) {
                Uri uri = data.getData();
                if (uri == null) throw new IllegalArgumentException("No image selected");
                image = InputImage.fromFilePath(activity, uri);
            } else {
                Object value = data.getExtras() == null ? null : data.getExtras().get("data");
                if (!(value instanceof Bitmap)) throw new IllegalArgumentException("Camera preview unavailable");
                image = InputImage.fromBitmap((Bitmap) value, 0);
            }
            analyze(activity, image);
        } catch (Exception e) {
            Toast.makeText(activity, "Selected image could not be analyzed.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private static void analyze(Activity activity, InputImage image) {
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build();
        ObjectDetector detector = ObjectDetection.getClient(options);

        AtomicInteger pending = new AtomicInteger(2);
        AtomicReference<String> sceneResult = new AtomicReference<>("Scene labels unavailable.");
        AtomicReference<String> objectResult = new AtomicReference<>("Object positions unavailable.");
        Runnable finish = () -> {
            if (pending.decrementAndGet() == 0 && !activity.isFinishing() && !activity.isDestroyed()) {
                showResult(activity, sceneResult.get(), objectResult.get());
            }
        };

        Task<List<ImageLabel>> sceneTask = labeler.process(image);
        sceneTask.addOnSuccessListener(labels -> {
            StringBuilder text = new StringBuilder();
            int count = 0;
            for (ImageLabel label : labels) {
                float confidence = label.getConfidence();
                if (!VisionConfidenceRules.shouldDisplay(confidence)) continue;
                if (count++ >= 6) break;
                text.append("• ").append(label.getText()).append(" — ")
                        .append(VisionConfidenceRules.confidenceLabel(confidence))
                        .append(" (").append(Math.round(confidence * 100)).append("%)\n");
            }
            sceneResult.set(count == 0 ? "No scene label passed the conservative confidence threshold." : text.toString());
        }).addOnFailureListener(error -> sceneResult.set("Scene labels unavailable for this image."))
                .addOnCompleteListener(task -> {
                    labeler.close();
                    finish.run();
                });

        Task<List<DetectedObject>> objectTask = detector.process(image);
        objectTask.addOnSuccessListener(objects -> {
            StringBuilder text = new StringBuilder();
            int count = 0;
            for (DetectedObject object : objects) {
                if (count++ >= 8) break;
                Rect box = object.getBoundingBox();
                float centerX = image.getWidth() <= 0 ? 0.5f : box.centerX() / (float) image.getWidth();
                float centerY = image.getHeight() <= 0 ? 0.5f : box.centerY() / (float) image.getHeight();
                String name = "Unclassified object";
                String confidenceText = "Low confidence • uncertain";
                for (DetectedObject.Label label : object.getLabels()) {
                    if (VisionConfidenceRules.shouldDisplay(label.getConfidence())) {
                        name = label.getText();
                        confidenceText = VisionConfidenceRules.confidenceLabel(label.getConfidence())
                                + " (" + Math.round(label.getConfidence() * 100) + "%)";
                        break;
                    }
                }
                text.append("• ").append(name).append(" — ")
                        .append(VisionConfidenceRules.spatialRegion(centerX, centerY))
                        .append(" — ").append(confidenceText).append("\n");
            }
            objectResult.set(count == 0 ? "No distinct object boxes were detected." : text.toString());
        }).addOnFailureListener(error -> objectResult.set("Object positions unavailable for this image."))
                .addOnCompleteListener(task -> {
                    detector.close();
                    finish.run();
                });
    }

    private static void showResult(Activity activity, String scenes, String objects) {
        TextView result = new TextView(activity);
        result.setText("SCENE / IMAGE LABELS\n" + scenes
                + "\nOBJECTS & APPROXIMATE IMAGE REGIONS\n" + objects
                + "\nMODEL LIMITATIONS\nThese are machine estimates from one image. Position means a rough region in the image, not real-world distance or coordinates. Occlusion, blur, lighting and unusual angles can reduce accuracy. Verify important observations yourself.");
        result.setTextIsSelectable(true);
        result.setTextSize(14);
        result.setPadding(28, 20, 28, 20);
        new AlertDialog.Builder(activity).setTitle("JARVIS • Vision result")
                .setView(result)
                .setPositiveButton("Close", null).show();
    }
}
