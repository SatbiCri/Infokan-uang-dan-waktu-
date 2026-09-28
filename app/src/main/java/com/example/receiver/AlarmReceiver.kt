package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.AlarmSoundManager
import com.example.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reqCode = intent.getIntExtra("EXTRA_REQ_CODE", System.currentTimeMillis().toInt())
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Infokan Pengingat"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Ada aktivitas/jadwal yang perlu kamu perhatikan."
        val channel = intent.getStringExtra("EXTRA_CHANNEL") ?: NotificationHelper.CHANNEL_ALARM

        // Play loud alarm audio via AudioAttributes.USAGE_ALARM
        AlarmSoundManager.playLoudAlarm(context, vibrate = true)

        NotificationHelper.showNotification(
            context = context,
            notificationId = reqCode,
            channelId = channel,
            title = title,
            message = message,
            playAlarmSound = true
        )
    }
}

