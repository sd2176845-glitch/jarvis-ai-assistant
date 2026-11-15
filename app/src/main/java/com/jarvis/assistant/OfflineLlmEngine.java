package com.jarvis.assistant;

import android.content.Context;
import android.content.res.AssetManager;

import com.tensai.llamakt.ChatMessage;
import com.tensai.llamakt.LlamaEngine;
import com.tensai.llamakt.SamplingParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Local text-generation fallback for JARVIS.
 *
 * The model is bundled as an uncompressed APK asset and copied once into app-private
 * storage because llama.cpp loads GGUF weights from a filesystem path.
 * Inference runs only on the calling worker thread; callers must not invoke this on
 * the Android main thread.
 */
public final class OfflineLlmEngine {
    private static final String MODEL_ASSET = "jarvis-offline-model.gguf";
    private static final String MODEL_FILE = "jarvis-offline-model.gguf";
    private static final long MIN_MODEL_BYTES = 250_000_000L;
    private static final int CONTEXT_TOKENS = 1024;
    private static final int OUTPUT_TOKENS = 120;

    private static final Object LOCK = new Object();
    private static LlamaEngine engine;
    private static String loadedModelPath;

    private OfflineLlmEngine() {}

    public static String generate(Context context, String userPrompt) throws Exception {
        if (context == null) throw new IllegalArgumentException("Context is required");
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-empty prompt is required");
        }

        synchronized (LOCK) {
            ensureLoaded(context.getApplicationContext());

            String systemPrompt =
                    "You are JARVIS, a concise Hindi and Hinglish phone assistant. "
                    + "Reply naturally in the language used by the user. "
                    + "You may answer questions and explain things, but you cannot operate "
                    + "the phone from this text-generation module. Never claim that you "
                    + "called someone, opened an app, changed a setting, or completed an "
                    + "action. Keep answers short and clear.";

            List<ChatMessage> messages = Arrays.asList(
                    new ChatMessage("system", systemPrompt, Collections.emptyList(), null, ""),
                    new ChatMessage("user", userPrompt.trim(), Collections.emptyList(), null, "")
            );

            String formattedPrompt = engine.formatChat(messages, false);
            engine.resetInterrupt();

            StringBuilder output = new StringBuilder();
            SamplingParams sampling = new SamplingParams(
                    OUTPUT_TOKENS,
                    0.55f,
                    40,
                    0.90f,
                    0.05f,
                    Collections.emptyList(),
                    -1,
                    1.08f,
                    64,
                    false
            );

            int generated = engine.completion(
                    formattedPrompt,
                    sampling,
                    token -> output.append(token),
                    null
            );

            String reply = output.toString().trim();
            if (generated <= 0 || reply.isEmpty()) {
                throw new IOException("The offline model did not generate a reply");
            }
            return reply;
        }
    }

    private static void ensureLoaded(Context context) throws Exception {
        File model = new File(context.getFilesDir(), MODEL_FILE);
        if (!model.isFile() || model.length() < MIN_MODEL_BYTES) {
            copyModelAsset(context.getAssets(), model);
        }

        String path = model.getAbsolutePath();
        if (engine != null && path.equals(loadedModelPath)) return;

        if (engine != null) {
            engine.free();
            engine = null;
        }

        LlamaEngine candidate = new LlamaEngine();
        try {
            // CPU-only is the safe baseline for the target MediaTek/Mali phone.
            candidate.load(
                    path,
                    0,
                    CONTEXT_TOKENS,
                    2,
                    null,
                    null,
                    null,
                    null,
                    2,
                    128,
                    128,
                    null,
                    null
            );
            engine = candidate;
            loadedModelPath = path;
        } catch (Exception error) {
            candidate.free();
            throw error;
        }
    }

    private static void copyModelAsset(AssetManager assets, File destination) throws IOException {
        File temporary = new File(destination.getParentFile(), destination.getName() + ".part");
        if (temporary.exists() && !temporary.delete()) {
            throw new IOException("Could not clear the previous partial model copy");
        }

        long copied = 0L;
        try (InputStream input = assets.open(MODEL_ASSET);
             FileOutputStream output = new FileOutputStream(temporary)) {
            byte[] buffer = new byte[1024 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                copied += count;
            }
            output.getFD().sync();
        } catch (IOException error) {
            temporary.delete();
            throw error;
        }

        if (copied < MIN_MODEL_BYTES) {
            temporary.delete();
            throw new IOException("Bundled offline model is incomplete");
        }
        if (destination.exists() && !destination.delete()) {
            temporary.delete();
            throw new IOException("Could not replace the old offline model");
        }
        if (!temporary.renameTo(destination)) {
            temporary.delete();
            throw new IOException("Could not finalize the offline model copy");
        }
    }
}
