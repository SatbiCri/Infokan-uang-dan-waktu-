package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.util.DateTimeUtils
import com.example.util.NotificationHelper
import com.example.widget.InfokanWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InfokanRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val userDao = database.userDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val scheduleDao = database.scheduleDao()
    private val taskDao = database.taskDao()
    private val alarmDao = database.alarmDao()
    private val categoryDao = database.categoryDao()

    val userAccount: Flow<UserAccount?> = userDao.getUserAccount()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val distinctMonths: Flow<List<String>> = transactionDao.getDistinctMonths()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allSchedules: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getTransactionsByMonth(monthYear: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByMonth(monthYear)
    }

    fun getTodayExpenses(): Flow<Long?> {
        return transactionDao.getTodayExpenses(DateTimeUtils.getStartOfTodayMillis())
    }

    fun getMonthExpenses(): Flow<Long?> {
        return transactionDao.getMonthExpenses(DateTimeUtils.getStartOfMonthMillis())
    }

    suspend fun initializeUserIfNeeded(deviceId: String) = withContext(Dispatchers.IO) {
        val existing = userDao.getUserAccountDirect()
        if (existing == null) {
            val newUser = UserAccount(
                id = 1,
                deviceId = deviceId,
                isActivated = false,
                userName = "Anak Kost Mandiri",
                cashBalance = 0L,
                debitBalance = 0L
            )
            userDao.insertUser(newUser)

            // Seed initial alarms
            val defaultAlarms = listOf(
                AlarmEntity(
                    hour = 5,
                    minute = 0,
                    label = "Bangun Pagi & Sholat Subuh",
                    isEnabled = true,
                    repeatDays = "1,2,3,4,5,6,7",
                    vibrate = true
                ),
                AlarmEntity(
                    hour = 7,
                    minute = 0,
                    label = "Siap-siap Kuliah & Aktivitas",
                    isEnabled = true,
                    repeatDays = "1,2,3,4,5",
                    vibrate = true
                )
            )
            for (alarm in defaultAlarms) {
                val alarmId = alarmDao.insertAlarm(alarm)
                val nextMillis = NotificationHelper.calculateNextAlarmMillis(alarm.hour, alarm.minute, alarm.repeatDays)
                NotificationHelper.scheduleAlarm(
                    context = context,
                    requestCode = (alarmId + 400000).toInt(),
                    triggerAtMillis = nextMillis,
                    title = "⏰ ${alarm.label}",
                    message = "Waktu menunjukkan pukul ${alarm.getFormattedTime()} WIB. Saatnya bangun!",
                    channelId = NotificationHelper.CHANNEL_ALARM
                )
            }
        }
        seedDefaultCategoriesIfNeeded()
    }

    suspend fun seedDefaultCategoriesIfNeeded() = withContext(Dispatchers.IO) {
        val existing = categoryDao.getAllCategoriesDirect()
        if (existing.isEmpty()) {
            val defaults = listOf(
                CategoryEntity(mainCategory = "Makan dan Minuman", subCategory = "Makan Siang & Malam", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Makan dan Minuman", subCategory = "Sarapan", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Makan dan Minuman", subCategory = "Kopi & Nongkrong", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Makan dan Minuman", subCategory = "Camilan & Jajan", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Transportasi", subCategory = "Bensin / BBM", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Transportasi", subCategory = "Ojek / Taksi Online", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Transportasi", subCategory = "Parkir", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Kebutuhan Kuliah", subCategory = "Buku & Alat Tulis", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Kebutuhan Kuliah", subCategory = "Fotokopi & Print", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Kebutuhan Kuliah", subCategory = "Uang Kas & UKM", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Kebutuhan Kost", subCategory = "Sabun & Kebutuhan Mandi", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Kebutuhan Kost", subCategory = "Laundry", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Kebutuhan Kost", subCategory = "Token Listrik & Air", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Hiburan & Pribadi", subCategory = "Paket Data Internet", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Hiburan & Pribadi", subCategory = "Bioskop / Nonton", type = TransactionType.PENGELUARAN),
                CategoryEntity(mainCategory = "Pemasukan", subCategory = "Uang Saku Orang Tua", type = TransactionType.PEMASUKAN),
                CategoryEntity(mainCategory = "Pemasukan", subCategory = "Gaji / Upah Freelance", type = TransactionType.PEMASUKAN),
                CategoryEntity(mainCategory = "Pemasukan", subCategory = "Beasiswa", type = TransactionType.PEMASUKAN),
                CategoryEntity(mainCategory = "Pemasukan", subCategory = "Hadiah / Transfer Lain", type = TransactionType.PEMASUKAN)
            )
            for (cat in defaults) {
                categoryDao.insertCategory(cat)
            }
        }
    }

    suspend fun addCategory(main: String, sub: String, type: TransactionType) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(CategoryEntity(mainCategory = main.trim(), subCategory = sub.trim(), type = type))
    }

    suspend fun deleteCategory(id: Long) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(id)
    }

    suspend fun setActivated(key: String) = withContext(Dispatchers.IO) {
        userDao.setActivated(key)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun updateBalances(cash: Long, debit: Long) = withContext(Dispatchers.IO) {
        userDao.updateBalances(cash, debit)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun updateProfile(
        userName: String,
        avatarId: Int,
        notifyExpense: Boolean,
        notifyIncome: Boolean,
        notifyAlarm: Boolean
    ) = withContext(Dispatchers.IO) {
        val current = userDao.getUserAccountDirect() ?: return@withContext
        val updated = current.copy(
            userName = userName,
            avatarId = avatarId,
            notifyExpenseSound = notifyExpense,
            notifyIncomeSound = notifyIncome,
            notifyAlarmSound = notifyAlarm
        )
        userDao.updateUser(updated)
    }

    suspend fun addTransaction(
        type: TransactionType,
        amount: Long,
        accountType: AccountType,
        category: String,
        bankName: String,
        notes: String,
        timestamp: Long,
        subCategory: String = ""
    ): String? = withContext(Dispatchers.IO) {
        val monthYear = DateTimeUtils.getMonthYear(timestamp)
        val transaction = TransactionEntity(
            type = type,
            amount = amount,
            accountType = accountType,
            category = category,
            subCategory = subCategory,
            bankName = bankName,
            notes = notes,
            timestamp = timestamp,
            monthYear = monthYear
        )
        transactionDao.insertTransaction(transaction)

        // Update User Cash / Debit balance
        val user = userDao.getUserAccountDirect()
        var alertMessage: String? = null
        if (user != null) {
            var newCash = user.cashBalance
            var newDebit = user.debitBalance

            when (type) {
                TransactionType.PENGELUARAN -> {
                    if (accountType == AccountType.CASH) {
                        newCash -= amount
                    } else {
                        newDebit -= amount
                    }
                }
                TransactionType.PEMASUKAN -> {
                    if (accountType == AccountType.CASH) {
                        newCash += amount
                    } else {
                        newDebit += amount
                    }
                }
                TransactionType.TARIK_TUNAI -> {
                    newDebit -= amount
                    newCash += amount
                }
                TransactionType.SETOR_TUNAI -> {
                    newCash -= amount
                    newDebit += amount
                }
            }
            userDao.updateBalances(newCash, newDebit)

            // Play notification sound / trigger notification if enabled
            if (type == TransactionType.PENGELUARAN && user.notifyExpenseSound) {
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = System.currentTimeMillis().toInt(),
                    channelId = NotificationHelper.CHANNEL_FINANCE,
                    title = "💸 Pengeluaran Dicatat: $category",
                    message = "Sebesar Rp $amount (${if (accountType == AccountType.CASH) "Cash" else "Kartu Debit"}).",
                    playAlarmSound = false
                )
            } else if (type == TransactionType.PEMASUKAN && user.notifyIncomeSound) {
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = System.currentTimeMillis().toInt(),
                    channelId = NotificationHelper.CHANNEL_FINANCE,
                    title = "💰 Pemasukan Masuk: $category",
                    message = "Sebesar Rp $amount ke ${if (accountType == AccountType.CASH) "Cash" else "Kartu Debit"}.",
                    playAlarmSound = false
                )
            }

            // Check Budget Alert
            if (type == TransactionType.PENGELUARAN) {
                val budget = budgetDao.getBudgetByCategory(category)
                if (budget != null) {
                    val startTime = if (budget.period == BudgetPeriod.HARIAN) {
                        DateTimeUtils.getStartOfTodayMillis()
                    } else {
                        DateTimeUtils.getStartOfMonthMillis()
                    }
                    val totalSpent = (transactionDao.getCategoryExpenseSince(category, startTime) ?: 0L)
                    if (totalSpent >= budget.limitAmount) {
                        val overAmount = totalSpent - budget.limitAmount
                        alertMessage = if (overAmount > 0)
                            "Pengeluaran $category telah melebihi batas sebesar Rp $overAmount!"
                        else
                            "Pengeluaran $category telah mencapai batas 100%!"
                        NotificationHelper.showNotification(
                            context = context,
                            notificationId = (budget.id + 9999).toInt(),
                            channelId = NotificationHelper.CHANNEL_ALARM,
                            title = "⚠️ Peringatan Anggaran: $category!",
                            message = alertMessage,
                            playAlarmSound = user.notifyAlarmSound
                        )
                    }
                }
            }
        }

        InfokanWidgetProvider.updateAllWidgets(context)
        alertMessage
    }

    suspend fun updateTransaction(
        oldTrx: TransactionEntity,
        newTrx: TransactionEntity
    ): String? = withContext(Dispatchers.IO) {
        val user = userDao.getUserAccountDirect()
        var alertMessage: String? = null
        if (user != null) {
            var cash = user.cashBalance
            var debit = user.debitBalance

            // Revert old transaction balances
            when (oldTrx.type) {
                TransactionType.PENGELUARAN -> {
                    if (oldTrx.accountType == AccountType.CASH) cash += oldTrx.amount
                    else debit += oldTrx.amount
                }
                TransactionType.PEMASUKAN -> {
                    if (oldTrx.accountType == AccountType.CASH) cash -= oldTrx.amount
                    else debit -= oldTrx.amount
                }
                TransactionType.TARIK_TUNAI -> {
                    cash -= oldTrx.amount
                    debit += oldTrx.amount
                }
                TransactionType.SETOR_TUNAI -> {
                    cash += oldTrx.amount
                    debit -= oldTrx.amount
                }
            }

            // Apply new transaction balances
            when (newTrx.type) {
                TransactionType.PENGELUARAN -> {
                    if (newTrx.accountType == AccountType.CASH) cash -= newTrx.amount
                    else debit -= newTrx.amount
                }
                TransactionType.PEMASUKAN -> {
                    if (newTrx.accountType == AccountType.CASH) cash += newTrx.amount
                    else debit += newTrx.amount
                }
                TransactionType.TARIK_TUNAI -> {
                    cash += newTrx.amount
                    debit -= newTrx.amount
                }
                TransactionType.SETOR_TUNAI -> {
                    cash -= newTrx.amount
                    debit += newTrx.amount
                }
            }
            userDao.updateBalances(cash, debit)

            val monthYear = DateTimeUtils.getMonthYear(newTrx.timestamp)
            val updatedTrx = newTrx.copy(monthYear = monthYear)
            transactionDao.updateTransaction(updatedTrx)

            if (updatedTrx.type == TransactionType.PENGELUARAN) {
                val budget = budgetDao.getBudgetByCategory(updatedTrx.category)
                if (budget != null) {
                    val startTime = if (budget.period == BudgetPeriod.HARIAN) {
                        DateTimeUtils.getStartOfTodayMillis()
                    } else {
                        DateTimeUtils.getStartOfMonthMillis()
                    }
                    val totalSpent = (transactionDao.getCategoryExpenseSince(updatedTrx.category, startTime) ?: 0L)
                    if (totalSpent >= budget.limitAmount) {
                        val overAmount = totalSpent - budget.limitAmount
                        alertMessage = if (overAmount > 0)
                            "Pengeluaran ${updatedTrx.category} telah melebihi batas anggaran sebesar Rp $overAmount!"
                        else
                            "Pengeluaran ${updatedTrx.category} telah mencapai batas 100%!"
                        NotificationHelper.showNotification(
                            context = context,
                            notificationId = (budget.id + 9999).toInt(),
                            channelId = NotificationHelper.CHANNEL_ALARM,
                            title = "⚠️ Peringatan Anggaran: ${updatedTrx.category}!",
                            message = alertMessage,
                            playAlarmSound = user.notifyAlarmSound
                        )
                    }
                }
            }
        }

        InfokanWidgetProvider.updateAllWidgets(context)
        alertMessage
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction.id)
        val user = userDao.getUserAccountDirect()
        if (user != null) {
            var newCash = user.cashBalance
            var newDebit = user.debitBalance
            when (transaction.type) {
                TransactionType.PENGELUARAN -> {
                    if (transaction.accountType == AccountType.CASH) newCash += transaction.amount
                    else newDebit += transaction.amount
                }
                TransactionType.PEMASUKAN -> {
                    if (transaction.accountType == AccountType.CASH) newCash -= transaction.amount
                    else newDebit -= transaction.amount
                }
                TransactionType.TARIK_TUNAI -> {
                    newDebit += transaction.amount
                    newCash -= transaction.amount
                }
                TransactionType.SETOR_TUNAI -> {
                    newCash += transaction.amount
                    newDebit -= transaction.amount
                }
            }
            userDao.updateBalances(newCash, newDebit)
        }
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun addBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun updateBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.updateBudget(budget)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteBudget(id: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(id)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun getSpentForBudget(category: String, period: BudgetPeriod): Long = withContext(Dispatchers.IO) {
        val startTime = if (period == BudgetPeriod.HARIAN) {
            DateTimeUtils.getStartOfTodayMillis()
        } else {
            DateTimeUtils.getStartOfMonthMillis()
        }
        return@withContext transactionDao.getCategoryExpenseSince(category, startTime) ?: 0L
    }

    suspend fun addSchedule(schedule: ScheduleEntity) = withContext(Dispatchers.IO) {
        val id = scheduleDao.insertSchedule(schedule)
        if (schedule.hasAlarm && schedule.dateTimeMillis > System.currentTimeMillis()) {
            NotificationHelper.scheduleAlarm(
                context = context,
                requestCode = (id + 100000).toInt(),
                triggerAtMillis = schedule.dateTimeMillis,
                title = "🗓 ${schedule.title}",
                message = if (schedule.notes.isNotBlank()) schedule.notes else "Waktunya: ${schedule.title}",
                channelId = NotificationHelper.CHANNEL_SCHEDULE
            )
        }
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteSchedule(id: Long) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelAlarm(context, (id + 100000).toInt())
        scheduleDao.deleteSchedule(id)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun addTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        val id = taskDao.insertTask(task)
        if (task.hasAlarm) {
            val now = System.currentTimeMillis()
            if (task.startTimeMillis > now) {
                NotificationHelper.scheduleAlarm(
                    context = context,
                    requestCode = (id + 200000).toInt(),
                    triggerAtMillis = task.startTimeMillis,
                    title = "🚀 Mulai Kerjakan: ${task.title}",
                    message = if (task.notes.isNotBlank()) task.notes else "Saatnya mulai mengerjakan tugas!",
                    channelId = NotificationHelper.CHANNEL_TASK
                )
            }
            if (task.deadlineMillis > now) {
                NotificationHelper.scheduleAlarm(
                    context = context,
                    requestCode = (id + 300000).toInt(),
                    triggerAtMillis = task.deadlineMillis,
                    title = "⏳ Deadline Tugas: ${task.title}",
                    message = "Tenggat waktu pengerjaan tugas telah tiba!",
                    channelId = NotificationHelper.CHANNEL_TASK
                )
            }
        }
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun updateTaskStatus(id: Long, status: TaskStatus) = withContext(Dispatchers.IO) {
        taskDao.updateTaskStatus(id, status)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelAlarm(context, (id + 200000).toInt())
        NotificationHelper.cancelAlarm(context, (id + 300000).toInt())
        taskDao.deleteTask(id)
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    // Alarm management
    suspend fun addAlarm(
        hour: Int,
        minute: Int,
        label: String,
        repeatDays: String,
        vibrate: Boolean
    ) = withContext(Dispatchers.IO) {
        val alarm = AlarmEntity(
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = true,
            repeatDays = repeatDays,
            vibrate = vibrate
        )
        val id = alarmDao.insertAlarm(alarm)
        val triggerMillis = NotificationHelper.calculateNextAlarmMillis(hour, minute, repeatDays)
        NotificationHelper.scheduleAlarm(
            context = context,
            requestCode = (id + 400000).toInt(),
            triggerAtMillis = triggerMillis,
            title = "⏰ $label",
            message = "Waktu menunjukkan pukul ${alarm.getFormattedTime()} WIB. Alarm berbunyi!",
            channelId = NotificationHelper.CHANNEL_ALARM
        )
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun toggleAlarm(id: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        alarmDao.toggleAlarm(id, isEnabled)
        val alarm = alarmDao.getAlarmById(id)
        if (alarm != null) {
            val reqCode = (id + 400000).toInt()
            if (isEnabled) {
                val triggerMillis = NotificationHelper.calculateNextAlarmMillis(alarm.hour, alarm.minute, alarm.repeatDays)
                NotificationHelper.scheduleAlarm(
                    context = context,
                    requestCode = reqCode,
                    triggerAtMillis = triggerMillis,
                    title = "⏰ ${alarm.label}",
                    message = "Waktu menunjukkan pukul ${alarm.getFormattedTime()} WIB. Alarm berbunyi!",
                    channelId = NotificationHelper.CHANNEL_ALARM
                )
            } else {
                NotificationHelper.cancelAlarm(context, reqCode)
            }
        }
        InfokanWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteAlarm(id: Long) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelAlarm(context, (id + 400000).toInt())
        alarmDao.deleteAlarm(id)
        InfokanWidgetProvider.updateAllWidgets(context)
    }
}
