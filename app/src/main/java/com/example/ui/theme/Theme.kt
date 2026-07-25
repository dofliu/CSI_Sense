package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BoldTypographyLightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleOnContainer,
    secondary = EmeraldSecondary,
    onSecondary = TextLight,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = EmeraldSecondary,
    background = LavenderBackground,
    onBackground = TextDark,
    surface = LavenderSurface,
    onSurface = TextDark,
    surfaceVariant = LavenderSurfaceVariant,
    onSurfaceVariant = TextMuted,
    error = AlertRed,
    errorContainer = AlertRedContainer,
    onError = TextLight,
    outline = LavenderOutline
)

private val BoldTypographyDarkColorScheme = darkColorScheme(
    primary = PurpleAccent,
    onPrimary = PurpleOnContainer,
    primaryContainer = PurplePrimary,
    onPrimaryContainer = PurpleContainer,
    secondary = EmeraldSecondary,
    onSecondary = TextLight,
    background = Color(0xFF141218),
    onBackground = TextLight,
    surface = Color(0xFF2B2930),
    onSurface = TextLight,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = LavenderSurfaceVariant,
    error = AlertRed,
    outline = LavenderOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Light Bold Typography mode by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BoldTypographyDarkColorScheme else BoldTypographyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
