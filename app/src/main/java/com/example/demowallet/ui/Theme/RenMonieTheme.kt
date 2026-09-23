package com.example.demowallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RenDarkColorScheme = darkColorScheme(
    primary = RenPrimary,
    onPrimary = RenTextOnPrimary,
    primaryContainer = RenPrimaryContainer,
    onPrimaryContainer = RenPrimaryLight,
    secondary = RenSecondary,
    onSecondary = RenTextOnPrimary,
    tertiary = RenAccent,
    onTertiary = Color(0xFF1A1A1A),
    background = RenBackground,
    onBackground = RenTextPrimary,
    surface = RenSurface,
    onSurface = RenTextPrimary,
    surfaceVariant = RenSurfaceVariant,
    onSurfaceVariant = RenTextSecondary,
    error = RenError,
    onError = RenTextOnPrimary,
    outline = RenBorder,
    outlineVariant = RenDivider
)

@Composable
fun RenMonieTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RenDarkColorScheme,
        typography = RenTypography,
        content = content
    )
}
