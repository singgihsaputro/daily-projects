package com.dailyprojects.redactlog

/** Built-in rules, tuned for Indonesian apps but safe anywhere. */
object DefaultRules {

    val jwt = Rule(
        "jwt",
        Regex("""eyJ[\w-]{5,}\.[\w-]{5,}\.[\w-]*"""),
        "[JWT]",
    )

    val bearer = Rule(
        "bearer",
        Regex("""(?i)\b(bearer)\s+[A-Za-z0-9._~+/=-]{8,}"""),
        "$1 [TOKEN]",
    )

    val secretPair = Rule(
        "secret",
        Regex("""(?i)\b(password|passwd|pwd|secret|token|api[_-]?key|otp|pin)(\s*[=:]\s*)[^\s&,;]+"""),
        "$1$2[SECRET]",
    )

    val email = Rule(
        "email",
        Regex("""[\w.%+-]+@[\w-]+(\.[\w-]+)+"""),
        "[EMAIL]",
    )

    /** +62 / 62 / 0 prefixes, mobile numbers of 9–12 digits after the prefix, spaces or dashes allowed. */
    val phone = Rule(
        "phone",
        Regex("""(?<![\d])(?:\+62|62|0)[\s-]?8\d{1,2}(?:[\s-]?\d){6,9}(?![\d])"""),
        "[PHONE]",
    )

    /** 13–19 digits (spaces/dashes allowed) that also pass the Luhn checksum. */
    val card = Rule(
        "card",
        Regex("""(?<![\d])\d(?:[ -]?\d){12,18}(?![\d])"""),
        "[CARD]",
        accepts = { luhnValid(it.filter(Char::isDigit)) },
    )

    /** NIK: 16 digits, province 11–94, month 01–12 (female birthdays add 40 to the day). */
    val nik = Rule(
        "nik",
        Regex("""(?<![\d])(?:[1-9]\d)\d{4}(?:[0-6]\d)(?:0[1-9]|1[0-2])\d{2}\d{4}(?![\d])"""),
        "[NIK]",
        accepts = { it.length == 16 && it.take(2).toInt() in 11..94 },
    )

    // Order matters: tokens first, then NIK before the broader card/phone patterns.
    val all = listOf(jwt, bearer, secretPair, email, nik, card, phone)

    internal fun luhnValid(digits: String): Boolean {
        if (digits.length !in 13..19) return false
        var sum = 0
        digits.reversed().forEachIndexed { i, c ->
            var d = c - '0'
            if (i % 2 == 1) {
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
        }
        return sum % 10 == 0
    }
}
