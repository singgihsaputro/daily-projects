package com.dailyprojects.grocerylist

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterPercentStyle

actual fun formatProgress(fraction: Double): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterPercentStyle
    formatter.maximumFractionDigits = 0
    return formatter.stringFromNumber(NSNumber(double = fraction)) ?: "${(fraction * 100).toInt()}%"
}
