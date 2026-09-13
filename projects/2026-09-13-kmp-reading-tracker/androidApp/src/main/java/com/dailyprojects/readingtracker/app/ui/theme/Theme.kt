package com.dailyprojects.readingtracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InkBlue = Color(0xFF2C4A73)
private val PageAmber = Color(0xFFB98A2E)

private val LightColors = lightColorScheme(
    primary = InkBlue,
    secondary = PageAmber,
)

private val DarkColors = darkColorScheme(
    primary = InkBlue,
    secondary = PageAmber,
)

@Composable
fun ReadingTrackerTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
