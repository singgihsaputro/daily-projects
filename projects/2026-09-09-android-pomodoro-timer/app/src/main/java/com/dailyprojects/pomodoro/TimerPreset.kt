package com.dailyprojects.pomodoro

data class TimerPreset(
    val id: String,
    val label: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
)

/** Where a timer preset list comes from. Swap [LocalPresetRepository] for a
 *  networked implementation later without touching any caller. */
interface PresetRepository {
    fun loadPresets(): List<TimerPreset>
}
