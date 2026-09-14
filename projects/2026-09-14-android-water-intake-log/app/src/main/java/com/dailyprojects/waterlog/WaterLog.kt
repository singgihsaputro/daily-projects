package com.dailyprojects.waterlog

data class DrinkPreset(
    val id: String,
    val label: String,
    val amountMl: Int,
)

data class WaterEntry(
    val id: String,
    val label: String,
    val amountMl: Int,
    val time: String,
)

data class WaterLogFixture(
    val dailyGoalMl: Int,
    val presets: List<DrinkPreset>,
    val entries: List<WaterEntry>,
)

/** Where today's goal, presets and logged entries come from. Swap
 *  [LocalWaterLogRepository] for a networked implementation later without
 *  touching the ViewModel or UI. */
interface WaterLogRepository {
    fun loadFixture(): WaterLogFixture
}
