package com.dailyprojects.shortlink.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF3F51B5)
private val Teal = Color(0xFF00897B)

private val LightColors = lightColorScheme(primary = Indigo, secondary = Teal)
private val DarkColors = darkColorScheme(primary = Indigo, secondary = Teal)

@Composable
fun ShortLinkTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
