package com.dailyprojects.caffeine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Brown = Color(0xFF6D4C41)
private val Amber = Color(0xFFFFA000)

private val LightColors = lightColorScheme(primary = Brown, secondary = Amber)
private val DarkColors = darkColorScheme(primary = Color(0xFFBCAAA4), secondary = Amber)

@Composable
fun CaffeineTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
