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
import com.example.data.model.TaskStatus
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskWidgetProvider : AppWidgetProvider() {

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
            val thisWidget = ComponentName(context, TaskWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context, TaskWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        suspend fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val db = AppDatabase.getInstance(context)
            val tasks = db.taskDao().getTasksDirect()
            val pendingTasks = tasks.filter { it.status != TaskStatus.SELESAI }
                .sortedBy { it.deadlineMillis }

            val views = RemoteViews(context.packageName, R.layout.widget_task)
            val openIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_task_root, pendingIntent)

            views.setTextViewText(R.id.widget_task_pending_count, "${pendingTasks.size} Belum Selesai")

            if (pendingTasks.isEmpty()) {
                views.setTextViewText(R.id.widget_task_1_deadline, "✅ Mantap!")
                views.setTextViewText(R.id.widget_task_1_title, "Semua tugas telah beres")
                views.setTextViewText(R.id.widget_task_1_subject, "Tidak ada deadline yang tertunda")
                views.setTextViewText(R.id.widget_task_2, "• Tekan untuk menambah catatan tugas baru")
            } else {
                val top = pendingTasks[0]
                val dlStr = DateTimeUtils.formatShortDate(top.deadlineMillis)
                val isUrgent = top.deadlineMillis - System.currentTimeMillis() < 24 * 3600 * 1000L
                views.setTextViewText(
                    R.id.widget_task_1_deadline,
                    if (isUrgent) "⚠️ DEADLINE: $dlStr" else "⏳ Deadline: $dlStr"
                )
                views.setTextViewText(R.id.widget_task_1_title, top.title)
                views.setTextViewText(
                    R.id.widget_task_1_subject,
                    if (top.notes.isNotBlank()) "📝 ${top.notes}" else "Status: ${if (top.status == TaskStatus.SEDANG) "Sedang Dikerjakan" else "Belum Dikerjakan"}"
                )

                if (pendingTasks.size > 1) {
                    val second = pendingTasks[1]
                    views.setTextViewText(
                        R.id.widget_task_2,
                        "• Berikutnya: ${second.title} (${DateTimeUtils.formatShortDate(second.deadlineMillis)})"
                    )
                } else {
                    views.setTextViewText(R.id.widget_task_2, "• Tersisa 1 tugas aktif")
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
