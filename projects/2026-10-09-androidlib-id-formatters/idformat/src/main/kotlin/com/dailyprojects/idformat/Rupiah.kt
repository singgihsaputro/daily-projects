package com.dailyprojects.idformat

/** Indonesian Rupiah formatting and parsing. Dots group thousands, commas mark decimals. */
object Rupiah {

    /** `1250000` -> `Rp 1.250.000`; negatives render as `-Rp 1.250.000`. */
    fun format(amount: Long, withSymbol: Boolean = true): String {
        val digits = if (amount < 0) amount.toString().drop(1) else amount.toString()
        val grouped = digits.reversed().chunked(3).joinToString(".").reversed()
        val sign = if (amount < 0) "-" else ""
        return if (withSymbol) "${sign}Rp $grouped" else "$sign$grouped"
    }

    /** Short form for tight UI: `1500000` -> `Rp 1,5 jt`, `2300000000` -> `Rp 2,3 M`. */
    fun formatCompact(amount: Long): String {
        val abs = if (amount < 0) -amount else amount
        val (divisor, unit) = when {
            abs >= 1_000_000_000_000L -> 1_000_000_000_000L to "T"
            abs >= 1_000_000_000L -> 1_000_000_000L to "M"
            abs >= 1_000_000L -> 1_000_000L to "jt"
            abs >= 1_000L -> 1_000L to "rb"
            else -> return format(amount)
        }
        val tenths = abs * 10 / divisor
        val whole = tenths / 10
        val fraction = tenths % 10
        val number = if (fraction == 0L) "$whole" else "$whole,$fraction"
        return "${if (amount < 0) "-" else ""}Rp $number $unit"
    }

    /**
     * Reads text such as `Rp 1.250.000`, `1.250.000,00` or `-Rp1.250.000` back into whole rupiah.
     * Returns null for anything that is not a well-formed Indonesian amount.
     */
    fun parse(text: String): Long? {
        var s = text.trim()
        val negative = s.startsWith("-")
        if (negative) s = s.drop(1).trim()
        s = s.removePrefix("Rp").removePrefix("rp").trim()
        if (s.contains(',')) {
            if (!s.substringAfter(',').all { it == '0' }) return null
            s = s.substringBefore(',')
        }
        if (!GROUPED.matches(s) && !PLAIN.matches(s)) return null
        val value = s.replace(".", "").toLongOrNull() ?: return null
        return if (negative) -value else value
    }

    private val GROUPED = Regex("""\d{1,3}(\.\d{3})+""")
    private val PLAIN = Regex("""\d+""")
}
