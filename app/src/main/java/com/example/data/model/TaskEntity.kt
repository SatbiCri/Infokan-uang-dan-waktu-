package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskStatus {
    BELUM,
    SEDANG,
    SELESAI
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val startTimeMillis: Long,
    val deadlineMillis: Long,
    val status: TaskStatus = TaskStatus.BELUM,
    val hasAlarm: Boolean = true
)
