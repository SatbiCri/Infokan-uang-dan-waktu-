package com.example.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    val INDONESIAN_LOCALE: Locale = Locale.forLanguageTag("id-ID")
    val ZONE_ID = ZoneId.systemDefault()

    fun nowMillis(): Long = System.currentTimeMillis()

    fun formatDateTime(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy • HH:mm", INDONESIAN_LOCALE)
        return dt.format(formatter)
    }

    fun formatDate(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", INDONESIAN_LOCALE)
        return dt.format(formatter)
    }

    fun formatShortDate(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val formatter = DateTimeFormatter.ofPattern("dd MMM", INDONESIAN_LOCALE)
        return dt.format(formatter)
    }

    fun formatTime(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", INDONESIAN_LOCALE)
        return "${dt.format(formatter)} WIB"
    }

    fun formatTimeOnly(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", INDONESIAN_LOCALE)
        return dt.format(formatter)
    }

    fun formatDayName(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val formatter = DateTimeFormatter.ofPattern("EEEE", INDONESIAN_LOCALE)
        return dt.format(formatter)
    }

    fun getMonthYear(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", INDONESIAN_LOCALE)
        return dt.format(formatter)
    }

    fun isToday(millis: Long): Boolean {
        val target = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID).toLocalDate()
        val today = LocalDate.now(ZONE_ID)
        return target.isEqual(today)
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val d1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis1), ZONE_ID).toLocalDate()
        val d2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis2), ZONE_ID).toLocalDate()
        return d1.isEqual(d2)
    }

    fun isThisMonth(millis: Long): Boolean {
        val target = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID)
        val now = LocalDateTime.now(ZONE_ID)
        return target.year == now.year && target.month == now.month
    }

    fun getStartOfTodayMillis(): Long {
        val today = LocalDate.now(ZONE_ID)
        return today.atStartOfDay(ZONE_ID).toInstant().toEpochMilli()
    }

    fun getStartOfMonthMillis(): Long {
        val now = LocalDate.now(ZONE_ID)
        val startOfMonth = now.withDayOfMonth(1)
        return startOfMonth.atStartOfDay(ZONE_ID).toInstant().toEpochMilli()
    }

    data class DayForecastItem(
        val dayName: String,
        val dateFormatted: String,
        val epochDay: Long,
        val isToday: Boolean,
        val localDate: LocalDate
    )

    /**
     * Returns today and the next 7 days (total 8 days)
     */
    fun getNext7Days(): List<DayForecastItem> {
        val today = LocalDate.now(ZONE_ID)
        val list = mutableListOf<DayForecastItem>()
        for (i in 0..7) {
            val date = today.plusDays(i.toLong())
            val dayName = if (i == 0) "Hari ini" else date.format(DateTimeFormatter.ofPattern("EEEE", INDONESIAN_LOCALE))
            val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd MMM", INDONESIAN_LOCALE))
            list.add(
                DayForecastItem(
                    dayName = dayName,
                    dateFormatted = dateFormatted,
                    epochDay = date.toEpochDay(),
                    isToday = i == 0,
                    localDate = date
                )
            )
        }
        return list
    }

    fun combineDateAndTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val ldt = LocalDateTime.of(year, month, day, hour, minute)
        return ldt.atZone(ZONE_ID).toInstant().toEpochMilli()
    }
}
