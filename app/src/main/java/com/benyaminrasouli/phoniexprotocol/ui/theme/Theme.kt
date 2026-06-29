package com.benyaminrasouli.phoniexprotocol.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PhoenixDarkColorScheme = darkColorScheme(
    primary = PhoenixPrimary,
    onPrimary = PhoenixOnPrimary,
    primaryContainer = PhoenixPrimaryContainer,
    onPrimaryContainer = PhoenixOnPrimaryContainer,
    secondary = PhoenixSecondary,
    onSecondary = PhoenixOnSecondary,
    secondaryContainer = PhoenixSecondaryContainer,
    onSecondaryContainer = PhoenixOnSecondaryContainer,
    tertiary = PhoenixTertiary,
    onTertiary = PhoenixOnTertiary,
    tertiaryContainer = PhoenixTertiaryContainer,
    onTertiaryContainer = PhoenixOnTertiaryContainer,
    background = PhoenixBackground,
    onBackground = PhoenixOnBackground,
    surface = PhoenixSurface,
    onSurface = PhoenixOnSurface,
    surfaceVariant = PhoenixSurfaceVariant,
    onSurfaceVariant = PhoenixOnSurfaceVariant,
    error = PhoenixError,
    onError = PhoenixOnError,
    errorContainer = PhoenixErrorContainer,
    onErrorContainer = PhoenixOnErrorContainer
)

@Composable
fun PhoenixProtocolTheme(content: @Composable () -> Unit) {
    val colorScheme = PhoenixDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PhoenixTypography,
        shapes = PhoenixShapes,
        content = content
    )
}
