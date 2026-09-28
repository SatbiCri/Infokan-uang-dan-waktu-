package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE monthYear = :monthYear ORDER BY timestamp DESC")
    fun getTransactionsByMonth(monthYear: String): Flow<List<TransactionEntity>>

    @Query("SELECT DISTINCT monthYear FROM transactions ORDER BY timestamp DESC")
    fun getDistinctMonths(): Flow<List<String>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTransactionsDirect(limit: Int): List<TransactionEntity>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'PENGELUARAN' AND timestamp >= :startOfDay")
    fun getTodayExpenses(startOfDay: Long): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'PENGELUARAN' AND timestamp >= :startOfMonth")
    fun getMonthExpenses(startOfMonth: Long): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'PENGELUARAN' AND category = :category AND timestamp >= :startTime")
    suspend fun getCategoryExpenseSince(category: String, startTime: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
}
