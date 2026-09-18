package com.dailyprojects.unitconverter

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun formatResult(value: Double): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    formatter.maximumFractionDigits = 4
    formatter.usesGroupingSeparator = true
    return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toString()
}
