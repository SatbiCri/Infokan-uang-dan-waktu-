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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmWidgetProvider : AppWidgetProvider() {

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
            val thisWidget = ComponentName(context, AlarmWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context, AlarmWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        suspend fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val db = AppDatabase.getInstance(context)
            val alarms = db.alarmDao().getAllAlarmsDirect()
            val activeAlarm = alarms.firstOrNull { it.isEnabled }

            val views = RemoteViews(context.packageName, R.layout.widget_alarm)
            val openIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("ROUTE", "ALARM")
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 1005, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_alarm_root, pendingIntent)

            if (activeAlarm == null) {
                views.setTextViewText(R.id.widget_alarm_status_badge, "Nonaktif")
                views.setTextViewText(R.id.widget_alarm_time_big, "--:--")
                views.setTextViewText(R.id.widget_alarm_label, "Semua alarm dinonaktifkan")
                views.setTextViewText(R.id.widget_alarm_repeat_days, "Tekan untuk mengaktifkan alarm")
                views.setTextViewText(R.id.widget_alarm_next_info, "• Atur jam bangun & pengingat aktivitas")
            } else {
                views.setTextViewText(R.id.widget_alarm_status_badge, "Aktif")
                views.setTextViewText(R.id.widget_alarm_time_big, activeAlarm.getFormattedTime())
                views.setTextViewText(R.id.widget_alarm_label, activeAlarm.label)
                views.setTextViewText(R.id.widget_alarm_repeat_days, "Ulangi: ${activeAlarm.getRepeatDaysLabel()}")
                views.setTextViewText(
                    R.id.widget_alarm_next_info,
                    if (activeAlarm.vibrate) "• Dering suara alarm keras + Getar aktif" else "• Dering suara alarm keras"
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
