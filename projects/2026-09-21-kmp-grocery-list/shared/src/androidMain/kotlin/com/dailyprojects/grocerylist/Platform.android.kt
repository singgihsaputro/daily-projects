package com.dailyprojects.grocerylist

import java.text.NumberFormat
import java.util.Locale

actual fun formatProgress(fraction: Double): String =
    NumberFormat.getPercentInstance(Locale.US).format(fraction)
