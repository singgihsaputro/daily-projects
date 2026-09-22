package com.dailyprojects.shortlink

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ShortCodeGeneratorTest {

    @Test
    fun generatesCodeOfRequestedLength() {
        val code = ShortCodeGenerator.generate(existingCodes = emptySet(), length = 8, random = Random(1))
        assertEquals(8, code.length)
    }

    @Test
    fun neverReturnsAnExistingCode() {
        val random = Random(42)
        val existing = mutableSetOf<String>()
        repeat(20) {
            val code = ShortCodeGenerator.generate(existingCodes = existing, length = 4, random = random)
            assertTrue(code !in existing)
            existing += code
        }
    }

    @Test
    fun givesUpAfterExhaustingTheSpace() {
        // length 1 over this alphabet has 62 possible codes; taking all of them
        // forces the call to fail after MAX_ATTEMPTS rather than loop forever.
        val fullAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            .map { it.toString() }.toSet()
        assertFailsWith<IllegalStateException> {
            ShortCodeGenerator.generate(existingCodes = fullAlphabet, length = 1, random = Random(7))
        }
    }
}
