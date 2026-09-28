package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    val ID_LOCALE: Locale = Locale.forLanguageTag("id-ID")

    /**
     * Formats number to Indonesian Rupiah standard format:
     * e.g. 2000000 -> "Rp 2.000.000"
     * Zero -> "Rp 0"
     * Handles negative values properly: -50000 -> "-Rp 50.000"
     */
    fun formatRupiah(amount: Long, withPrefix: Boolean = true): String {
        val symbols = DecimalFormatSymbols(ID_LOCALE).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        val absAmount = kotlin.math.abs(amount)
        val formattedNumber = formatter.format(absAmount)
        val sign = if (amount < 0) "-" else ""

        return if (withPrefix) {
            "${sign}Rp $formattedNumber"
        } else {
            "${sign}$formattedNumber"
        }
    }

    /**
     * Compact format for space-constrained badges (e.g. "Rp 2,5 jt")
     */
    fun formatCompactRupiah(amount: Long): String {
        val abs = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            abs >= 1_000_000_000 -> "${sign}Rp ${String.format(ID_LOCALE, "%.1f", abs / 1_000_000_000.0)} M"
            abs >= 1_000_000 -> "${sign}Rp ${String.format(ID_LOCALE, "%.1f", abs / 1_000_000.0)} jt"
            abs >= 1_000 -> "${sign}Rp ${String.format(ID_LOCALE, "%.0f", abs / 1_000.0)} rb"
            else -> formatRupiah(amount)
        }
    }

    /**
     * Parses digits string safely into Long
     */
    fun parseRupiahInput(input: String): Long {
        val cleanDigits = input.filter { it.isDigit() }
        return cleanDigits.toLongOrNull() ?: 0L
    }

    /**
     * Real-time formatter for text fields as the user types
     * e.g. "50000" -> "Rp 50.000"
     * Empty -> ""
     */
    fun formatInputAsRupiah(input: String): String {
        val cleanDigits = input.filter { it.isDigit() }
        if (cleanDigits.isEmpty()) return ""
        val number = cleanDigits.toLongOrNull() ?: return ""
        return formatRupiah(number, withPrefix = true)
    }
}
