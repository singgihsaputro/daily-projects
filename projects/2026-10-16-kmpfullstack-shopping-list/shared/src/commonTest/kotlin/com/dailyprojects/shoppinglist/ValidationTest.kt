package com.dailyprojects.shoppinglist

import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationTest {
    @Test
    fun categoryIsLowercasedTrimmedAndHyphenated() {
        assertEquals("frozen-food", normalizeCategory("  Frozen   Food "))
        assertEquals("", normalizeCategory("   "))
    }
}
