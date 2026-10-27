package com.jarvis.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.jarvis.assistant.ai.GeminiApiClient
import com.jarvis.assistant.actions.SystemActionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

class JarvisVoiceService : Service(), TextToSpeech.OnInitListener {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var textToSpeech: TextToSpeech? = null
    private lateinit var actionController: SystemActionController
    private lateinit var geminiClient: GeminiApiClient

    companion object {
        const val CHANNEL_ID = "jarvis_service_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_SPEAK = "com.jarvis.assistant.ACTION_SPEAK"
        const val EXTRA_TEXT = "extra_speech_text"
    }

    override fun onCreate() {
        super.onCreate()
        actionController = SystemActionController(this)

        val prefs = getSharedPreferences("jarvis_preferences", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("gemini_api_key", "").orEmpty()
        geminiClient = GeminiApiClient(apiKey)

        textToSpeech = TextToSpeech(this, this)
        startForegroundServiceNotification()
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active voice listener and AI assistant monitor"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JARVIS AI Active")
            .setContentText("Monitoring voice commands & audio stream")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_SPEAK) {
            val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
            speak(text)
        }
        return START_STICKY
    }

    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_TTS_${System.currentTimeMillis()}")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.ENGLISH
            textToSpeech?.setPitch(0.95f)
            textToSpeech?.setSpeechRate(0.92f)
            speak("Systems initialized and online, sir.")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        serviceScope.cancel()
    }
}
