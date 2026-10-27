package com.jarvis.assistant.ai

import com.jarvis.assistant.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Calls the JARVIS backend; Gemini credentials must remain server-side. */
class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun generateJarvisResponse(userPrompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            if (BuildConfig.JARVIS_API_BASE_URL.contains("YOUR_SERVER_DOMAIN")) {
                return@withContext Result.failure(IllegalStateException("JARVIS server is not configured yet."))
            }
            try {
                val payload = JSONObject().put("message", userPrompt)
                val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(BuildConfig.JARVIS_API_BASE_URL)
                    .post(body).header("Accept", "application/json").build()
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(IOException("JARVIS server error (HTTP ${response.code})."))
                    }
                    val text = JSONObject(responseBody).optString("text")
                    if (text.isBlank()) Result.failure(IOException("JARVIS server returned an empty reply."))
                    else Result.success(text)
                }
            } catch (e: Exception) { Result.failure(e) }
        }
}