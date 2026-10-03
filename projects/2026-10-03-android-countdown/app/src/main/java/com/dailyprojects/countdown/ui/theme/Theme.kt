package com.dailyprojects.countdown.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Plum = Color(0xFF6A3FA0)
private val Amber = Color(0xFFE8A21A)

private val LightColors = lightColorScheme(primary = Plum, secondary = Amber)
private val DarkColors = darkColorScheme(primary = Color(0xFFCFB2F5), secondary = Amber)

@Composable
fun CountdownTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
