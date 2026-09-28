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
    categories: List<CategoryEntity>,
    budgetAlert: String?,
    onClearBudgetAlert: () -> Unit,
    onUpdateBalances: (Long, Long) -> Unit,
    onAddTransaction: (TransactionType, Long, AccountType, String, String, String, Long, String) -> Unit,
    onUpdateTransaction: (TransactionEntity, TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAddBudget: (String, BudgetPeriod, Long) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddCategory: (String, String, TransactionType) -> Unit,
    onDeleteCategory: (Long) -> Unit
) {
    var showBalanceDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedTransactionType by remember { mutableStateOf(TransactionType.PENGELUARAN) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }

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

                        QuickActionButton(
                            icon = Icons.Default.Category,
                            label = "Kategori",
                            color = Color(0xFF673AB7),
                            bgColor = Color(0xFFEDE7F6),
                            tag = "quick_category_btn",
                            onClick = {
                                showCategoryManagerDialog = true
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
                        onEdit = { editingTransaction = trx },
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
            categories = categories,
            onDismiss = { showTransactionDialog = false },
            onSave = { type, amount, accountType, category, bank, notes, time, subCat ->
                onAddTransaction(type, amount, accountType, category, bank, notes, time, subCat)
                showTransactionDialog = false
            },
            onAddCategory = onAddCategory
        )
    }

    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!,
            categories = categories,
            onDismiss = { editingTransaction = null },
            onSave = { updatedTrx ->
                onUpdateTransaction(editingTransaction!!, updatedTrx)
                editingTransaction = null
            },
            onAddCategory = onAddCategory
        )
    }

    if (showBudgetDialog) {
        AddBudgetDialog(
            categories = categories,
            onDismiss = { showBudgetDialog = false },
            onSave = { category, period, amount ->
                onAddBudget(category, period, amount)
                showBudgetDialog = false
            }
        )
    }

    if (showCategoryManagerDialog) {
        CategoryManagementDialog(
            categories = categories,
            onDismiss = { showCategoryManagerDialog = false },
            onAddCategory = onAddCategory,
            onDeleteCategory = onDeleteCategory
        )
    }

    if (budgetAlert != null) {
        AlertDialog(
            onDismissRequest = onClearBudgetAlert,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RedExpense) },
            title = { Text("Peringatan Anggaran!", fontWeight = FontWeight.Bold, color = RedExpense) },
            text = { Text(budgetAlert) },
            confirmButton = {
                Button(
                    onClick = onClearBudgetAlert,
                    colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
                ) {
                    Text("Saya Mengerti")
                }
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
    onEdit: () -> Unit,
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
                    val categoryDisplay = if (transaction.subCategory.isNotBlank()) {
                        "${transaction.category} • ${transaction.subCategory}"
                    } else {
                        transaction.category
                    }
                    Text(
                        text = categoryDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
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

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (isExpense) "- " else if (isIncome) "+ " else ""
                val amountColor = if (isExpense) RedExpense else if (isIncome) GreenSuccess else BluePrimary

                Text(
                    text = "$prefix${CurrencyUtils.formatRupiah(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Transaksi",
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
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
}
