package com.dailyprojects.pwgen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultPurple = Color(0xFF6750A4)
private val SafeGreen = Color(0xFF2E7D32)

private val LightColors = lightColorScheme(
    primary = VaultPurple,
    secondary = SafeGreen,
)

private val DarkColors = darkColorScheme(
    primary = VaultPurple,
    secondary = SafeGreen,
)

@Composable
fun PasswordGeneratorTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
