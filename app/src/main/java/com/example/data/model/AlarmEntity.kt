package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,          // 0 - 23
    val minute: Int,        // 0 - 59
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: String = "1,2,3,4,5,6,7", // comma separated day-of-week numbers: 1=Senin, 7=Minggu
    val vibrate: Boolean = true,
    val soundUri: String = ""
) {
    fun getFormattedTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun getRepeatDaysLabel(): String {
        if (repeatDays.isBlank()) return "Sekali saja"
        val days = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        if (days.size == 7) return "Setiap hari"
        if (days == setOf(1, 2, 3, 4, 5)) return "Senin - Jumat"
        if (days == setOf(6, 7)) return "Akhir pekan (Sabtu - Minggu)"
        val dayNames = mapOf(
            1 to "Sen",
            2 to "Sel",
            3 to "Rab",
            4 to "Kam",
            5 to "Jum",
            6 to "Sab",
            7 to "Min"
        )
        return days.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
}
