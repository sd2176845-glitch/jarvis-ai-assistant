package com.jarvis.assistant.model

/**
 * Message exchange representation between user and Jarvis.
 */
data class AssistantMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: ActionType? = null,
    val actionDetail: String? = null
)

enum class MessageRole {
    USER,
    JARVIS,
    SYSTEM
}

enum class ActionType {
    MAKE_CALL,
    SEND_WHATSAPP,
    SEND_SMS,
    OPEN_APP,
    SEARCH_WEB,
    SET_ALARM,
    TOGGLE_TORCH,
    CHECK_BATTERY,
    GENERAL_KNOWLEDGE,
    UNKNOWN
}

data class ActionRequest(
    val type: ActionType,
    val target: String? = null,
    val payload: String? = null,
    val rawQuery: String
)

data class ActionResult(
    val success: Boolean,
    val userFeedback: String
)

data class JarvisUiState(
    val isListening: Boolean = false,
    val isThinking: Boolean = false,
    val isSpeaking: Boolean = false,
    val statusText: String = "JARVIS Online // Systems Nominal",
    val activeQuery: String = "",
    val latestResponse: String = "",
    val batteryPercent: Int = 100,
    val isTorchOn: Boolean = false
)
