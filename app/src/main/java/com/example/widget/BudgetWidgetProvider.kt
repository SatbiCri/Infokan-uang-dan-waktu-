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
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BudgetWidgetProvider : AppWidgetProvider() {

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
            val thisWidget = ComponentName(context, BudgetWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context, BudgetWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        suspend fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val db = AppDatabase.getInstance(context)
            val budgets = db.budgetDao().getBudgetsDirect()

            val views = RemoteViews(context.packageName, R.layout.widget_budget)
            val openIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_budget_root, pendingIntent)

            if (budgets.isEmpty()) {
                views.setTextViewText(R.id.widget_budget_spent_text, "Belum Ada Anggaran")
                views.setTextViewText(R.id.widget_budget_percent_badge, "0%")
                views.setProgressBar(R.id.widget_budget_progress, 100, 0, false)
                views.setTextViewText(R.id.widget_budget_remaining_text, "Atur di dalam aplikasi")
                views.setTextViewText(R.id.widget_budget_item_1, "• Tekan untuk membuka aplikasi")
                views.setTextViewText(R.id.widget_budget_item_2, "• Kontrol pengeluaran harian & bulanan")
            } else {
                var totalSpent = 0L
                var totalLimit = 0L
                val items = mutableListOf<String>()

                for (b in budgets) {
                    val startTime = if (b.period == BudgetPeriod.HARIAN) {
                        DateTimeUtils.getStartOfTodayMillis()
                    } else {
                        DateTimeUtils.getStartOfMonthMillis()
                    }
                    val spent = db.transactionDao().getCategoryExpenseSince(b.category, startTime) ?: 0L
                    totalSpent += spent
                    totalLimit += b.limitAmount
                    val isExceeded = spent >= b.limitAmount
                    val status = if (isExceeded) "⚠️ MELEBIHI" else "Terkontrol"
                    items.add("• ${b.category}: $status (${CurrencyUtils.formatCompactRupiah(spent)}/${CurrencyUtils.formatCompactRupiah(b.limitAmount)})")
                }

                val totalPercent = if (totalLimit > 0) ((totalSpent.toDouble() / totalLimit.toDouble()) * 100).toInt() else 0
                val cappedPercent = totalPercent.coerceIn(0, 100)

                views.setTextViewText(R.id.widget_budget_spent_text, "Terpakai: ${CurrencyUtils.formatRupiah(totalSpent)}")
                views.setTextViewText(
                    R.id.widget_budget_percent_badge,
                    if (totalSpent >= totalLimit && totalLimit > 0) "⚠️ $cappedPercent%" else "$cappedPercent%"
                )
                views.setProgressBar(R.id.widget_budget_progress, 100, cappedPercent, false)
                views.setTextViewText(R.id.widget_budget_remaining_text, "Batas Total: ${CurrencyUtils.formatRupiah(totalLimit)}")

                if (items.isNotEmpty()) {
                    views.setTextViewText(R.id.widget_budget_item_1, items[0])
                }
                if (items.size > 1) {
                    views.setTextViewText(R.id.widget_budget_item_2, items[1])
                } else {
                    views.setTextViewText(R.id.widget_budget_item_2, "• Semua pengeluaran tercatat")
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
