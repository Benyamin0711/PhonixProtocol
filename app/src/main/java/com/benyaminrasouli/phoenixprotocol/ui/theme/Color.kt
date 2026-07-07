package com.benyaminrasouli.phoenixprotocol.ui.theme

import androidx.compose.ui.graphics.Color

// Phoenix Core
val PhoenixOrange = Color(0xFFFF6B35)
val PhoenixRed = Color(0xFFE63946)
val PhoenixGold = Color(0xFFFFD700)

// Backgrounds
val BackgroundDark = Color(0xFF0D0D0D)
val SurfaceDark = Color(0xFF1A1A2E)
val SurfaceVariantDark = Color(0xFF252540)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0A0B0)
val TextTertiary = Color(0xFF6B6B80)

// Status
val EnergyGreen = Color(0xFF4CAF50)
val EnergyYellow = Color(0xFFFFEB3B)
val EnergyRed = Color(0xFFFF5252)
val ShadowPurple = Color(0xFF9C27B0)
val CompletedGreen = Color(0xFF4CAF50)
val FailedRed = Color(0xFFFF5252)

// Leaderboard medals
val LeaderboardGold = PhoenixGold
val LeaderboardSilver = Color(0xFFC0C0C0)
val LeaderboardBronze = Color(0xFFCD7F32)

// Mood scale
val MoodNoData = SurfaceDark
val MoodTerrible = Color(0xFFE53935)
val MoodBad = Color(0xFFFF9800)
val MoodOkay = Color(0xFFFFC107)
val MoodGood = Color(0xFF8BC34A)
val MoodAmazing = EnergyGreen

// Shadow tiers
val ShadowWarning = EnergyYellow
val ShadowCritical = Color(0xFFF44336)
val ShadowCorrupted = Color(0xFF673AB7)

// Analytics category colors
val CategoryBlue = Color(0xFF4A90D9)
val CategoryGreen = EnergyGreen
val CategoryPurple = ShadowPurple
val CategoryOrange = PhoenixOrange
val CategoryYellow = Color(0xFFFFC107)
val CategoryPink = Color(0xFFE91E63)
val CategoryGray = Color(0xFF607D8B)

// Splash
val SplashRadialDark = Color(0xFF1A0A00)

// Campaign gradient stops
val CampaignActiveStart = Color(0xFF5C2800)
val CampaignActiveEnd = Color(0xFF3D1A00)
val CampaignInactiveStart = Color(0xFF402000)
val CampaignInactiveEnd = Color(0xFF261200)
val CampaignCompletedStart = Color(0xFF0D2818)
val CampaignCompletedEnd = Color(0xFF0A1A10)

// Mood input card
val MoodCardBackground = Color(0xFF101010)

// Seed colors for Material 3
val PhoenixPrimary = PhoenixOrange
val PhoenixOnPrimary = Color(0xFFFFFFFF)
val PhoenixPrimaryContainer = Color(0xFF3D1A00)
val PhoenixOnPrimaryContainer = Color(0xFFFFDBC8)

val PhoenixSecondary = PhoenixRed
val PhoenixOnSecondary = Color(0xFFFFFFFF)
val PhoenixSecondaryContainer = Color(0xFF3B0A10)
val PhoenixOnSecondaryContainer = Color(0xFFFFDAD6)

val PhoenixTertiary = PhoenixGold
val PhoenixOnTertiary = Color(0xFF1A1A1A)
val PhoenixTertiaryContainer = Color(0xFF3D3500)
val PhoenixOnTertiaryContainer = Color(0xFFFFE08A)

val PhoenixBackground = BackgroundDark
val PhoenixOnBackground = TextPrimary
val PhoenixSurface = SurfaceDark
val PhoenixOnSurface = TextPrimary
val PhoenixSurfaceVariant = SurfaceVariantDark
val PhoenixOnSurfaceVariant = TextSecondary

val PhoenixError = EnergyRed
val PhoenixOnError = Color(0xFFFFFFFF)
val PhoenixErrorContainer = Color(0xFF3B0000)
val PhoenixOnErrorContainer = Color(0xFFFFDAD6)

// Accent color map
val accentColorMap = mapOf(
    "orange" to Color(0xFFFF6B35),
    "blue" to Color(0xFF4A90D9),
    "green" to Color(0xFF4CAF50),
    "purple" to Color(0xFF9C27B0),
    "pink" to Color(0xFFE91E63),
    "red" to Color(0xFFE63946),
    "gold" to Color(0xFFFFD700),
    "teal" to Color(0xFF009688)
)

fun getAccentColor(name: String): Color = accentColorMap[name] ?: PhoenixOrange

fun getBackgroundColor(level: Int, brightness: Int): Color {
    val base = when (level) {
        1 -> Color(0xFF080808) // Darker
        2 -> Color(0xFF000000) // AMOLED
        else -> Color(0xFF0D0D0D) // Dark (default)
    }
    val factor = brightness.coerceIn(0, 100) / 100f
    return base.copy(red = base.red * factor, green = base.green * factor, blue = base.blue * factor)
}

fun getSurfaceColor(level: Int, brightness: Int): Color {
    val bg = getBackgroundColor(level, brightness)
    return bg.copy(
        red = (bg.red + 0.10f).coerceAtMost(1f),
        green = (bg.green + 0.10f).coerceAtMost(1f),
        blue = (bg.blue + 0.18f).coerceAtMost(1f)
    )
}
