package com.dailyprojects.unitconverter

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

actual fun formatResult(value: Double): String {
    val formatter = DecimalFormat("#,##0.####", DecimalFormatSymbols(Locale.US))
    return formatter.format(value)
}
