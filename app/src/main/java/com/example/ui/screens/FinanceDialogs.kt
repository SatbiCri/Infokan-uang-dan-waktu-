package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import java.util.Calendar

@Composable
fun SetBalanceDialog(
    currentCash: Long,
    currentDebit: Long,
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit
) {
    // Empty state if zero so user can type from blank
    var cashDigits by remember { mutableStateOf(if (currentCash > 0) currentCash.toString() else "") }
    var debitDigits by remember { mutableStateOf(if (currentDebit > 0) currentDebit.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Atur Saldo Awal & Riil", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column {
                Text(
                    text = "Ketikkan nominal saldo tunai dan kartu debit Anda. Format Rupiah otomatis muncul saat Anda mengetik.",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = CurrencyUtils.formatInputAsRupiah(cashDigits),
                    onValueChange = { input ->
                        cashDigits = input.filter { it.isDigit() }
                    },
                    label = { Text("Jumlah Uang Cash") },
                    placeholder = { Text("Rp 0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_input_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = CurrencyUtils.formatInputAsRupiah(debitDigits),
                    onValueChange = { input ->
                        debitDigits = input.filter { it.isDigit() }
                    },
                    label = { Text("Jumlah Saldo Kartu Debit") },
                    placeholder = { Text("Rp 0") },
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
                    val cash = cashDigits.toLongOrNull() ?: 0L
                    val debit = debitDigits.toLongOrNull() ?: 0L
                    onSave(cash, debit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan Saldo")
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
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Long, AccountType, String, String, String, Long, String) -> Unit,
    onAddCategory: (String, String, TransactionType) -> Unit
) {
    val context = LocalContext.current
    var transactionType by remember { mutableStateOf(initialType) }
    var amountDigits by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.CASH) }
    var transferType by remember { mutableStateOf(TransactionType.TARIK_TUNAI) }
    var bankName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var selectedMainCategory by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf("") }
    var customSubCategoryInput by remember { mutableStateOf("") }

    var showNewCategoryDialog by remember { mutableStateOf(false) }

    // Filter categories by transaction type
    val typeCategories = remember(categories, transactionType) {
        val targetType = if (transactionType == TransactionType.PENGELUARAN) TransactionType.PENGELUARAN else TransactionType.PEMASUKAN
        categories.filter { it.type == targetType }
    }

    val mainCategories = remember(typeCategories) {
        val list = typeCategories.map { it.mainCategory }.distinct()
        if (list.isEmpty()) {
            if (transactionType == TransactionType.PENGELUARAN) {
                listOf("Makan dan Minuman", "Transportasi", "Kebutuhan Kuliah", "Kebutuhan Kost", "Lain-lain")
            } else {
                listOf("Uang Saku", "Gaji/Pemasukan", "Beasiswa", "Lain-lain")
            }
        } else list
    }

    LaunchedEffect(mainCategories) {
        if (selectedMainCategory.isBlank() || !mainCategories.contains(selectedMainCategory)) {
            selectedMainCategory = mainCategories.firstOrNull() ?: "Lain-lain"
        }
    }

    val subCategories = remember(typeCategories, selectedMainCategory) {
        typeCategories.filter { it.mainCategory == selectedMainCategory }.map { it.subCategory }.distinct()
    }

    LaunchedEffect(subCategories) {
        if (subCategories.isNotEmpty() && (selectedSubCategory.isBlank() || !subCategories.contains(selectedSubCategory))) {
            selectedSubCategory = subCategories.first()
        }
    }

    // Date & Time pickers
    val calendar = remember(selectedTimestamp) {
        Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Catat Transaksi", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selector chips
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

                // Date & Time Selector (Interactive Backdating)
                Surface(
                    color = BlueLight,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = DateTimeUtils.formatDateTime(selectedTimestamp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimaryVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AssistChip(
                                onClick = {
                                    val currentCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val c = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                            c.set(Calendar.YEAR, y)
                                            c.set(Calendar.MONTH, m)
                                            c.set(Calendar.DAY_OF_MONTH, d)
                                            selectedTimestamp = c.timeInMillis
                                        },
                                        currentCal.get(Calendar.YEAR),
                                        currentCal.get(Calendar.MONTH),
                                        currentCal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                label = { Text("Ubah Tanggal", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.weight(1f)
                            )

                            AssistChip(
                                onClick = {
                                    val currentCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                    TimePickerDialog(
                                        context,
                                        { _, h, min ->
                                            val c = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                            c.set(Calendar.HOUR_OF_DAY, h)
                                            c.set(Calendar.MINUTE, min)
                                            selectedTimestamp = c.timeInMillis
                                        },
                                        currentCal.get(Calendar.HOUR_OF_DAY),
                                        currentCal.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                label = { Text("Ubah Jam", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.weight(1f)
                            )

                            AssistChip(
                                onClick = { selectedTimestamp = System.currentTimeMillis() },
                                label = { Text("Sekarang", fontSize = 10.sp) },
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time formatted Rupiah nominal input
                OutlinedTextField(
                    value = CurrencyUtils.formatInputAsRupiah(amountDigits),
                    onValueChange = { input ->
                        amountDigits = input.filter { it.isDigit() }
                    },
                    label = { Text("Nominal Uang (Rp)") },
                    placeholder = { Text("Rp 0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trx_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Method / Source account
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

                    // Main Category Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Kategori Utama:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        TextButton(
                            onClick = { showNewCategoryDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Kategori", fontSize = 11.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(mainCategories) { cat ->
                            FilterChip(
                                selected = selectedMainCategory == cat,
                                onClick = {
                                    selectedMainCategory = cat
                                    selectedSubCategory = ""
                                },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Sub-Category Selection
                    if (subCategories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Sub-Kategori ($selectedMainCategory):", fontSize = 11.sp, color = TextSecondaryLight)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(subCategories) { sub ->
                                FilterChip(
                                    selected = selectedSubCategory == sub && customSubCategoryInput.isBlank(),
                                    onClick = {
                                        selectedSubCategory = sub
                                        customSubCategoryInput = ""
                                    },
                                    label = { Text(sub, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // Optional custom sub-category / detail
                    OutlinedTextField(
                        value = customSubCategoryInput,
                        onValueChange = {
                            customSubCategoryInput = it
                            if (it.isNotBlank()) selectedSubCategory = it
                        },
                        label = { Text("Ketik Sub-Kategori / Detail Spesifik (Opsional)") },
                        placeholder = { Text("contoh: Makan Siang, Bensin, Fotokopi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                } else {
                    // Tarik / Setor Tunai
                    Text(text = "Jenis Tarik / Setor:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = transferType == TransactionType.TARIK_TUNAI,
                            onClick = {
                                transferType = TransactionType.TARIK_TUNAI
                                transactionType = TransactionType.TARIK_TUNAI
                            },
                            label = { Text("Tarik Tunai (Debit ➔ Cash)") }
                        )
                        FilterChip(
                            selected = transferType == TransactionType.SETOR_TUNAI,
                            onClick = {
                                transferType = TransactionType.SETOR_TUNAI
                                transactionType = TransactionType.SETOR_TUNAI
                            },
                            label = { Text("Setor Tunai (Cash ➔ Debit)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Nama Bank / ATM (Asal / Tujuan)") },
                        placeholder = { Text("contoh: BCA, BRI, Mandiri, BNI") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes Field (Responsive, limited lines with counter to prevent layout overflow)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 150) notes = it },
                    label = { Text("Catatan Pengeluaran/Pemasukan (Opsional)") },
                    placeholder = { Text("contoh: Nasi padang + es teh") },
                    maxLines = 3,
                    minLines = 2,
                    supportingText = {
                        Text(
                            text = "${notes.length}/150 karakter",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            fontSize = 10.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountDigits.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        val finalType = if (transactionType == TransactionType.PENGELUARAN || transactionType == TransactionType.PEMASUKAN) {
                            transactionType
                        } else {
                            transferType
                        }
                        val category = if (finalType == TransactionType.TARIK_TUNAI) {
                            "Tarik Tunai"
                        } else if (finalType == TransactionType.SETOR_TUNAI) {
                            "Setor Tunai"
                        } else {
                            selectedMainCategory.ifBlank { "Lain-lain" }
                        }
                        val subCat = if (finalType == TransactionType.PENGELUARAN || finalType == TransactionType.PEMASUKAN) {
                            customSubCategoryInput.ifBlank { selectedSubCategory }
                        } else ""

                        onSave(finalType, amount, accountType, category, bankName, notes, selectedTimestamp, subCat)
                    }
                },
                enabled = amountDigits.isNotBlank() && (amountDigits.toLongOrNull() ?: 0L) > 0L,
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

    if (showNewCategoryDialog) {
        CreateCategoryDialog(
            defaultType = transactionType,
            onDismiss = { showNewCategoryDialog = false },
            onSave = { main, sub, type ->
                onAddCategory(main, sub, type)
                selectedMainCategory = main
                selectedSubCategory = sub
                showNewCategoryDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onAddCategory: (String, String, TransactionType) -> Unit
) {
    val context = LocalContext.current
    var transactionType by remember { mutableStateOf(transaction.type) }
    var amountDigits by remember { mutableStateOf(transaction.amount.toString()) }
    var accountType by remember { mutableStateOf(transaction.accountType) }
    var transferType by remember {
        mutableStateOf(if (transaction.type == TransactionType.SETOR_TUNAI) TransactionType.SETOR_TUNAI else TransactionType.TARIK_TUNAI)
    }
    var bankName by remember { mutableStateOf(transaction.bankName) }
    var notes by remember { mutableStateOf(transaction.notes) }
    var selectedTimestamp by remember { mutableLongStateOf(transaction.timestamp) }

    var selectedMainCategory by remember { mutableStateOf(transaction.category) }
    var selectedSubCategory by remember { mutableStateOf(transaction.subCategory) }
    var customSubCategoryInput by remember { mutableStateOf(transaction.subCategory) }

    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val typeCategories = remember(categories, transactionType) {
        val targetType = if (transactionType == TransactionType.PENGELUARAN) TransactionType.PENGELUARAN else TransactionType.PEMASUKAN
        categories.filter { it.type == targetType }
    }

    val mainCategories = remember(typeCategories) {
        val list = typeCategories.map { it.mainCategory }.distinct()
        if (list.isEmpty()) {
            if (transactionType == TransactionType.PENGELUARAN) {
                listOf("Makan dan Minuman", "Transportasi", "Kebutuhan Kuliah", "Kebutuhan Kost", "Lain-lain")
            } else {
                listOf("Uang Saku", "Gaji/Pemasukan", "Beasiswa", "Lain-lain")
            }
        } else list
    }

    val subCategories = remember(typeCategories, selectedMainCategory) {
        typeCategories.filter { it.mainCategory == selectedMainCategory }.map { it.subCategory }.distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Transaksi", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selector chips
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

                // Date & Time Selector (Interactive Backdating)
                Surface(
                    color = BlueLight,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = DateTimeUtils.formatDateTime(selectedTimestamp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimaryVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AssistChip(
                                onClick = {
                                    val currentCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val c = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                            c.set(Calendar.YEAR, y)
                                            c.set(Calendar.MONTH, m)
                                            c.set(Calendar.DAY_OF_MONTH, d)
                                            selectedTimestamp = c.timeInMillis
                                        },
                                        currentCal.get(Calendar.YEAR),
                                        currentCal.get(Calendar.MONTH),
                                        currentCal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                label = { Text("Ubah Tanggal", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.weight(1f)
                            )

                            AssistChip(
                                onClick = {
                                    val currentCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                    TimePickerDialog(
                                        context,
                                        { _, h, min ->
                                            val c = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                            c.set(Calendar.HOUR_OF_DAY, h)
                                            c.set(Calendar.MINUTE, min)
                                            selectedTimestamp = c.timeInMillis
                                        },
                                        currentCal.get(Calendar.HOUR_OF_DAY),
                                        currentCal.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                label = { Text("Ubah Jam", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time formatted Rupiah nominal input
                OutlinedTextField(
                    value = CurrencyUtils.formatInputAsRupiah(amountDigits),
                    onValueChange = { input ->
                        amountDigits = input.filter { it.isDigit() }
                    },
                    label = { Text("Nominal Uang (Rp)") },
                    placeholder = { Text("Rp 0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Method / Source account
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

                    // Main Category Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Kategori Utama:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        TextButton(
                            onClick = { showNewCategoryDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Kategori", fontSize = 11.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(mainCategories) { cat ->
                            FilterChip(
                                selected = selectedMainCategory == cat,
                                onClick = {
                                    selectedMainCategory = cat
                                    selectedSubCategory = ""
                                },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Sub-Category Selection
                    if (subCategories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Sub-Kategori ($selectedMainCategory):", fontSize = 11.sp, color = TextSecondaryLight)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(subCategories) { sub ->
                                FilterChip(
                                    selected = selectedSubCategory == sub && customSubCategoryInput.isBlank(),
                                    onClick = {
                                        selectedSubCategory = sub
                                        customSubCategoryInput = ""
                                    },
                                    label = { Text(sub, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customSubCategoryInput,
                        onValueChange = {
                            customSubCategoryInput = it
                            if (it.isNotBlank()) selectedSubCategory = it
                        },
                        label = { Text("Ketik Sub-Kategori / Detail Spesifik (Opsional)") },
                        placeholder = { Text("contoh: Makan Siang, Bensin, Fotokopi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                } else {
                    // Tarik / Setor Tunai
                    Text(text = "Jenis Tarik / Setor:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = transferType == TransactionType.TARIK_TUNAI,
                            onClick = {
                                transferType = TransactionType.TARIK_TUNAI
                                transactionType = TransactionType.TARIK_TUNAI
                            },
                            label = { Text("Tarik Tunai (Debit ➔ Cash)") }
                        )
                        FilterChip(
                            selected = transferType == TransactionType.SETOR_TUNAI,
                            onClick = {
                                transferType = TransactionType.SETOR_TUNAI
                                transactionType = TransactionType.SETOR_TUNAI
                            },
                            label = { Text("Setor Tunai (Cash ➔ Debit)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Nama Bank / ATM (Asal / Tujuan)") },
                        placeholder = { Text("contoh: BCA, BRI, Mandiri, BNI") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 150) notes = it },
                    label = { Text("Catatan Pengeluaran/Pemasukan (Opsional)") },
                    placeholder = { Text("contoh: Nasi padang + es teh") },
                    maxLines = 3,
                    minLines = 2,
                    supportingText = {
                        Text(
                            text = "${notes.length}/150 karakter",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            fontSize = 10.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountDigits.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        val finalType = if (transactionType == TransactionType.PENGELUARAN || transactionType == TransactionType.PEMASUKAN) {
                            transactionType
                        } else {
                            transferType
                        }
                        val category = if (finalType == TransactionType.TARIK_TUNAI) {
                            "Tarik Tunai"
                        } else if (finalType == TransactionType.SETOR_TUNAI) {
                            "Setor Tunai"
                        } else {
                            selectedMainCategory.ifBlank { "Lain-lain" }
                        }
                        val subCat = if (finalType == TransactionType.PENGELUARAN || finalType == TransactionType.PEMASUKAN) {
                            customSubCategoryInput.ifBlank { selectedSubCategory }
                        } else ""

                        val updated = transaction.copy(
                            type = finalType,
                            amount = amount,
                            accountType = accountType,
                            category = category,
                            subCategory = subCat,
                            bankName = bankName,
                            notes = notes,
                            timestamp = selectedTimestamp
                        )
                        onSave(updated)
                    }
                },
                enabled = amountDigits.isNotBlank() && (amountDigits.toLongOrNull() ?: 0L) > 0L,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )

    if (showNewCategoryDialog) {
        CreateCategoryDialog(
            defaultType = transactionType,
            onDismiss = { showNewCategoryDialog = false },
            onSave = { main, sub, type ->
                onAddCategory(main, sub, type)
                selectedMainCategory = main
                selectedSubCategory = sub
                showNewCategoryDialog = false
            }
        )
    }
}

@Composable
fun CreateCategoryDialog(
    defaultType: TransactionType,
    onDismiss: () -> Unit,
    onSave: (String, String, TransactionType) -> Unit
) {
    var mainCategory by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(defaultType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tambah Kategori Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column {
                Text(
                    text = "Buat kategori utama dan sub-kategori khusus untuk mengelompokkan transaksi Anda secara rapi.",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.PENGELUARAN,
                        onClick = { type = TransactionType.PENGELUARAN },
                        label = { Text("Pengeluaran") }
                    )
                    FilterChip(
                        selected = type == TransactionType.PEMASUKAN,
                        onClick = { type = TransactionType.PEMASUKAN },
                        label = { Text("Pemasukan") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mainCategory,
                    onValueChange = { mainCategory = it },
                    label = { Text("Kategori Utama") },
                    placeholder = { Text("contoh: Hobi, Kebutuhan Skripsi, Usaha") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    label = { Text("Sub-Kategori") },
                    placeholder = { Text("contoh: Beli Kertas, Servis Laptop, Game") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mainCategory.isNotBlank() && subCategory.isNotBlank()) {
                        onSave(mainCategory.trim(), subCategory.trim(), type)
                    }
                },
                enabled = mainCategory.isNotBlank() && subCategory.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Tambah")
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
fun CategoryManagementDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (String, String, TransactionType) -> Unit,
    onDeleteCategory: (Long) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.PENGELUARAN) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filtered = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }
    val grouped = remember(filtered) {
        filtered.groupBy { it.mainCategory }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kelola Kategori & Sub", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                FilledTonalButton(
                    onClick = { showCreateDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 11.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = "Atur kategori dan sub-kategori pengeluaran dan pemasukan sesuai gaya hidup Anda.",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == TransactionType.PENGELUARAN,
                        onClick = { selectedType = TransactionType.PENGELUARAN },
                        label = { Text("Pengeluaran") }
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.PEMASUKAN,
                        onClick = { selectedType = TransactionType.PEMASUKAN },
                        label = { Text("Pemasukan") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (grouped.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada kategori. Tekan 'Tambah' untuk membuat.", fontSize = 12.sp, color = TextSecondaryLight)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        grouped.forEach { (main, list) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "📁 $main",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BluePrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    list.forEach { cat ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "↳ ${cat.subCategory}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = { onDeleteCategory(cat.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus",
                                                    tint = RedExpense,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Selesai")
            }
        }
    )

    if (showCreateDialog) {
        CreateCategoryDialog(
            defaultType = selectedType,
            onDismiss = { showCreateDialog = false },
            onSave = { main, sub, type ->
                onAddCategory(main, sub, type)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun AddBudgetDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, BudgetPeriod, Long) -> Unit
) {
    val expenseCategories = remember(categories) {
        val list = categories.filter { it.type == TransactionType.PENGELUARAN }.map { it.mainCategory }.distinct()
        if (list.isEmpty()) {
            listOf("Makan dan Minuman", "Transportasi", "Kebutuhan Kuliah", "Kebutuhan Kost", "Hiburan & Pribadi", "Lain-lain")
        } else list
    }

    var selectedCategory by remember { mutableStateOf(expenseCategories.firstOrNull() ?: "Makan dan Minuman") }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.BULANAN) }
    // Start empty so user types their own budget amount
    var limitDigits by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tetapkan Anggaran Baru", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column {
                Text(
                    text = "Tetapkan batas anggaran untuk mengontrol pengeluaran. Anda akan mendapat alarm dan peringatan ketika anggaran telah penuh atau melampaui batas.",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Kategori Pengeluaran:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(expenseCategories) { cat ->
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
                    value = CurrencyUtils.formatInputAsRupiah(limitDigits),
                    onValueChange = { input ->
                        limitDigits = input.filter { it.isDigit() }
                    },
                    label = { Text("Batas Maksimal Anggaran (Rp)") },
                    placeholder = { Text("Rp 0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitDigits.toLongOrNull() ?: 0L
                    if (limit > 0) {
                        onSave(selectedCategory, selectedPeriod, limit)
                    }
                },
                enabled = limitDigits.isNotBlank() && (limitDigits.toLongOrNull() ?: 0L) > 0L,
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
