package com.dailyprojects.grocerylist

/** Each platform formats the checked-off fraction with its own locale-aware percent formatter. */
expect fun formatProgress(fraction: Double): String
