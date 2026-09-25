package com.dailyprojects.qrgen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InkBlue = Color(0xFF1F4E8C)
private val ScanTeal = Color(0xFF00796B)

private val LightColors = lightColorScheme(
    primary = InkBlue,
    secondary = ScanTeal,
)

private val DarkColors = darkColorScheme(
    primary = InkBlue,
    secondary = ScanTeal,
)

@Composable
fun QrGeneratorTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
