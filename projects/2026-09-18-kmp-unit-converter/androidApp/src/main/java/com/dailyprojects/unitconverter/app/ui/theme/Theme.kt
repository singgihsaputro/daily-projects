package com.dailyprojects.unitconverter.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF00695C)
private val Amber = Color(0xFFEF6C00)

private val LightColors = lightColorScheme(
    primary = Teal,
    secondary = Amber,
)

private val DarkColors = darkColorScheme(
    primary = Teal,
    secondary = Amber,
)

@Composable
fun UnitConverterTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
