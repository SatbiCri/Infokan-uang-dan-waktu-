package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.security.MessageDigest
import java.util.UUID

object SecurityUtils {
    private const val PREFS_NAME = "infokan_security_prefs"
    private const val KEY_DEVICE_ID = "infokan_device_id"
    private const val KEY_ACTIVATED = "infokan_is_activated"
    private const val SECRET_SALT = "INFOKAN_OFFLINE_SECRET_KEY_2026"

    fun getOrCreateDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId.isNullOrBlank()) {
            // Generate clean human-readable device ID: INF-XXXX-YYYY
            val raw = UUID.randomUUID().toString().replace("-", "").uppercase()
            deviceId = "INF-${raw.take(4)}-${raw.substring(4, 8)}"
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    /**
     * Algoritma kode aktivasi offline berbasis hash SHA-256 dan deviceId
     * Format: AKT-XXXX-YYYY (8 digit hex/alphanumeric)
     */
    fun calculateActivationKey(deviceId: String): String {
        val input = "$deviceId:$SECRET_SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val hex = bytes.joinToString("") { "%02X".format(it) }
        val part1 = hex.take(4)
        val part2 = hex.substring(4, 8)
        return "AKT-$part1-$part2"
    }

    fun verifyActivationKey(deviceId: String, inputKey: String): Boolean {
        val expected = calculateActivationKey(deviceId)
        val cleanInput = inputKey.trim().uppercase()
        // Allow with or without "AKT-" prefix, or master offline dev key
        return cleanInput == expected ||
                cleanInput == expected.removePrefix("AKT-") ||
                cleanInput == "INFOKAN2026" ||
                cleanInput == "DEV-PASS-OFFLINE"
    }

    fun sendIdToDeveloper(context: Context, deviceId: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Infokan Device ID", deviceId)
            clipboard.setPrimaryClip(clip)

            val url = "https://wa.me/message/PRBQXSIM2V5WP1?src=qr"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val message = "Halo Developer Infokan,\nSaya ingin meminta kode aktivasi untuk ID Perangkat saya:\n\nID: $deviceId\n\nMohon bantuannya, terima kasih!"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Kirim ID ke Developer via...")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        }
    }
}
