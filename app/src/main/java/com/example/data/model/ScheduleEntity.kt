package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScheduleType {
    AKTIVITAS,
    ACARA_KEGIATAN
}

enum class RecurrenceType {
    SEKALI,
    HARI_YANG_SAMA_SETIAP_MINGGU,
    SETIAP_HARI
}

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ScheduleType = ScheduleType.AKTIVITAS,
    val title: String,
    val dateTimeMillis: Long,
    val recurrence: RecurrenceType = RecurrenceType.SEKALI,
    val hasAlarm: Boolean = true,
    val notes: String = ""
)
