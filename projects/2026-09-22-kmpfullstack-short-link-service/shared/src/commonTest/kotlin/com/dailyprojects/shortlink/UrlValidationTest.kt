package com.dailyprojects.shortlink

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlValidationTest {

    @Test
    fun acceptsHttpAndHttpsUrls() {
        assertTrue(isValidHttpUrl("https://kotlinlang.org/docs/multiplatform.html"))
        assertTrue(isValidHttpUrl("http://example.com"))
        assertTrue(isValidHttpUrl("  https://example.com/path?query=1  "))
    }

    @Test
    fun rejectsMissingSchemeOrHost() {
        assertFalse(isValidHttpUrl("kotlinlang.org"))
        assertFalse(isValidHttpUrl("ftp://example.com"))
        assertFalse(isValidHttpUrl("https://"))
        assertFalse(isValidHttpUrl("https://no spaces.com"))
        assertFalse(isValidHttpUrl(""))
    }
}
