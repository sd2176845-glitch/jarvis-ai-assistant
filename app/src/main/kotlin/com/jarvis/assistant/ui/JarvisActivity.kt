package com.jarvis.assistant.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.jarvis.assistant.actions.SystemActionController
import com.jarvis.assistant.ai.GeminiApiClient
import com.jarvis.assistant.model.ActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class JarvisActivity : Activity(), TextToSpeech.OnInitListener {

    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var statusView: TextView
    private lateinit var transcriptView: TextView
    private lateinit var arcReactorOrb: FrameLayout
    private lateinit var speechRecognizer: SpeechRecognizer
    private var textToSpeech: TextToSpeech? = null
    private lateinit var actionController: SystemActionController
    private lateinit var geminiClient: GeminiApiClient

    private val cyanColor = Color.rgb(70, 220, 255)
    private val darkBgColor = Color.rgb(3, 10, 22)
    private val cardBgColor = Color.rgb(10, 25, 45)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = darkBgColor
        window.navigationBarColor = darkBgColor

        actionController = SystemActionController(this)
        // Gemini API keys must stay on the server, never inside the Android app.
        geminiClient = GeminiApiClient()

        buildCyberneticHud()
        textToSpeech = TextToSpeech(this, this)
        setupSpeechRecognizer()
        checkPermissions()
    }

    private fun buildCyberneticHud() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(darkBgColor)
            setPadding(40, 60, 40, 40)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Header Title
        val titleView = TextView(this).apply {
            text = "J.A.R.V.I.S. // KOTLIN OS"
            setTextColor(cyanColor)
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setShadowLayer(14f, 0f, 0f, cyanColor)
        }
        root.addView(titleView)

        // Status Subtitle
        statusView = TextView(this).apply {
            text = "MARK VII ONLINE // SYSTEM ARMED"
            setTextColor(Color.rgb(140, 190, 230))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 40)
        }
        root.addView(statusView)

        // Arc Reactor Orb
        arcReactorOrb = FrameLayout(this).apply {
            val size = 320
            this.layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, 20, 0, 40)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(40, 70, 220, 255))
                setStroke(6, cyanColor)
            }
        }
        root.addView(arcReactorOrb)

        // Live Transcript Container
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
            )
            background = GradientDrawable().apply {
                setColor(cardBgColor)
                cornerRadius = 24f
                setStroke(2, Color.argb(80, 70, 220, 255))
            }
            setPadding(30, 30, 30, 30)
        }

        transcriptView = TextView(this).apply {
            text = "Say 'Hey Jarvis' or tap Voice Command below..."
            setTextColor(Color.WHITE)
            textSize = 15f
            lineSpacingExtra = 8f
        }
        scroll.addView(transcriptView)
        root.addView(scroll)

        // Controls bar
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 30, 0, 0)
            gravity = Gravity.CENTER
        }

        val btnListen = Button(this).apply {
            text = "COMMAND"
            setTextColor(Color.BLACK)
            setBackgroundColor(cyanColor)
            setOnClickListener { startListening() }
        }
        controls.addView(btnListen)

        root.addView(controls)
        setContentView(root)
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                statusView.text = "LISTENING... SAY YOUR COMMAND"
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                statusView.text = "PROCESSING QUERY..."
            }
            override fun onError(error: Int) {
                statusView.text = "READY // STANDBY"
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val query = matches?.firstOrNull().orEmpty()
                if (query.isNotBlank()) {
                    handleVoiceCommand(query)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer.startListening(intent)
    }

    private fun handleVoiceCommand(query: String) {
        transcriptView.text = "YOU: $query\n\nJARVIS: Analyzing command..."
        activityScope.launch {
            val result = geminiClient.generateJarvisResponse(query)
            result.onSuccess { aiText ->
                processAiResponse(aiText)
            }.onFailure { err ->
                val fallback = "Offline fallback: Command received: '$query'. ${err.localizedMessage}"
                transcriptView.text = "YOU: $query\n\nJARVIS: $fallback"
                speak(fallback)
            }
        }
    }

    private fun processAiResponse(raw: String) {
        var cleanText = raw
        val actionMatch = Regex("\\[ACTION:(CALL|WHATSAPP|OPEN_APP|SEARCH|TORCH|BATTERY)\\s*\\|([^]]*)]", RegexOption.IGNORE_CASE).find(raw)
        if (actionMatch != null) {
            val actionName = actionMatch.groupValues[1].uppercase(Locale.ROOT)
            val parts = actionMatch.groupValues[2].split('|').map { it.trim() }
            val result = when (actionName) {
                "CALL" -> actionController.executeAction(ActionType.MAKE_CALL, parts.getOrNull(0), null)
                "WHATSAPP" -> actionController.executeAction(ActionType.SEND_WHATSAPP, parts.getOrNull(0), parts.getOrNull(1))
                "OPEN_APP" -> actionController.executeAction(ActionType.OPEN_APP, parts.getOrNull(0), null)
                "SEARCH" -> actionController.executeAction(ActionType.SEARCH_WEB, parts.getOrNull(0), null)
                "TORCH" -> actionController.executeAction(ActionType.TOGGLE_TORCH, parts.getOrNull(0), null)
                "BATTERY" -> actionController.executeAction(ActionType.CHECK_BATTERY, null, null)
                else -> null
            }
            cleanText = raw.replace(actionMatch.value, "").trim()
            if (result != null) cleanText = listOf(cleanText, result.userFeedback).filter { it.isNotBlank() }.joinToString("\n")
        }
        transcriptView.text = "JARVIS:\n$cleanText"
        speak(cleanText)
    }
    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_REPLY")
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS
        )
        ActivityCompat.requestPermissions(this, permissions, 101)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val hindiLocale = Locale("hi", "IN")
            textToSpeech?.language = if (textToSpeech?.isLanguageAvailable(hindiLocale) == TextToSpeech.LANG_AVAILABLE || textToSpeech?.isLanguageAvailable(hindiLocale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) hindiLocale else Locale("en", "IN")
        }
    }

    override fun onDestroy() {
        // Cancel in-flight network work before the Activity is discarded.
        activityScope.coroutineContext[Job]?.cancel()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.cancel()
            speechRecognizer.destroy()
        }
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
