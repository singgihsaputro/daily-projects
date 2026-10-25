package com.dailyprojects.redactlog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactorTest {
    private val r = Redactor()

    @Test fun email() = assertEquals("hi [EMAIL]!", r.redact("hi budi.s@example.co.id!"))

    @Test fun phoneFormats() {
        assertEquals("[PHONE]", r.redact("+62 812-3456-7890"))
        assertEquals("a [PHONE] b", r.redact("a 081298765432 b"))
        assertEquals("[PHONE]", r.redact("6281234567890"))
    }

    @Test fun nik() = assertEquals("NIK [NIK].", r.redact("NIK 3174021203900001."))

    @Test fun cardNeedsLuhn() {
        assertEquals("[CARD]", r.redact("4111 1111 1111 1111"))
        assertEquals("4111 1111 1111 1112", r.redact("4111 1111 1111 1112"))
    }

    @Test fun plainNumbersSurvive() {
        val s = "order=12345678 id=5f2c total 250000"
        assertEquals(s, r.redact(s))
    }

    @Test fun tokens() {
        assertEquals("Authorization: Bearer [TOKEN]", r.redact("Authorization: Bearer abcdef1234567890"))
        assertEquals("sub [JWT]", r.redact("sub eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MiJ9.c2lnbmF0dXJl"))
    }

    @Test fun secretPairKeepsKey() =
        assertEquals("password=[SECRET]&remember=true", r.redact("password=hunter2&remember=true"))

    @Test fun matchedRules() {
        assertEquals(listOf("email", "nik"), r.matchedRules("a@b.id 3174021203900001"))
        assertTrue(r.matchedRules("nothing here").isEmpty())
    }

    @Test fun customRule() {
        val extra = Rule("plate", Regex("""\b[A-Z]{1,2} \d{1,4} [A-Z]{1,3}\b"""), "[PLATE]")
        assertEquals("car [PLATE]", (r + extra).redact("car B 1234 XYZ"))
    }

    @Test fun luhn() {
        assertTrue(DefaultRules.luhnValid("4111111111111111"))
        assertFalse(DefaultRules.luhnValid("4111111111111112"))
    }
}
