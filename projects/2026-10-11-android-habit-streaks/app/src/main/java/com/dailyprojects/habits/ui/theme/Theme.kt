package com.dailyprojects.habits.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF00796B)
private val Flame = Color(0xFFF57C00)

private val LightColors = lightColorScheme(primary = Teal, secondary = Flame)
private val DarkColors = darkColorScheme(primary = Color(0xFF80CBC4), secondary = Flame)

@Composable
fun HabitsTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
