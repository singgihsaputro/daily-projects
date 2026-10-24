package com.dailyprojects.habitsync.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Orange = Color(0xFFE65100)
private val Teal = Color(0xFF00897B)

private val LightColors = lightColorScheme(primary = Orange, secondary = Teal)
private val DarkColors = darkColorScheme(primary = Orange, secondary = Teal)

@Composable
fun HabitTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
