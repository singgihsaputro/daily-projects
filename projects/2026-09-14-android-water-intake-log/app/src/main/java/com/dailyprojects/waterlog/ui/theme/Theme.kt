package com.dailyprojects.waterlog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AquaBlue = Color(0xFF1E88E5)
private val DeepTeal = Color(0xFF00695C)

private val LightColors = lightColorScheme(
    primary = AquaBlue,
    secondary = DeepTeal,
)

private val DarkColors = darkColorScheme(
    primary = AquaBlue,
    secondary = DeepTeal,
)

@Composable
fun WaterLogTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
