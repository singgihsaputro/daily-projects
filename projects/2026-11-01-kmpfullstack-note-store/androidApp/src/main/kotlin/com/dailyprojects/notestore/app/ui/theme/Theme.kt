package com.dailyprojects.notestore.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF3949AB)
private val Amber = Color(0xFFF9A825)

private val LightColors = lightColorScheme(primary = Indigo, secondary = Amber)
private val DarkColors = darkColorScheme(primary = Color(0xFF9FA8DA), secondary = Amber)

@Composable
fun NoteTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
