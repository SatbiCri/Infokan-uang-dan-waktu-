package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.util.CurrencyUtils

/**
 * Renders currency text in Indonesian standard format (e.g. "Rp 2.000.000")
 * Ensures numbers NEVER clip into awkward dots ("2.00.00.....") even for
 * very large values (e.g. Rp 2.000.000.000.000), by dynamically calculating
 * available width, scaling font size cleanly, or allowing smooth horizontal scroll.
 */
@Composable
fun AutoResizingCurrencyText(
    amount: Long,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    baseFontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Start,
    testTag: String = "currency_text"
) {
    val formatted = CurrencyUtils.formatRupiah(amount)

    BoxWithConstraints(modifier = modifier) {
        val maxWidthDp = maxWidth

        // Calculate appropriate font size based on text length and available container width
        val textLength = formatted.length
        val calculatedFontSize = when {
            maxWidthDp.value < 130 && textLength > 12 -> 13.sp
            maxWidthDp.value < 150 && textLength > 14 -> 14.sp
            maxWidthDp.value < 180 && textLength > 16 -> 15.sp
            textLength > 18 -> 15.sp
            textLength > 15 -> (baseFontSize.value * 0.85f).sp
            textLength > 12 -> (baseFontSize.value * 0.92f).sp
            else -> baseFontSize
        }

        Text(
            text = formatted,
            color = color,
            fontSize = calculatedFontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .testTag(testTag)
                .horizontalScroll(rememberScrollState())
        )
    }
}
