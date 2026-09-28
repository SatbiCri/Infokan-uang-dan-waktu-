package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = AppDatabase.getInstance(context)
                val now = System.currentTimeMillis()

                // Reschedule alarms
                val alarms = db.alarmDao().getAllAlarmsDirect()
                for (a in alarms) {
                    if (a.isEnabled) {
                        val triggerMillis = NotificationHelper.calculateNextAlarmMillis(a.hour, a.minute, a.repeatDays)
                        NotificationHelper.scheduleAlarm(
                            context = context,
                            requestCode = (a.id + 400000).toInt(),
                            triggerAtMillis = triggerMillis,
                            title = "⏰ ${a.label}",
                            message = "Waktu menunjukkan pukul ${a.getFormattedTime()} WIB. Alarm berbunyi!",
                            channelId = NotificationHelper.CHANNEL_ALARM
                        )
                    }
                }

                // Reschedule upcoming schedules
                val schedules = db.scheduleDao().getSchedulesDirect()
                for (s in schedules) {
                    if (s.hasAlarm && s.dateTimeMillis > now) {
                        NotificationHelper.scheduleAlarm(
                            context = context,
                            requestCode = (s.id + 100000).toInt(),
                            triggerAtMillis = s.dateTimeMillis,
                            title = "🗓 ${s.title}",
                            message = if (s.notes.isNotBlank()) s.notes else "Waktunya aktivitas: ${s.title}",
                            channelId = NotificationHelper.CHANNEL_SCHEDULE
                        )
                    }
                }

                // Reschedule upcoming tasks
                val tasks = db.taskDao().getTasksDirect()
                for (t in tasks) {
                    if (t.hasAlarm && t.deadlineMillis > now) {
                        NotificationHelper.scheduleAlarm(
                            context = context,
                            requestCode = (t.id + 200000).toInt(),
                            triggerAtMillis = t.deadlineMillis,
                            title = "⏳ Deadline Tugas: ${t.title}",
                            message = if (t.notes.isNotBlank()) t.notes else "Tugas harus diselesaikan!",
                            channelId = NotificationHelper.CHANNEL_TASK
                        )
                    }
                }
            }
        }
    }
}
