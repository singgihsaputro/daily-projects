package com.dailyprojects.unitconverter

/** Each platform formats the converted number with its own decimal formatter (grouping, locale rounding). */
expect fun formatResult(value: Double): String
