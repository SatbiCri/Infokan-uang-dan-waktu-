package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AutoResizingCurrencyText
import com.example.ui.theme.*
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    userAccount: UserAccount?,
    transactions: List<TransactionEntity>,
    distinctMonths: List<String>,
    selectedMonth: String,
    onSelectMonth: (String) -> Unit,
    todayExpenses: Long,
    monthExpenses: Long,
    budgets: List<BudgetEntity>,
    budgetSpending: Map<String, Long>,
    onUpdateBalances: (Long, Long) -> Unit,
    onAddTransaction: (TransactionType, Long, AccountType, String, String, String, Long) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAddBudget: (String, BudgetPeriod, Long) -> Unit,
    onDeleteBudget: (Long) -> Unit
) {
    var showBalanceDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedTransactionType by remember { mutableStateOf(TransactionType.PENGELUARAN) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val cashBalance = userAccount?.cashBalance ?: 0L
    val debitBalance = userAccount?.debitBalance ?: 0L
    val totalBalance = cashBalance + debitBalance

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedTransactionType = TransactionType.PENGELUARAN
                    showTransactionDialog = true
                },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Tambah") },
                text = { Text("Catat Transaksi", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_transaction")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // BRImo / GoPay inspired Blue Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(BluePrimary, BluePrimaryVariant)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        // Top Greeting & Total Saldo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Halo, ${userAccount?.userName ?: "Anak Kost"}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Total Saldo Anda",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            FilledTonalButton(
                                onClick = { showBalanceDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("edit_balance_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Atur Saldo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large Responsive Total Saldo Text
                        AutoResizingCurrencyText(
                            amount = totalBalance,
                            color = Color.White,
                            baseFontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            testTag = "total_balance_text",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // 2 Cards: Cash and Debit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cash Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(GreenSuccessContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = GreenSuccess,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Uang Cash",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondaryLight
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    AutoResizingCurrencyText(
                                        amount = cashBalance,
                                        color = TextPrimaryLight,
                                        baseFontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        testTag = "cash_balance_text",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Debit Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(BlueLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = BluePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Kartu Debit",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondaryLight
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    AutoResizingCurrencyText(
                                        amount = debitBalance,
                                        color = TextPrimaryLight,
                                        baseFontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        testTag = "debit_balance_text",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions: Pengeluaran, Pemasukan, Tarik/Setor Tunai
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-14).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.TrendingDown,
                            label = "Pengeluaran",
                            color = RedExpense,
                            bgColor = RedExpenseContainer,
                            tag = "quick_expense_btn",
                            onClick = {
                                selectedTransactionType = TransactionType.PENGELUARAN
                                showTransactionDialog = true
                            }
                        )

                        QuickActionButton(
                            icon = Icons.Default.TrendingUp,
                            label = "Pemasukan",
                            color = GreenSuccess,
                            bgColor = GreenSuccessContainer,
                            tag = "quick_income_btn",
                            onClick = {
                                selectedTransactionType = TransactionType.PEMASUKAN
                                showTransactionDialog = true
                            }
                        )

                        QuickActionButton(
                            icon = Icons.AutoMirrored.Filled.CompareArrows,
                            label = "Tarik / Setor",
                            color = BluePrimary,
                            bgColor = BlueLight,
                            tag = "quick_transfer_btn",
                            onClick = {
                                selectedTransactionType = TransactionType.TARIK_TUNAI
                                showTransactionDialog = true
                            }
                        )
                    }
                }
            }

            // Today's & Month's Spending Summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExpenseSummaryCard(
                        title = "Pengeluaran Hari Ini",
                        amount = todayExpenses,
                        icon = Icons.Default.Today,
                        accentColor = OrangeWarning,
                        modifier = Modifier.weight(1f)
                    )

                    ExpenseSummaryCard(
                        title = "Pengeluaran Bulan Ini",
                        amount = monthExpenses,
                        icon = Icons.Default.CalendarMonth,
                        accentColor = BluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Penetapan Anggaran (Budget Section with Enhanced Progress Bar)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Penetapan Anggaran",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Tetapkan anggaran untuk mengontrol pengeluaran",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { showBudgetDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = BlueLight,
                                contentColor = BluePrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_budget_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (budgets.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Belum ada anggaran yang ditetapkan.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mulai atur batas belanja harian/bulanan agar uang tidak cepat habis!",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    } else {
                        // Master Overall Budget Progress Bar Card
                        val totalBudgetLimit = budgets.sumOf { it.limitAmount }
                        val totalBudgetSpent = budgets.sumOf { budgetSpending[it.category] ?: 0L }
                        val totalProgress = if (totalBudgetLimit > 0) {
                            (totalBudgetSpent.toFloat() / totalBudgetLimit.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        val totalPercent = if (totalBudgetLimit > 0) {
                            ((totalBudgetSpent.toDouble() / totalBudgetLimit.toDouble()) * 100).toInt()
                        } else 0
                        val isTotalExceeded = totalBudgetSpent >= totalBudgetLimit

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTotalExceeded) RedExpenseContainer.copy(alpha = 0.5f) else BlueLight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PieChart,
                                            contentDescription = null,
                                            tint = if (isTotalExceeded) RedExpense else BluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Total Proses Anggaran",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isTotalExceeded) RedExpense else BluePrimaryVariant
                                        )
                                    }

                                    Surface(
                                        color = if (isTotalExceeded) RedExpense else if (totalPercent > 80) OrangeWarning else BluePrimary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "$totalPercent% Terpakai",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Master Progress Bar
                                LinearProgressIndicator(
                                    progress = { totalProgress },
                                    color = if (isTotalExceeded) RedExpense else if (totalPercent > 80) OrangeWarning else BluePrimary,
                                    trackColor = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Terpakai: ${CurrencyUtils.formatRupiah(totalBudgetSpent)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Batas Total: ${CurrencyUtils.formatRupiah(totalBudgetLimit)}",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight
                                    )
                                }

                                val remainingTotal = totalBudgetLimit - totalBudgetSpent
                                if (remainingTotal >= 0) {
                                    Text(
                                        text = "Sisa Dana Anggaran: ${CurrencyUtils.formatRupiah(remainingTotal)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenSuccess,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                } else {
                                    Text(
                                        text = "⚠️ Anggaran Melebihi Batas Sebesar: ${CurrencyUtils.formatRupiah(-remainingTotal)}!",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedExpense,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        // Individual Category Progress Bars
                        budgets.forEach { budget ->
                            val spent = budgetSpending[budget.category] ?: 0L
                            val progress = if (budget.limitAmount > 0) {
                                (spent.toFloat() / budget.limitAmount.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                            val percent = if (budget.limitAmount > 0) {
                                ((spent.toDouble() / budget.limitAmount.toDouble()) * 100).toInt()
                            } else 0
                            val isExceeded = spent >= budget.limitAmount

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isExceeded) RedExpenseContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = budget.category,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = if (budget.period == BudgetPeriod.HARIAN) OrangeContainer else BlueLight,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (budget.period == BudgetPeriod.HARIAN) "Harian" else "Bulanan",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (budget.period == BudgetPeriod.HARIAN) OrangeWarning else BluePrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (isExceeded) RedExpenseContainer else if (percent > 75) OrangeContainer else GreenSuccessContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "$percent%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isExceeded) RedExpense else if (percent > 75) OrangeWarning else GreenSuccess,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteBudget(budget.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus Anggaran",
                                                    tint = TextSecondaryLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Category progress bar
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = if (isExceeded) RedExpense else if (progress > 0.75f) OrangeWarning else BluePrimary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(9.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Terpakai: ${CurrencyUtils.formatRupiah(spent)}",
                                            fontSize = 11.sp,
                                            color = if (isExceeded) RedExpense else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isExceeded) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "Batas: ${CurrencyUtils.formatRupiah(budget.limitAmount)}",
                                            fontSize = 11.sp,
                                            color = TextSecondaryLight
                                        )
                                    }

                                    val remaining = budget.limitAmount - spent
                                    if (isExceeded) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "⚠️ Anggaran penuh! Melebihi: ${CurrencyUtils.formatRupiah(-remaining)}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RedExpense
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Tersisa: ${CurrencyUtils.formatRupiah(remaining)}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GreenSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Riwayat Transaksi per Bulan
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Riwayat Transaksi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tercatat rapi dan terbagi otomatis menurut bulan",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Month filter tabs
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMonth.isBlank(),
                            onClick = { onSelectMonth("") },
                            label = { Text("Semua Bulan") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    items(distinctMonths) { month ->
                        FilterChip(
                            selected = selectedMonth == month,
                            onClick = { onSelectMonth(month) },
                            label = { Text(month) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Transactions list
            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada transaksi di bulan ini",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { trx ->
                    TransactionItemCard(
                        transaction = trx,
                        onDelete = { onDeleteTransaction(trx) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
            }
        }
    }

    // Dialogs
    if (showBalanceDialog) {
        SetBalanceDialog(
            currentCash = cashBalance,
            currentDebit = debitBalance,
            onDismiss = { showBalanceDialog = false },
            onSave = { newCash, newDebit ->
                onUpdateBalances(newCash, newDebit)
                showBalanceDialog = false
            }
        )
    }

    if (showTransactionDialog) {
        AddTransactionDialog(
            initialType = selectedTransactionType,
            onDismiss = { showTransactionDialog = false },
            onSave = { type, amount, accountType, category, bank, notes, time ->
                onAddTransaction(type, amount, accountType, category, bank, notes, time)
                showTransactionDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showBudgetDialog = false },
            onSave = { category, period, amount ->
                onAddBudget(category, period, amount)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    bgColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ExpenseSummaryCard(
    title: String,
    amount: Long,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            AutoResizingCurrencyText(
                amount = amount,
                color = MaterialTheme.colorScheme.onSurface,
                baseFontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.PENGELUARAN
    val isIncome = transaction.type == TransactionType.PEMASUKAN
    val isTransfer = transaction.type == TransactionType.TARIK_TUNAI || transaction.type == TransactionType.SETOR_TUNAI

    val icon = when {
        isExpense -> Icons.Default.TrendingDown
        isIncome -> Icons.Default.TrendingUp
        transaction.type == TransactionType.TARIK_TUNAI -> Icons.Default.LocalAtm
        else -> Icons.Default.AccountBalance
    }

    val iconColor = when {
        isExpense -> RedExpense
        isIncome -> GreenSuccess
        else -> BluePrimary
    }

    val iconBg = when {
        isExpense -> RedExpenseContainer
        isIncome -> GreenSuccessContainer
        else -> BlueLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (transaction.accountType == AccountType.CASH) GreenSuccessContainer else BlueLight,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isTransfer) {
                                if (transaction.type == TransactionType.TARIK_TUNAI) "Debit ➔ Cash" else "Cash ➔ Debit"
                            } else {
                                if (transaction.accountType == AccountType.CASH) "Cash" else "Kartu Debit"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.accountType == AccountType.CASH) GreenSuccess else BluePrimary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (transaction.bankName.isNotBlank()) {
                    Text(
                        text = "Bank: ${transaction.bankName}",
                        fontSize = 10.sp,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (transaction.notes.isNotBlank()) {
                    Text(
                        text = transaction.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = DateTimeUtils.formatDateTime(transaction.timestamp),
                    fontSize = 10.sp,
                    color = TextSecondaryLight
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (isExpense) "- " else if (isIncome) "+ " else ""
                val amountColor = if (isExpense) RedExpense else if (isIncome) GreenSuccess else BluePrimary

                Text(
                    text = "$prefix${CurrencyUtils.formatRupiah(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = amountColor
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ----------------- DIALOGS -----------------

@Composable
fun SetBalanceDialog(
    currentCash: Long,
    currentDebit: Long,
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit
) {
    var cashStr by remember { mutableStateOf(currentCash.toString()) }
    var debitStr by remember { mutableStateOf(currentDebit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Atur Saldo Cash & Debit", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column {
                Text(
                    text = "Perbarui nominal saldo tunai dan kartu debit Anda sesuai keadaan riil.",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = cashStr,
                    onValueChange = { cashStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Uang Cash (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_input_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = debitStr,
                    onValueChange = { debitStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Kartu Debit (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debit_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cash = cashStr.toLongOrNull() ?: 0L
                    val debit = debitStr.toLongOrNull() ?: 0L
                    onSave(cash, debit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Long, AccountType, String, String, String, Long) -> Unit
) {
    var transactionType by remember { mutableStateOf(initialType) }
    var amountStr by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.CASH) }
    var category by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var transferType by remember { mutableStateOf(TransactionType.TARIK_TUNAI) } // Debit->Cash or Cash->Debit
    val timestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    val expenseCategories = listOf(
        "Makan dan Minuman",
        "Transportasi",
        "Kebutuhan Kuliah",
        "Belanja",
        "Lain-lain"
    )

    val incomeCategories = listOf(
        "Uang Saku",
        "Uang Orang Tua",
        "Gaji/Pemasukan",
        "Hadiah",
        "Lain-lain"
    )

    // Set default category
    LaunchedEffect(transactionType) {
        if (transactionType == TransactionType.PENGELUARAN) {
            category = expenseCategories[0]
        } else if (transactionType == TransactionType.PEMASUKAN) {
            category = incomeCategories[0]
        } else {
            category = if (transferType == TransactionType.TARIK_TUNAI) "Tarik Tunai" else "Setor Tunai"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Catat Transaksi Real-Time", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = transactionType == TransactionType.PENGELUARAN,
                        onClick = { transactionType = TransactionType.PENGELUARAN },
                        label = { Text("Pengeluaran", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == TransactionType.PEMASUKAN,
                        onClick = { transactionType = TransactionType.PEMASUKAN },
                        label = { Text("Pemasukan", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == TransactionType.TARIK_TUNAI || transactionType == TransactionType.SETOR_TUNAI,
                        onClick = { transactionType = transferType },
                        label = { Text("Tarik/Setor", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Date and Time display
                Surface(
                    color = BlueLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Waktu Real-time: ${DateTimeUtils.formatDateTime(timestamp)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BluePrimaryVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nominal
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Uang (Rp)") },
                    placeholder = { Text("contoh: 25000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trx_amount_input")
                )

                if (amountStr.isNotBlank()) {
                    val preview = amountStr.toLongOrNull() ?: 0L
                    Text(
                        text = "Format: ${CurrencyUtils.formatRupiah(preview)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // If Pengeluaran or Pemasukan: Cash vs Debit selector
                if (transactionType == TransactionType.PENGELUARAN || transactionType == TransactionType.PEMASUKAN) {
                    Text(
                        text = if (transactionType == TransactionType.PENGELUARAN) "Metode Pembayaran:" else "Tujuan Masuk:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = accountType == AccountType.CASH,
                            onClick = { accountType = AccountType.CASH },
                            label = { Text("💵 Uang Cash") }
                        )
                        FilterChip(
                            selected = accountType == AccountType.DEBIT,
                            onClick = { accountType = AccountType.DEBIT },
                            label = { Text("💳 Kartu Debit") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Pilih Kategori:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val catList = if (transactionType == TransactionType.PENGELUARAN) expenseCategories else incomeCategories

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(catList) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                } else {
                    // Tarik / Setor Tunai
                    Text(text = "Jenis Tarik / Setor:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = transferType == TransactionType.TARIK_TUNAI,
                            onClick = {
                                transferType = TransactionType.TARIK_TUNAI
                                transactionType = TransactionType.TARIK_TUNAI
                                category = "Tarik Tunai"
                            },
                            label = { Text("Tarik Tunai (Debit ➔ Cash)") }
                        )
                        FilterChip(
                            selected = transferType == TransactionType.SETOR_TUNAI,
                            onClick = {
                                transferType = TransactionType.SETOR_TUNAI
                                transactionType = TransactionType.SETOR_TUNAI
                                category = "Setor Tunai"
                            },
                            label = { Text("Setor Tunai (Cash ➔ Debit)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Nama Bank / ATM (Tujuan/Asal)") },
                        placeholder = { Text("contoh: BCA, BRI, Mandiri, BNI") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Pengeluaran/Pemasukan") },
                    placeholder = { Text("contoh: Nasi padang + es teh") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        val finalType = if (transactionType == TransactionType.PENGELUARAN || transactionType == TransactionType.PEMASUKAN) {
                            transactionType
                        } else {
                            transferType
                        }
                        onSave(finalType, amount, accountType, category, bankName, notes, timestamp)
                    }
                },
                enabled = amountStr.isNotBlank() && (amountStr.toLongOrNull() ?: 0L) > 0L,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (String, BudgetPeriod, Long) -> Unit
) {
    val categories = listOf(
        "Makan dan Minuman",
        "Transportasi",
        "Kebutuhan Kuliah",
        "Belanja",
        "Lain-lain"
    )
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.BULANAN) }
    var limitStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tetapkan Anggaran Baru", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column {
                Text(
                    text = "Tetapkan anggaran untuk mengontrol pengeluaran. Anda akan mendapat alarm/notifikasi saat anggaran telah penuh atau lewat.",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Periode Anggaran:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedPeriod == BudgetPeriod.HARIAN,
                        onClick = { selectedPeriod = BudgetPeriod.HARIAN },
                        label = { Text("Harian") }
                    )
                    FilterChip(
                        selected = selectedPeriod == BudgetPeriod.BULANAN,
                        onClick = { selectedPeriod = BudgetPeriod.BULANAN },
                        label = { Text("Bulanan") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Batas Maksimal Anggaran (Rp)") },
                    placeholder = { Text("contoh: 500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (limitStr.isNotBlank()) {
                    val preview = limitStr.toLongOrNull() ?: 0L
                    Text(
                        text = "Batas: ${CurrencyUtils.formatRupiah(preview)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitStr.toLongOrNull() ?: 0L
                    if (limit > 0) {
                        onSave(selectedCategory, selectedPeriod, limit)
                    }
                },
                enabled = limitStr.isNotBlank() && (limitStr.toLongOrNull() ?: 0L) > 0L,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan Anggaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
