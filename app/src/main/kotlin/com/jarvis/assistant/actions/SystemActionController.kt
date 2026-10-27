package com.jarvis.assistant.actions

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.ContactsContract
import com.jarvis.assistant.model.ActionResult
import com.jarvis.assistant.model.ActionType

class SystemActionController(private val context: Context) {

    private var isTorchEnabled = false

    fun executeAction(actionType: ActionType, target: String?, payload: String?): ActionResult {
        return when (actionType) {
            ActionType.MAKE_CALL -> makePhoneCall(target)
            ActionType.SEND_WHATSAPP -> sendWhatsAppMessage(target, payload)
            ActionType.OPEN_APP -> openApplication(target)
            ActionType.SEARCH_WEB -> searchWeb(target ?: payload)
            ActionType.TOGGLE_TORCH -> toggleTorch(target)
            ActionType.CHECK_BATTERY -> getBatteryStatus()
            else -> ActionResult(false, "Unknown command, sir.")
        }
    }

    private fun makePhoneCall(target: String?): ActionResult {
        if (target.isNullOrBlank()) return ActionResult(false, "Who should I call, sir?")
        val phoneNumber = resolveContactOrNumber(target)
        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Dialing $target now, sir.")
        } catch (e: Exception) {
            ActionResult(false, "Unable to initiate call: ${e.message}")
        }
    }

    private fun sendWhatsAppMessage(contact: String?, message: String?): ActionResult {
        if (contact.isNullOrBlank()) return ActionResult(false, "Specify a recipient for WhatsApp, sir.")
        val text = message ?: "Hello from JARVIS"
        return try {
            val phone = resolveContactOrNumber(contact).replace("+", "").replace(" ", "")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(text)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Opening WhatsApp to send message to $contact.")
        } catch (e: Exception) {
            ActionResult(false, "WhatsApp dispatch failed: ${e.message}")
        }
    }

    private fun openApplication(appName: String?): ActionResult {
        if (appName.isNullOrBlank()) return ActionResult(false, "Which application shall I open?")
        val target = appName.lowercase()
        val pkg = when {
            target.contains("youtube") -> "com.google.android.youtube"
            target.contains("chrome") || target.contains("browser") -> "com.android.chrome"
            target.contains("whatsapp") -> "com.whatsapp"
            target.contains("instagram") -> "com.instagram.android"
            target.contains("facebook") -> "com.facebook.katana"
            else -> null
        }

        return try {
            if (pkg != null) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ActionResult(true, "Launching $appName immediately, sir.")
                }
            }
            // Fallback search in Play Store or browser
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(appName)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            ActionResult(true, "Searching for $appName on the web.")
        } catch (e: Exception) {
            ActionResult(false, "Unable to open $appName: ${e.message}")
        }
    }

    private fun searchWeb(query: String?): ActionResult {
        if (query.isNullOrBlank()) return ActionResult(false, "What shall I search for, sir?")
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Searching the mainframe for: $query")
        } catch (e: Exception) {
            ActionResult(false, "Search failed: ${e.message}")
        }
    }

    private fun toggleTorch(mode: String?): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return ActionResult(false, "Camera hardware not accessible.")
            val cameraId = cameraManager.cameraIdList.firstOrNull()
                ?: return ActionResult(false, "No camera flash detected.")

            val turnOn = if (mode?.equals("off", ignoreCase = true) == true) false else !isTorchEnabled
            cameraManager.setTorchMode(cameraId, turnOn)
            isTorchEnabled = turnOn
            val status = if (turnOn) "engaged" else "disengaged"
            ActionResult(true, "Illumination $status, sir.")
        } catch (e: Exception) {
            ActionResult(false, "Flashlight control failed: ${e.message}")
        }
    }

    private fun getBatteryStatus(): ActionResult {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            ActionResult(true, "Power cells currently at $level percent capacity, sir.")
        } catch (e: Exception) {
            ActionResult(false, "Telemetry unavailable: ${e.message}")
        }
    }

    private fun resolveContactOrNumber(query: String): String {
        if (query.any { it.isDigit() }) return query.replace("[^0-9+]".toRegex(), "")
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$query%"),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(0)
                }
            }
            query
        } catch (e: Exception) {
            query
        }
    }
}
