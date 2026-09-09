package com.dailyprojects.pomodoro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TomatoRed = Color(0xFFD64545)
private val FocusGreen = Color(0xFF3C9D6B)

private val LightColors = lightColorScheme(
    primary = TomatoRed,
    secondary = FocusGreen,
)

private val DarkColors = darkColorScheme(
    primary = TomatoRed,
    secondary = FocusGreen,
)

@Composable
fun PomodoroTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
