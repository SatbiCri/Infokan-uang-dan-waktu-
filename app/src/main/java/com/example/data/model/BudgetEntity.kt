package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BudgetPeriod {
    HARIAN,
    BULANAN
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val period: BudgetPeriod = BudgetPeriod.BULANAN,
    val limitAmount: Long,
    val notes: String = "Tetapkan anggaran untuk mengontrol pengeluaran"
)
