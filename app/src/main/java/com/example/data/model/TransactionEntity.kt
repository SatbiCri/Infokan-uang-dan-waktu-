package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    PENGELUARAN,
    PEMASUKAN,
    TARIK_TUNAI, // Debit -> Cash
    SETOR_TUNAI  // Cash -> Debit
}

enum class AccountType {
    CASH,
    DEBIT
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val accountType: AccountType, // for Pengeluaran/Pemasukan: source/dest
    val category: String, // e.g. "Makan dan Minuman", "Uang Saku"
    val subCategory: String = "", // e.g. "Makan Siang", "Bensin"
    val bankName: String = "", // e.g. "BCA", "BRI", "Mandiri"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val monthYear: String = "" // e.g. "September 2026"
)
