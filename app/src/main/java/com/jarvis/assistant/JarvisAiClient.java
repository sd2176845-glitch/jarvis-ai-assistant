package com.jarvis.assistant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Calls the private-key-protected JARVIS backend. The Groq key must never be stored in the APK. */
public final class JarvisAiClient {
    private static final String ENDPOINT =
            "https://jarvis-ai-backend-bq4m.onrender.com/api/jarvis/chat";

    public interface Callback {
        void onReply(String reply);
    }

    private JarvisAiClient() {}

    public static void ask(Context context, String message, Callback callback) {
        Context appContext = context.getApplicationContext();
        Handler main = new Handler(Looper.getMainLooper());
        SharedPreferences memory = appContext.getSharedPreferences("jarvis_ai_memory", Context.MODE_PRIVATE);
        String originalMessage = message == null ? "" : message.trim();
        String prior = memory.getString("conversation", "");
        SharedPreferences appPreferences = appContext.getSharedPreferences("jarvis_preferences", Context.MODE_PRIVATE);
        String mode = appPreferences.getString("assistant_mode", "Normal");
        String modeGuidance;
        if ("Study".equalsIgnoreCase(mode)) modeGuidance = "Assistant mode: STUDY. Teach step-by-step, encourage understanding, and do not simply do a student's work without explanation.\n";
        else if ("Gaming".equalsIgnoreCase(mode)) modeGuidance = "Assistant mode: GAMING. Keep replies quick, concise, and focused on game settings or strategy.\n";
        else if ("Travel".equalsIgnoreCase(mode)) modeGuidance = "Assistant mode: TRAVEL. Help organize routes and plans; never pretend to know live traffic/weather unless verified.\n";
        else if ("Night".equalsIgnoreCase(mode)) modeGuidance = "Assistant mode: NIGHT. Use a calm, low-key tone and concise replies.\n";
        else if ("Caring".equalsIgnoreCase(mode)) modeGuidance = "Assistant mode: CARING. Be patient and supportive without claiming human feelings or assuming the user's mood.\n";
        else if ("Funny".equalsIgnoreCase(mode)) modeGuidance = "Assistant mode: FUNNY. Use gentle, situational humor when appropriate, not during serious topics.\n";
        else if ("Serious".equalsIgnoreCase(mode)) modeGuidance = "Assistant mode: SERIOUS. Be direct, focused, and factual.\n";
        else modeGuidance = "Assistant mode: NORMAL. Be helpful, respectful, and natural.\n";
        String conversationContext = prior.isEmpty() ? "" : "Recent conversation for context (use only when relevant):\n" + prior + "\n";
        String requestMessage = modeGuidance + conversationContext + "Current user message: " + originalMessage;
        Thread worker = new Thread(() -> {
            String answer;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(45000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");

                JSONObject body = new JSONObject();
                body.put("message", requestMessage);
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder response = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) response.append(line);
                    }
                }

                JSONObject json = new JSONObject(response.toString());
                if (status >= 200 && status < 300) {
                    answer = json.optString("reply", json.optString("text", ""));
                    if (answer.trim().isEmpty()) answer = "JARVIS ko khaali reply mila. Dobara bolo.";
                } else {
                    answer = "JARVIS AI server mein error aaya (" + status + "). Thodi der baad try karo.";
                }
            } catch (Exception e) {
                answer = "JARVIS AI se connect nahi ho paaya. Internet check karke dobara try karo.";
            } finally {
                if (connection != null) connection.disconnect();
            }

            final String result = answer;
            if (!result.startsWith("JARVIS AI server mein error") &&
                    !result.startsWith("JARVIS AI se connect nahi ho paaya")) {
                String old = memory.getString("conversation", "");
                String updated = (old.isEmpty() ? "" : old + "\n")
                        + "Boss: " + originalMessage + "\nJARVIS: " + result;
                String[] lines = updated.split("\n");
                int start = Math.max(0, lines.length - 12);
                StringBuilder compact = new StringBuilder();
                for (int i = start; i < lines.length; i++) {
                    if (compact.length() > 0) compact.append('\n');
                    compact.append(lines[i]);
                }
                memory.edit().putString("conversation", compact.toString()).apply();
            }
            main.post(() -> {
                if (callback != null) callback.onReply(result);
            });
        }, "jarvis-ai-request");
        worker.start();
    }
}
