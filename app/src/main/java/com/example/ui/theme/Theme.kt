package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantDarkPrimary,
    onPrimary = ElegantDarkOnPrimary,
    primaryContainer = ElegantDarkPrimaryContainer,
    onPrimaryContainer = ElegantDarkOnPrimaryContainer,
    secondary = CasinoGold,
    onSecondary = Color.Black,
    secondaryContainer = ElegantDarkSurfaceElevated,
    onSecondaryContainer = ElegantDarkTextPrimary,
    tertiary = CasinoGold,
    onTertiary = Color.Black,
    background = ElegantDarkBackground,
    onBackground = ElegantDarkTextPrimary,
    surface = ElegantDarkSurface,
    onSurface = ElegantDarkTextPrimary,
    surfaceVariant = ElegantDarkSurfaceElevated,
    onSurfaceVariant = ElegantDarkTextSecondary,
    outline = ElegantDarkBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ElegantDarkColorScheme,
        typography = Typography,
        content = content
    )
}


