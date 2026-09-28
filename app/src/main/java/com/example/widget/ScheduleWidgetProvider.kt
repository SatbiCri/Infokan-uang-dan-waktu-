package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, ScheduleWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context, ScheduleWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        suspend fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val db = AppDatabase.getInstance(context)
            val schedules = db.scheduleDao().getSchedulesDirect()
            val todaySchedules = schedules.filter { DateTimeUtils.isToday(it.dateTimeMillis) }
                .sortedBy { it.dateTimeMillis }

            val views = RemoteViews(context.packageName, R.layout.widget_schedule)
            val openIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_schedule_root, pendingIntent)

            views.setTextViewText(R.id.widget_schedule_date, DateTimeUtils.formatShortDate(System.currentTimeMillis()))

            if (todaySchedules.isEmpty()) {
                views.setTextViewText(R.id.widget_schedule_item_1_time, "⏰ Santai!")
                views.setTextViewText(R.id.widget_schedule_item_1_title, "Tidak ada jadwal hari ini")
                views.setTextViewText(R.id.widget_schedule_item_1_loc, "Tekan untuk membuat jadwal baru")
                views.setTextViewText(R.id.widget_schedule_item_2, "• Manfaatkan waktu luang untuk istirahat / belajar")
            } else {
                val first = todaySchedules[0]
                views.setTextViewText(R.id.widget_schedule_item_1_time, "⏰ Pukul ${DateTimeUtils.formatTimeOnly(first.dateTimeMillis)} WIB")
                views.setTextViewText(R.id.widget_schedule_item_1_title, first.title)
                views.setTextViewText(R.id.widget_schedule_item_1_loc, if (first.notes.isNotBlank()) "📝 ${first.notes}" else "Tipe: ${if (first.type == com.example.data.model.ScheduleType.ACARA_KEGIATAN) "Acara / Kegiatan" else "Aktivitas"}")

                if (todaySchedules.size > 1) {
                    val second = todaySchedules[1]
                    views.setTextViewText(
                        R.id.widget_schedule_item_2,
                        "• Berikutnya: ${DateTimeUtils.formatTimeOnly(second.dateTimeMillis)} - ${second.title}"
                    )
                } else {
                    views.setTextViewText(R.id.widget_schedule_item_2, "• Hanya 1 jadwal tersisa hari ini")
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
