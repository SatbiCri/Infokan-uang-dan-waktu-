package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ActivationScreen
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.TaskScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InfokanViewModel
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Notification Channels
        NotificationHelper.initChannels(this)

        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

enum class NavigationTab(val title: String) {
    KEUANGAN("Keuangan"),
    ALARM("Alarm"),
    JADWAL("Jadwal"),
    TUGAS("Tugas"),
    PROFIL("Profil")
}

@Composable
fun MainApp(viewModel: InfokanViewModel = viewModel()) {
    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val distinctMonths by viewModel.distinctMonths.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val todayExpenses by viewModel.todayExpenses.collectAsStateWithLifecycle()
    val monthExpenses by viewModel.monthExpenses.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val budgetSpending by viewModel.budgetSpending.collectAsStateWithLifecycle()
    val schedules by viewModel.allSchedules.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val alarms by viewModel.allAlarms.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val budgetAlert by viewModel.budgetAlert.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.KEUANGAN) }

    // Request notification permission if needed (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Permission handled gracefully */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Back handling: if in secondary tab, back to KEUANGAN
    BackHandler(enabled = currentTab != NavigationTab.KEUANGAN) {
        currentTab = NavigationTab.KEUANGAN
    }

    val isActivated = userAccount?.isActivated == true

    if (!isActivated) {
        // Show Activation Screen (Anti-kirim file system)
        ActivationScreen(
            deviceId = viewModel.deviceId,
            onActivated = { key ->
                viewModel.activateApp(key) { /* handled reactively by StateFlow */ }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // Keuangan Tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.KEUANGAN,
                        onClick = { currentTab = NavigationTab.KEUANGAN },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.KEUANGAN) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                contentDescription = "Keuangan"
                            )
                        },
                        label = {
                            Text(
                                text = "Keuangan",
                                fontSize = 10.sp,
                                fontWeight = if (currentTab == NavigationTab.KEUANGAN) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_keuangan")
                    )

                    // Alarm Tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.ALARM,
                        onClick = { currentTab = NavigationTab.ALARM },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.ALARM) Icons.Filled.Alarm else Icons.Outlined.Alarm,
                                contentDescription = "Alarm"
                            )
                        },
                        label = {
                            Text(
                                text = "Alarm",
                                fontSize = 10.sp,
                                fontWeight = if (currentTab == NavigationTab.ALARM) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_alarm")
                    )

                    // Jadwal Tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.JADWAL,
                        onClick = { currentTab = NavigationTab.JADWAL },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.JADWAL) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = "Jadwal"
                            )
                        },
                        label = {
                            Text(
                                text = "Jadwal",
                                fontSize = 10.sp,
                                fontWeight = if (currentTab == NavigationTab.JADWAL) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_jadwal")
                    )

                    // Catatan Tugas Tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.TUGAS,
                        onClick = { currentTab = NavigationTab.TUGAS },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.TUGAS) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                                contentDescription = "Tugas"
                            )
                        },
                        label = {
                            Text(
                                text = "Tugas",
                                fontSize = 10.sp,
                                fontWeight = if (currentTab == NavigationTab.TUGAS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_tugas")
                    )

                    // Profil & Suara Tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.PROFIL,
                        onClick = { currentTab = NavigationTab.PROFIL },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.PROFIL) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Profil"
                            )
                        },
                        label = {
                            Text(
                                text = "Profil",
                                fontSize = 10.sp,
                                fontWeight = if (currentTab == NavigationTab.PROFIL) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_profil")
                    )
                }
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentTab) {
                    NavigationTab.KEUANGAN -> {
                        FinanceScreen(
                            userAccount = userAccount,
                            transactions = transactions,
                            distinctMonths = distinctMonths,
                            selectedMonth = selectedMonth,
                            onSelectMonth = { viewModel.selectMonth(it) },
                            todayExpenses = todayExpenses,
                            monthExpenses = monthExpenses,
                            budgets = budgets,
                            budgetSpending = budgetSpending,
                            categories = categories,
                            budgetAlert = budgetAlert,
                            onClearBudgetAlert = { viewModel.clearBudgetAlert() },
                            onUpdateBalances = { cash, debit -> viewModel.updateBalances(cash, debit) },
                            onAddTransaction = { type, amount, accountType, category, bank, notes, time, subCat ->
                                viewModel.addTransaction(type, amount, accountType, category, bank, notes, time, subCat)
                            },
                            onUpdateTransaction = { oldTrx, newTrx ->
                                viewModel.updateTransaction(oldTrx, newTrx)
                            },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) },
                            onAddBudget = { category, period, amount ->
                                viewModel.addBudget(category, period, amount)
                            },
                            onDeleteBudget = { viewModel.deleteBudget(it) },
                            onAddCategory = { main, sub, type ->
                                viewModel.addCategory(main, sub, type)
                            },
                            onDeleteCategory = { viewModel.deleteCategory(it) }
                        )
                    }

                    NavigationTab.ALARM -> {
                        AlarmScreen(
                            alarms = alarms,
                            onAddAlarm = { h, m, label, days, vib ->
                                viewModel.addAlarm(h, m, label, days, vib)
                            },
                            onToggleAlarm = { id, enabled ->
                                viewModel.toggleAlarm(id, enabled)
                            },
                            onDeleteAlarm = { id ->
                                viewModel.deleteAlarm(id)
                            }
                        )
                    }

                    NavigationTab.JADWAL -> {
                        ScheduleScreen(
                            schedules = schedules,
                            onAddSchedule = { type, title, time, recurrence, alarm, notes ->
                                viewModel.addSchedule(type, title, time, recurrence, alarm, notes)
                            },
                            onDeleteSchedule = { viewModel.deleteSchedule(it) }
                        )
                    }

                    NavigationTab.TUGAS -> {
                        TaskScreen(
                            tasks = tasks,
                            onAddTask = { title, notes, start, deadline, alarm ->
                                viewModel.addTask(title, notes, start, deadline, alarm)
                            },
                            onUpdateStatus = { id, status -> viewModel.updateTaskStatus(id, status) },
                            onDeleteTask = { viewModel.deleteTask(it) }
                        )
                    }

                    NavigationTab.PROFIL -> {
                        ProfileScreen(
                            userAccount = userAccount,
                            deviceId = viewModel.deviceId,
                            onUpdateProfile = { name, avatarId, notifyExpense, notifyIncome, notifyAlarm ->
                                viewModel.updateProfile(name, avatarId, notifyExpense, notifyIncome, notifyAlarm)
                            }
                        )
                    }
                }
            }
        }
    }
}
