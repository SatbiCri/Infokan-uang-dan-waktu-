package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.InfokanRepository
import com.example.util.DateTimeUtils
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InfokanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InfokanRepository
    val deviceId: String

    init {
        val db = AppDatabase.getInstance(application)
        repository = InfokanRepository(application, db)
        deviceId = SecurityUtils.getOrCreateDeviceId(application)

        viewModelScope.launch {
            repository.initializeUserIfNeeded(deviceId)
        }
    }

    val userAccount: StateFlow<UserAccount?> = repository.userAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctMonths: StateFlow<List<String>> = repository.distinctMonths
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedMonth = MutableStateFlow<String>("")

    val filteredTransactions = combine(allTransactions, selectedMonth) { list, month ->
        if (month.isBlank()) {
            list
        } else {
            list.filter { it.monthYear.equals(month, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayExpenses: StateFlow<Long> = repository.getTodayExpenses()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthExpenses: StateFlow<Long> = repository.getMonthExpenses()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<ScheduleEntity>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Spending map for each budget category
    private val _budgetSpending = MutableStateFlow<Map<String, Long>>(emptyMap())
    val budgetSpending: StateFlow<Map<String, Long>> = _budgetSpending.asStateFlow()

    init {
        // Observe transactions and budgets to recalculate spent amounts
        viewModelScope.launch {
            combine(allBudgets, allTransactions) { budgets, _ -> budgets }.collect { budgets ->
                val map = mutableMapOf<String, Long>()
                for (b in budgets) {
                    val spent = repository.getSpentForBudget(b.category, b.period)
                    map[b.category] = spent
                }
                _budgetSpending.value = map
            }
        }
    }

    fun selectMonth(month: String) {
        selectedMonth.value = month
    }

    fun activateApp(key: String, onResult: (Boolean) -> Unit) {
        val isValid = SecurityUtils.verifyActivationKey(deviceId, key)
        if (isValid) {
            viewModelScope.launch {
                repository.setActivated(key)
                onResult(true)
            }
        } else {
            onResult(false)
        }
    }

    fun updateBalances(cash: Long, debit: Long) {
        viewModelScope.launch {
            repository.updateBalances(cash, debit)
        }
    }

    fun updateProfile(
        userName: String,
        avatarId: Int,
        notifyExpense: Boolean,
        notifyIncome: Boolean,
        notifyAlarm: Boolean
    ) {
        viewModelScope.launch {
            repository.updateProfile(userName, avatarId, notifyExpense, notifyIncome, notifyAlarm)
        }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Long,
        accountType: AccountType,
        category: String,
        bankName: String = "",
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                type = type,
                amount = amount,
                accountType = accountType,
                category = category,
                bankName = bankName,
                notes = notes,
                timestamp = timestamp
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addBudget(category: String, period: BudgetPeriod, limitAmount: Long) {
        viewModelScope.launch {
            repository.addBudget(
                BudgetEntity(
                    category = category,
                    period = period,
                    limitAmount = limitAmount,
                    notes = "Tetapkan anggaran untuk mengontrol pengeluaran"
                )
            )
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    fun addSchedule(
        type: ScheduleType,
        title: String,
        dateTimeMillis: Long,
        recurrence: RecurrenceType,
        hasAlarm: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addSchedule(
                ScheduleEntity(
                    type = type,
                    title = title,
                    dateTimeMillis = dateTimeMillis,
                    recurrence = recurrence,
                    hasAlarm = hasAlarm,
                    notes = notes
                )
            )
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteSchedule(id)
        }
    }

    fun addTask(
        title: String,
        notes: String,
        startTimeMillis: Long,
        deadlineMillis: Long,
        hasAlarm: Boolean
    ) {
        viewModelScope.launch {
            repository.addTask(
                TaskEntity(
                    title = title,
                    notes = notes,
                    startTimeMillis = startTimeMillis,
                    deadlineMillis = deadlineMillis,
                    hasAlarm = hasAlarm
                )
            )
        }
    }

    fun updateTaskStatus(id: Long, status: TaskStatus) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, status)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // Alarm
    fun addAlarm(hour: Int, minute: Int, label: String, repeatDays: String, vibrate: Boolean) {
        viewModelScope.launch {
            repository.addAlarm(hour, minute, label, repeatDays, vibrate)
        }
    }

    fun toggleAlarm(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarm(id, isEnabled)
        }
    }

    fun deleteAlarm(id: Long) {
        viewModelScope.launch {
            repository.deleteAlarm(id)
        }
    }
}
