package com.benyaminrasouli.phoenixprotocol.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

@Composable
fun PhoenixProtocolTheme(
    accentColor: String = "orange",
    backgroundLevel: Int = 0,
    brightness: Int = 100,
    content: @Composable () -> Unit
) {
    val accent = remember(accentColor) { getAccentColor(accentColor) }
    val bg = remember(backgroundLevel, brightness) { getBackgroundColor(backgroundLevel, brightness) }
    val surface = remember(backgroundLevel, brightness) { getSurfaceColor(backgroundLevel, brightness) }

    val colorScheme = darkColorScheme(
        primary = accent,
        onPrimary = Color.White,
        primaryContainer = accent.copy(alpha = 0.15f),
        onPrimaryContainer = accent.copy(alpha = 0.8f),
        secondary = accent.copy(alpha = 0.7f),
        onSecondary = Color.White,
        secondaryContainer = accent.copy(alpha = 0.1f),
        onSecondaryContainer = accent.copy(alpha = 0.6f),
        tertiary = PhoenixGold,
        onTertiary = PhoenixOnTertiary,
        tertiaryContainer = PhoenixGold.copy(alpha = 0.15f),
        onTertiaryContainer = PhoenixGold.copy(alpha = 0.8f),
        background = bg,
        onBackground = TextPrimary,
        surface = surface,
        onSurface = TextPrimary,
        surfaceVariant = surface.copy(
            red = (surface.red + 0.05f).coerceAtMost(1f),
            green = (surface.green + 0.05f).coerceAtMost(1f),
            blue = (surface.blue + 0.08f).coerceAtMost(1f)
        ),
        onSurfaceVariant = TextSecondary,
        error = EnergyRed,
        onError = TextPrimary,
        errorContainer = PhoenixErrorContainer,
        onErrorContainer = PhoenixOnErrorContainer
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: (view.context as? android.content.ContextWrapper)?.baseContext as? Activity
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PhoenixTypography,
            shapes = PhoenixShapes,
            content = content
        )
    }
}
