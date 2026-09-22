package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = Color.Black,
    primaryContainer = PurpleDark,
    onPrimaryContainer = Color.White,
    secondary = PurpleGlow,
    onSecondary = Color.White,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextPrimary,
    tertiary = PinkAccent,
    onTertiary = Color.White,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = RedError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark SaaS UI
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
