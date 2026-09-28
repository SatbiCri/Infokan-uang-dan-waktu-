package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.receiver.AlarmReceiver
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationHelper {
    const val CHANNEL_FINANCE = "infokan_channel_finance"
    const val CHANNEL_SCHEDULE = "infokan_channel_schedule"
    const val CHANNEL_TASK = "infokan_channel_task"
    const val CHANNEL_ALARM = "infokan_channel_alarm"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM,
                "Alarm Bangun & Harian",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm bersuara keras seperti alarm jam bawaan HP"
                enableVibration(true)
                setSound(alarmSound, audioAttributes)
            }

            val financeChannel = NotificationChannel(
                CHANNEL_FINANCE,
                "Keuangan & Anggaran",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peringatan anggaran penuh/melebihi batas dan transaksi"
                enableVibration(true)
            }

            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE,
                "Jadwal & Acara",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat alarm aktivitas dan jadwal harian"
                enableVibration(true)
            }

            val taskChannel = NotificationChannel(
                CHANNEL_TASK,
                "Tugas & Deadline",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat waktu pengerjaan dan deadline tugas"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(alarmChannel, financeChannel, scheduleChannel, taskChannel)
            )
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        playAlarmSound: Boolean = true
    ) {
        initChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(
            if (playAlarmSound) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_NOTIFICATION
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 800, 400, 800, 400, 800))

        if (playAlarmSound) {
            builder.setSound(soundUri)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Calculates the next timestamp (in epoch millis) for an alarm given hour, minute and repeatDays
     * repeatDays: "1,2,3,4,5,6,7" where 1=Monday, 7=Sunday
     */
    fun calculateNextAlarmMillis(hour: Int, minute: Int, repeatDays: String): Long {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val daysList = if (repeatDays.isBlank()) {
            emptySet()
        } else {
            repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }

        if (daysList.isEmpty()) {
            // One-shot alarm: today or tomorrow
            var candidate = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (!candidate.isAfter(now)) {
                candidate = candidate.plusDays(1)
            }
            return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        // Repeating on specific days
        for (i in 0..7) {
            val date = now.plusDays(i.toLong())
            val dayOfWeekInt = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
            if (daysList.contains(dayOfWeekInt)) {
                val candidate = date.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                if (candidate.isAfter(now)) {
                    return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
        }

        // Fallback next week
        return now.plusDays(1).withHour(hour).withMinute(minute).withSecond(0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun scheduleAlarm(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        message: String,
        channelId: String
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.aistudio.infokan.ACTION_ALARM"
            putExtra("EXTRA_REQ_CODE", requestCode)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
            putExtra("EXTRA_CHANNEL", channelId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.aistudio.infokan.ACTION_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
