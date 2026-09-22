package com.dailyprojects.shortlink

import kotlin.random.Random

/** Generates short, URL-safe codes that don't collide with the codes already in use. */
object ShortCodeGenerator {
    private const val ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private const val MAX_ATTEMPTS = 50

    fun generate(existingCodes: Set<String>, length: Int = 6, random: Random = Random): String {
        require(length > 0) { "length must be positive" }
        repeat(MAX_ATTEMPTS) {
            val candidate = (1..length).joinToString("") { ALPHABET[random.nextInt(ALPHABET.length)].toString() }
            if (candidate !in existingCodes) return candidate
        }
        error("could not generate a unique short code after $MAX_ATTEMPTS attempts")
    }
}
