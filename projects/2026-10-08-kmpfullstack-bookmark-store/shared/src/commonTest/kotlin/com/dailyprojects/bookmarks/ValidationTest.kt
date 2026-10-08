package com.dailyprojects.bookmarks

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationTest {
    @Test
    fun acceptsHttpAndHttpsUrls() {
        assertTrue(isValidHttpUrl("https://kotlinlang.org/docs/multiplatform.html"))
        assertTrue(isValidHttpUrl("  http://example.com  "))
    }

    @Test
    fun rejectsMissingSchemeOrHost() {
        assertFalse(isValidHttpUrl("kotlinlang.org"))
        assertFalse(isValidHttpUrl("ftp://example.com"))
        assertFalse(isValidHttpUrl("https://"))
        assertFalse(isValidHttpUrl("https://no spaces.com"))
    }

    @Test
    fun normalizesTags() {
        assertEquals("kotlin", normalizeTag("  Kotlin "))
        assertEquals("build-tools", normalizeTag("Build   Tools"))
        assertEquals("", normalizeTag("   "))
    }
}
