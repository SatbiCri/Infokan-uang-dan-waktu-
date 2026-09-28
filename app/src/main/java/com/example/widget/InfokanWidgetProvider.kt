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
import com.example.data.model.BudgetPeriod
import com.example.data.model.TaskStatus
import com.example.data.model.TransactionType
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfokanWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, InfokanWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context, InfokanWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = AppDatabase.getInstance(context)
                val user = db.userDao().getUserAccountDirect()

                val cash = user?.cashBalance ?: 0L
                val debit = user?.debitBalance ?: 0L
                val total = cash + debit

                val recentTrx = db.transactionDao().getRecentTransactionsDirect(2)
                val budgets = db.budgetDao().getBudgetsDirect()
                val schedules = db.scheduleDao().getSchedulesDirect()
                val tasks = db.taskDao().getTasksDirect()
                val alarms = db.alarmDao().getAllAlarmsDirect()

                val views = RemoteViews(context.packageName, R.layout.infokan_widget)

                // Set click intent to open main app
                val openIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                // Date label
                views.setTextViewText(R.id.widget_date_label, DateTimeUtils.formatShortDate(System.currentTimeMillis()))

                // Balances
                views.setTextViewText(R.id.widget_total_balance, CurrencyUtils.formatRupiah(total))
                views.setTextViewText(R.id.widget_cash_balance, CurrencyUtils.formatRupiah(cash))
                views.setTextViewText(R.id.widget_debit_balance, CurrencyUtils.formatRupiah(debit))

                // 2 Last Transactions
                if (recentTrx.isNotEmpty()) {
                    val t1 = recentTrx[0]
                    val sign1 = if (t1.type == TransactionType.PENGELUARAN) "-" else if (t1.type == TransactionType.PEMASUKAN) "+" else "⇄"
                    val desc1 = if (t1.notes.isNotBlank()) t1.notes else t1.category
                    views.setTextViewText(R.id.widget_trx_1, "• $desc1: $sign1${CurrencyUtils.formatRupiah(t1.amount)}")
                } else {
                    views.setTextViewText(R.id.widget_trx_1, "• Belum ada transaksi")
                }

                if (recentTrx.size > 1) {
                    val t2 = recentTrx[1]
                    val sign2 = if (t2.type == TransactionType.PENGELUARAN) "-" else if (t2.type == TransactionType.PEMASUKAN) "+" else "⇄"
                    val desc2 = if (t2.notes.isNotBlank()) t2.notes else t2.category
                    views.setTextViewText(R.id.widget_trx_2, "• $desc2: $sign2${CurrencyUtils.formatRupiah(t2.amount)}")
                } else {
                    views.setTextViewText(R.id.widget_trx_2, "• -")
                }

                // Budget calculation
                if (budgets.isNotEmpty()) {
                    val firstBudget = budgets[0]
                    val startTime = if (firstBudget.period == BudgetPeriod.HARIAN) {
                        DateTimeUtils.getStartOfTodayMillis()
                    } else {
                        DateTimeUtils.getStartOfMonthMillis()
                    }
                    val spent = db.transactionDao().getCategoryExpenseSince(firstBudget.category, startTime) ?: 0L
                    val isExceeded = spent >= firstBudget.limitAmount
                    val percent = if (firstBudget.limitAmount > 0) ((spent.toDouble() / firstBudget.limitAmount.toDouble()) * 100).toInt() else 0
                    views.setTextViewText(
                        R.id.widget_budget_status,
                        if (isExceeded) "⚠️ Penuh $percent% (${firstBudget.category})" else "Terkontrol $percent% (${firstBudget.category})"
                    )
                    views.setTextViewText(
                        R.id.widget_budget_numbers,
                        "${CurrencyUtils.formatCompactRupiah(spent)} / ${CurrencyUtils.formatCompactRupiah(firstBudget.limitAmount)}"
                    )
                } else {
                    views.setTextViewText(R.id.widget_budget_status, "Belum diset")
                    views.setTextViewText(R.id.widget_budget_numbers, "Rp 0 / Rp 0")
                }

                // Alarm & Schedule
                val activeAlarm = alarms.firstOrNull { it.isEnabled }
                val todaySchedules = schedules.filter { DateTimeUtils.isToday(it.dateTimeMillis) }

                if (activeAlarm != null) {
                    views.setTextViewText(
                        R.id.widget_schedule_today,
                        "⏰ ${activeAlarm.getFormattedTime()} (${activeAlarm.label})"
                    )
                } else if (todaySchedules.isNotEmpty()) {
                    val first = todaySchedules[0]
                    views.setTextViewText(
                        R.id.widget_schedule_today,
                        "🗓 ${first.title} (${DateTimeUtils.formatTimeOnly(first.dateTimeMillis)})"
                    )
                } else {
                    views.setTextViewText(R.id.widget_schedule_today, "⏰ Alarm: nonaktif")
                }

                // Active task
                val activeTask = tasks.firstOrNull { it.status != TaskStatus.SELESAI }
                if (activeTask != null) {
                    val deadlineStr = DateTimeUtils.formatShortDate(activeTask.deadlineMillis)
                    views.setTextViewText(
                        R.id.widget_task_status,
                        "⏳ ${activeTask.title} (DL: $deadlineStr)"
                    )
                } else {
                    views.setTextViewText(R.id.widget_task_status, "✅ Semua tugas beres")
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
