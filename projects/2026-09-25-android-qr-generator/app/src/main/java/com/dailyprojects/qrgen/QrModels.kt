package com.dailyprojects.qrgen

data class QrPreset(
    val label: String,
    val content: String,
)

data class QrHistoryEntry(
    val id: String,
    val content: String,
    val time: String,
)

data class QrFixture(
    val presets: List<QrPreset>,
    val history: List<QrHistoryEntry>,
)

/** Where starting presets and generation history come from. Swap
 *  [LocalQrHistoryRepository] for a networked/synced implementation
 *  later without touching the ViewModel or UI. */
interface QrHistoryRepository {
    fun loadFixture(): QrFixture
}
