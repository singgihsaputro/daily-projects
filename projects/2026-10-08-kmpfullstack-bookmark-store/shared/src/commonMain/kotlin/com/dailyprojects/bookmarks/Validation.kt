package com.dailyprojects.bookmarks

const val MAX_TITLE_LENGTH = 80

/** A deliberately small check: has a scheme, has a non-blank host with a dot in it. */
fun isValidHttpUrl(value: String): Boolean {
    val trimmed = value.trim()
    if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) return false
    val host = trimmed.substringAfter("://").substringBefore("/").substringBefore("?")
    return host.isNotBlank() && host.contains(".") && !host.contains(" ")
}

/** Tags are single lowercase words so "Kotlin " and "kotlin" land in the same bucket. */
fun normalizeTag(value: String): String = value.trim().lowercase().replace(Regex("\\s+"), "-")
