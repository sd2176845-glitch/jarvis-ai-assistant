package com.jarvis.assistant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

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

    public interface UnderstandCallback {
        void onUnderstood(String text);
    }

    /**
     * Corrects likely speech-recognition omissions and spelling in Hindi/Hinglish/English.
     * The backend returns text only; all device actions remain in MainActivity's local,
     * confirmation-aware command router.
     */
    public static void understandCommand(Context context, String rawTranscript, UnderstandCallback callback) {
        Context appContext = context.getApplicationContext();
        Handler main = new Handler(Looper.getMainLooper());
        SharedPreferences connectivityPreferences = appContext.getSharedPreferences(
                "jarvis_preferences", Context.MODE_PRIVATE);
        String connectivityMode = ConnectivityIntelligenceRules.normalizeAiMode(
                connectivityPreferences.getString("ai_connectivity_mode", "Hybrid"));
        if (!hasPotentialInternetRoute(appContext, connectivityMode)) {
            final String localTranscript = rawTranscript == null ? "" : rawTranscript.trim();
            main.post(() -> {
                if (callback != null) callback.onUnderstood(localTranscript);
            });
            return;
        }
        Thread worker = new Thread(() -> {
            HttpURLConnection connection = null;
            String understood = rawTranscript == null ? "" : rawTranscript.trim();
            try {
                connection = (HttpURLConnection) new URL(
                        "https://jarvis-ai-backend-bq4m.onrender.com/api/jarvis/understand").openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(12000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");
                JSONObject body = new JSONObject();
                body.put("transcript", understood);
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
                if (status >= 200 && status < 300) {
                    JSONObject json = new JSONObject(response.toString());
                    String candidate = json.optString("corrected_text", "").trim();
                    if (json.optBoolean("understood", false) && !candidate.isEmpty()
                            && candidate.length() <= 500) understood = candidate;
                }
            } catch (Exception ignored) {
                // Offline/server failure: pass the original speech transcript to local routing.
            } finally {
                if (connection != null) connection.disconnect();
            }
            final String result = understood;
            main.post(() -> {
                if (callback != null) callback.onUnderstood(result);
            });
        }, "jarvis-command-understanding");
        worker.start();
    }

    /**
     * Sends a single owner-entered prompt without attaching or saving conversation memory.
     * Used by focused tools that should process only the text the owner explicitly submits.
     */
    public static void askStandalone(Context context, String message, Callback callback) {
        Context appContext = context.getApplicationContext();
        Handler main = new Handler(Looper.getMainLooper());
        String prompt = message == null ? "" : message.trim();
        SharedPreferences connectivityPreferences = appContext.getSharedPreferences(
                "jarvis_preferences", Context.MODE_PRIVATE);
        String connectivityMode = ConnectivityIntelligenceRules.normalizeAiMode(
                connectivityPreferences.getString("ai_connectivity_mode", "Hybrid"));
        if (!ConnectivityIntelligenceRules.cloudAiAllowed(connectivityMode)) {
            main.post(() -> {
                if (callback != null) callback.onReply(ConnectivityIntelligenceRules.localOnlyResponse());
            });
            return;
        }
        if (!hasPotentialInternetRoute(appContext, connectivityMode)) {
            main.post(() -> {
                if (callback != null) callback.onReply(
                        ConnectivityIntelligenceRules.noInternetRouteResponse(connectivityMode));
            });
            return;
        }
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
                body.put("message", prompt);
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
                if (status >= 200 && status < 300) {
                    JSONObject json = new JSONObject(response.toString());
                    answer = json.optString("reply", json.optString("text", "")).trim();
                    if (answer.isEmpty()) answer = "JARVIS AI returned an empty response. Please try again.";
                } else {
                    answer = "Hybrid".equals(connectivityMode)
                            ? ConnectivityIntelligenceRules.hybridFallbackResponse()
                            : "JARVIS AI server returned an error (" + status + "). Please try again later.";
                }
            } catch (Exception e) {
                answer = "Hybrid".equals(connectivityMode)
                        ? ConnectivityIntelligenceRules.hybridFallbackResponse()
                        : "JARVIS AI could not connect. Check the internet connection and try again.";
            } finally {
                if (connection != null) connection.disconnect();
            }
            final String result = answer;
            main.post(() -> {
                if (callback != null) callback.onReply(result);
            });
        }, "jarvis-ai-standalone-request");
        worker.start();
    }

    /**
     * Avoids predictable cloud timeouts when Android confirms there is no internet-capable
     * default route. Unknown capability state fails open and still attempts the request.
     * A positive INTERNET capability is only a route hint, not proof that the provider works.
     */
    private static boolean hasPotentialInternetRoute(Context context, String mode) {
        if (!ConnectivityIntelligenceRules.cloudAiAllowed(mode)) return false;
        try {
            if (Build.VERSION.SDK_INT < 23) return true;
            ConnectivityManager manager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) return true;
            Network active = manager.getActiveNetwork();
            if (active == null) {
                return ConnectivityIntelligenceRules.shouldAttemptCloudAi(mode, false, false, false);
            }
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(active);
            boolean known = capabilities != null;
            boolean internet = known
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            return ConnectivityIntelligenceRules.shouldAttemptCloudAi(mode, true, known, internet);
        } catch (SecurityException e) {
            // Do not block a legitimate online request when Android does not disclose state.
            return true;
        } catch (Exception e) {
            // Capability state is unknown; let the existing bounded HTTP request decide.
            return true;
        }
    }

    private JarvisAiClient() {}

    public static void ask(Context context, String message, Callback callback) {
        Context appContext = context.getApplicationContext();
        Handler main = new Handler(Looper.getMainLooper());
        SharedPreferences memory = appContext.getSharedPreferences("jarvis_ai_memory", Context.MODE_PRIVATE);
        String originalMessage = message == null ? "" : message.trim();
        long nowMillis = System.currentTimeMillis();
        long contextUpdatedAt = memory.getLong("conversation_updated_at", 0L);
        long contextTtl = 2L * 60L * 60L * 1000L;
        boolean contextFresh = ConversationContextRules.isFresh(contextUpdatedAt, nowMillis, contextTtl);
        String prior = contextFresh ? ConversationContextRules.limitContext(memory.getString("conversation", "")) : "";
        if (!contextFresh && memory.contains("conversation")) {
            memory.edit().remove("conversation").remove("conversation_updated_at").apply();
        }
        SharedPreferences appPreferences = appContext.getSharedPreferences("jarvis_preferences", Context.MODE_PRIVATE);
        String connectivityMode = ConnectivityIntelligenceRules.normalizeAiMode(
                appPreferences.getString("ai_connectivity_mode", "Hybrid"));
        if (!ConnectivityIntelligenceRules.cloudAiAllowed(connectivityMode)) {
            main.post(() -> {
                if (callback != null) callback.onReply(ConnectivityIntelligenceRules.localOnlyResponse());
            });
            return;
        }
        if (!hasPotentialInternetRoute(appContext, connectivityMode)) {
            main.post(() -> {
                if (callback != null) callback.onReply(
                        ConnectivityIntelligenceRules.noInternetRouteResponse(connectivityMode));
            });
            return;
        }
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
        String requestMessage = ConversationContextRules.buildRequestMessage(modeGuidance, prior, originalMessage);
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
                    answer = "Hybrid".equals(connectivityMode)
                            ? ConnectivityIntelligenceRules.hybridFallbackResponse()
                            : "JARVIS AI server mein error aaya (" + status + "). Thodi der baad try karo.";
                }
            } catch (Exception e) {
                answer = "Hybrid".equals(connectivityMode)
                        ? ConnectivityIntelligenceRules.hybridFallbackResponse()
                        : "JARVIS AI se connect nahi ho paaya. Internet check karke dobara try karo.";
            } finally {
                if (connection != null) connection.disconnect();
            }

            final String result = answer;
            if (!result.equals(ConnectivityIntelligenceRules.hybridFallbackResponse())
                    && !result.startsWith("JARVIS AI server mein error")
                    && !result.startsWith("JARVIS AI se connect nahi ho paaya")) {
                synchronized (memory) {
                    String old = memory.getString("conversation", "");
                    String updated = ConversationContextRules.appendExchange(old, originalMessage, result);
                    memory.edit().putString("conversation", updated)
                            .putLong("conversation_updated_at", System.currentTimeMillis()).apply();
                }
            }
            main.post(() -> {
                if (callback != null) callback.onReply(result);
            });
        }, "jarvis-ai-request");
        worker.start();
    }
}
