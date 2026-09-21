package com.dailyprojects.grocerylist.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF2E7D32)
private val Orange = Color(0xFFEF6C00)

private val LightColors = lightColorScheme(
    primary = Green,
    secondary = Orange,
)

private val DarkColors = darkColorScheme(
    primary = Green,
    secondary = Orange,
)

@Composable
fun GroceryListTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
