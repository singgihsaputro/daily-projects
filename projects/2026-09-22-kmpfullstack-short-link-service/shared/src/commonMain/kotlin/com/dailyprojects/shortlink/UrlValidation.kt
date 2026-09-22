package com.dailyprojects.shortlink

/** A deliberately small check: has a scheme, has a non-blank host with a dot in it. */
fun isValidHttpUrl(value: String): Boolean {
    val trimmed = value.trim()
    if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) return false
    val host = trimmed.substringAfter("://").substringBefore("/").substringBefore("?")
    return host.isNotBlank() && host.contains(".") && !host.contains(" ")
}
