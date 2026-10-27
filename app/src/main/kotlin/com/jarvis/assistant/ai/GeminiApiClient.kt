package com.jarvis.assistant.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiClient(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val MODEL_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

        private const val SYSTEM_PROMPT = """
You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), Tony Stark's personal AI assistant, operating on an Android mobile device.
Respond concisely, politely, and intelligently with calm confidence and subtle British wit.
Never write overly long essays unless explicitly asked.
When the user wants to perform an action, detect their intent and provide clear actionable guidance:
- Call someone: Specify [ACTION:CALL | name_or_number]
- Send WhatsApp: Specify [ACTION:WHATSAPP | name_or_number | message]
- Open an app (YouTube, Chrome, Camera, etc.): Specify [ACTION:OPEN_APP | app_name]
- Search Google: Specify [ACTION:SEARCH | search_term]
- Toggle Flashlight/Torch: Specify [ACTION:TORCH | ON/OFF]
- Check Battery: Specify [ACTION:BATTERY]
- Otherwise, answer with intelligent conversation.
"""
    }

    suspend fun generateJarvisResponse(userPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        try {
            val payload = JSONObject().apply {
                // System Instruction
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
                })

                // Contents
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                    })
                }
                put("contents", contentsArray)

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 512)
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "$MODEL_ENDPOINT?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string().orEmpty()
                    return@withContext Result.failure(
                        IOException("Gemini API Error (HTTP ${response.code}): $errBody")
                    )
                }

                val responseString = response.body?.string() ?: ""
                val json = JSONObject(responseString)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        return@withContext Result.success(text)
                    }
                }
                Result.success("At your service, sir. All systems running optimally.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
