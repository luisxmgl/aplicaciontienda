package com.example.aplicaciontienda.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Mapea 1:1 con res/values/themes.xml y res/values-night/themes.xml (misma paleta que la web).
private val LightColors = lightColorScheme(
    primary = PrimaryInk,
    onPrimary = BackgroundPaper,
    primaryContainer = PrimaryInkDark,
    onPrimaryContainer = BackgroundPaper,
    secondary = AccentThread,
    onSecondary = BackgroundPaper,
    secondaryContainer = AccentThreadDark,
    onSecondaryContainer = BackgroundPaper,
    tertiary = SuccessGreen,
    onTertiary = BackgroundPaper,
    background = BackgroundPaper,
    onBackground = NeutralCharcoal,
    surface = BackgroundPaper,
    onSurface = NeutralCharcoal,
    surfaceVariant = BackgroundPaperRaised,
    onSurfaceVariant = NeutralMutedBrown,
    outline = BorderPaleTan,
    error = androidx.compose.ui.graphics.Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = BackgroundPaper,
    onPrimary = PrimaryInkDark,
    primaryContainer = PrimaryInk,
    onPrimaryContainer = BackgroundPaper,
    secondary = AccentThread,
    onSecondary = PrimaryInkDark,
    secondaryContainer = AccentThreadDark,
    onSecondaryContainer = BackgroundPaper,
    tertiary = SuccessGreen,
    onTertiary = PrimaryInkDark,
    background = PrimaryInkDark,
    onBackground = BackgroundPaper,
    surface = PrimaryInkDark,
    onSurface = BackgroundPaper,
    surfaceVariant = PrimaryInk,
    onSurfaceVariant = BackgroundPaperRaised,
    outline = NeutralMutedBrown
)

@Composable
fun VillaAceroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
