package com.dailyprojects.pwgen

data class PasswordOptions(
    val length: Int,
    val useUppercase: Boolean,
    val useLowercase: Boolean,
    val useNumbers: Boolean,
    val useSymbols: Boolean,
)

data class HistoryEntry(
    val id: String,
    val password: String,
    val length: Int,
    val strength: String,
    val time: String,
)

data class PasswordSettingsFixture(
    val defaultOptions: PasswordOptions,
    val history: List<HistoryEntry>,
)

/** Where the starting options and generation history come from. Swap
 *  [LocalPasswordHistoryRepository] for a networked/synced implementation
 *  later without touching the ViewModel or UI. */
interface PasswordHistoryRepository {
    fun loadFixture(): PasswordSettingsFixture
}
