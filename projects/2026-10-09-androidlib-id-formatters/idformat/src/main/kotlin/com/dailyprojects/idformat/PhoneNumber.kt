package com.dailyprojects.idformat

/** Indonesian mobile numbers: accepts `08…`, `628…`, `+628…` and `8…`, with spaces or dashes. */
class PhoneNumber private constructor(
    /** Subscriber digits after the country code, e.g. `81234567890`. */
    private val subscriber: String
) {
    /** `+6281234567890` */
    val e164: String get() = "+62$subscriber"

    /** `081234567890` */
    val national: String get() = "0$subscriber"

    /** `0812-3456-7890`, grouped 4-4-rest as printed on most Indonesian cards. */
    val display: String
        get() {
            val n = national
            return listOf(n.take(4), n.drop(4).take(4), n.drop(8)).filter { it.isNotEmpty() }.joinToString("-")
        }

    /** Operator inferred from the four-digit prefix, or null when it is not in the table. */
    val operator: String? get() = OPERATORS[national.take(4)]

    override fun equals(other: Any?) = other is PhoneNumber && other.subscriber == subscriber
    override fun hashCode() = subscriber.hashCode()
    override fun toString() = e164

    companion object {
        /** Returns null unless [raw] is a plausible Indonesian mobile number (10–13 digits nationally). */
        fun parse(raw: String): PhoneNumber? {
            val cleaned = raw.filterNot { it == ' ' || it == '-' || it == '(' || it == ')' || it == '.' }
            val digits = cleaned.removePrefix("+")
            if (digits.isEmpty() || !digits.all { it in '0'..'9' }) return null
            if (cleaned.startsWith("+") && !digits.startsWith("62")) return null
            val subscriber = when {
                digits.startsWith("62") -> digits.drop(2)
                digits.startsWith("0") -> digits.drop(1)
                else -> digits
            }
            if (!subscriber.startsWith("8") || subscriber.length !in 9..12) return null
            return PhoneNumber(subscriber)
        }

        private val OPERATORS: Map<String, String> = buildMap {
            fun add(name: String, vararg prefixes: String) = prefixes.forEach { put(it, name) }
            add("Telkomsel", "0811", "0812", "0813", "0821", "0822", "0823", "0851", "0852", "0853")
            add("Indosat", "0814", "0815", "0816", "0855", "0856", "0857", "0858")
            add("XL", "0817", "0818", "0819", "0859", "0877", "0878")
            add("Tri", "0895", "0896", "0897", "0898", "0899")
            add("Smartfren", "0881", "0882", "0883", "0884", "0885", "0886", "0887", "0888", "0889")
        }
    }
}
