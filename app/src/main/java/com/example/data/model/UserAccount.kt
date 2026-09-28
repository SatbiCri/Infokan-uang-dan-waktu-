package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey val id: Int = 1,
    val deviceId: String,
    val isActivated: Boolean = false,
    val activationKey: String = "",
    val userName: String = "Anak Kost Mandiri",
    val avatarId: Int = 0,
    val cashBalance: Long = 250000L,
    val debitBalance: Long = 1750000L,
    val notifyExpenseSound: Boolean = true,
    val notifyIncomeSound: Boolean = true,
    val notifyAlarmSound: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
