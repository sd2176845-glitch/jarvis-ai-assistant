package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

/** Owner-selected PDF first-page OCR. Rendering and recognition stay on-device. */
public final class DocumentOcrStudio {
    public static final int REQUEST_PDF = 7315;
    private DocumentOcrStudio() { }

    public static void choosePdf(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("JARVIS • PDF text reader")
                .setMessage("Choose a PDF. JARVIS will OCR only the first page on this phone. The document and extracted text are not uploaded or saved.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Choose PDF", (d, w) -> {
                    Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pick.addCategory(Intent.CATEGORY_OPENABLE);
                    pick.setType("application/pdf");
                    pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        activity.startActivityForResult(pick, REQUEST_PDF);
                    } catch (Exception e) {
                        Toast.makeText(activity, "PDF picker is not available.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    public static boolean handleResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_PDF) return false;
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(activity, "PDF selection cancelled.", Toast.LENGTH_SHORT).show();
            return true;
        }
        ParcelFileDescriptor descriptor = null;
        PdfRenderer renderer = null;
        Bitmap bitmap = null;
        try {
            descriptor = activity.getContentResolver().openFileDescriptor(data.getData(), "r");
            if (descriptor == null) throw new IOException("PDF descriptor unavailable");
            renderer = new PdfRenderer(descriptor);
            if (renderer.getPageCount() == 0) throw new IOException("PDF has no pages");
            PdfRenderer.Page page = renderer.openPage(0);
            bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            canvas.drawColor(android.graphics.Color.WHITE);
            page.render(bitmap, null, new Matrix(), PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            renderer.close();
            descriptor.close();
            runOcr(activity, bitmap);
        } catch (Exception e) {
            if (renderer != null) try { renderer.close(); } catch (Exception ignored) { }
            if (descriptor != null) try { descriptor.close(); } catch (Exception ignored) { }
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            Toast.makeText(activity, "The selected PDF page could not be opened.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private static void runOcr(Activity activity, Bitmap bitmap) {
        com.google.mlkit.vision.text.TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener(result -> {
                    recognizer.close();
                    if (!bitmap.isRecycled()) bitmap.recycle();
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    String text = result.getText();
                    TextView preview = new TextView(activity);
                    preview.setText(text == null || text.trim().isEmpty()
                            ? "No readable Latin-script text was detected on the first page." : text);
                    preview.setTextIsSelectable(true);
                    preview.setTextSize(15);
                    preview.setPadding(28, 20, 28, 20);
                    new AlertDialog.Builder(activity).setTitle("PDF OCR • Page 1")
                            .setMessage("LOCAL RESULT • FIRST PAGE ONLY")
                            .setView(preview).setPositiveButton("Close", null).show();
                })
                .addOnFailureListener(error -> {
                    recognizer.close();
                    if (!bitmap.isRecycled()) bitmap.recycle();
                    Toast.makeText(activity, "Could not read text from this PDF page.", Toast.LENGTH_LONG).show();
                });
    }
}
