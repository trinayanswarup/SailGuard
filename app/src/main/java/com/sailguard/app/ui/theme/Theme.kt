package com.sailguard.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SailGuardColorScheme = lightColorScheme(
    primary              = TealPrimary,
    onPrimary            = Color.White,
    primaryContainer     = TealSurface,
    onPrimaryContainer   = TextPrimary,
    secondary            = TealLight,
    onSecondary          = Color.White,
    background           = AppBackground,
    onBackground         = TextPrimary,
    surface              = AppSurface,
    onSurface            = TextPrimary,
    surfaceVariant       = AppSurface2,
    onSurfaceVariant     = TextSecondary,
    error                = ErrorRed,
    onError              = Color.White,
    outline              = CardBorder,
    outlineVariant       = CardBorder
)

@Composable
fun SailGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SailGuardColorScheme,
        typography  = Typography,
        content     = content
    )
}
