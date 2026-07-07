# Theme Customization Design Spec

## [S1] Problem
The app has a hardcoded dark theme with no user customization. All colors are static values in Color.kt and Theme.kt, and many screens use hardcoded hex colors directly. Users cannot personalize the look and feel of the app.

## [S2] Solution Overview
Add accent color selection (8 presets) and background darkness control (3 presets + brightness slider) to the Settings screen. Theme changes apply app-wide through a dynamic Material 3 color scheme generated from the user's selections.

## [S3] Accent Color System
- 8 preset colors: Orange (#FF6B35), Blue (#4A90D9), Green (#4CAF50), Purple (#9C27B0), Pink (#E91E63), Red (#E63946), Gold (#FFD700), Teal (#009688)
- Stored as String key in SettingsDataStore ("orange", "blue", "green", "purple", "pink", "red", "gold", "teal")
- Default: "orange" (current Phoenix Orange)
- Color function generates full Material 3 color scheme from single accent color

## [S4] Background System
- 3 preset levels: Dark (#0D0D0D), Darker (#080808), AMOLED (#000000)
- Brightness slider: 0-100%, applied as brightness multiplier to background color
- Stored as Int key in SettingsDataStore (0=Dark, 1=Darker, 2=AMOLED) + Int brightness (0-100, default 100)
- Background color = base color * (brightness / 100)

## [S5] Settings UI
- New "Theme" section in Settings screen between Language and Notifications
- Accent color: 8 color circles in 2 rows of 4, selected shows orange border
- Background: 3 radio buttons (Dark, Darker, AMOLED)
- Brightness: Slider 0-100% with percentage label
- Live preview: theme changes apply immediately on selection

## [S6] Data Flow
```
SettingsDataStore (accent, bgLevel, brightness)
  -> SettingsViewModel (reads, exposes as StateFlow)
  -> MainActivity (collects theme state)
  -> PhoenixProtocolTheme (generates dynamic colorScheme)
  -> All screens recompose with new colors
```

## [S7] Files to Modify
- `SettingsDataStore.kt` - add ACCENT_COLOR, BACKGROUND_LEVEL, BRIGHTNESS keys + setters
- `Color.kt` - add accentColorMap, getBackgroundColor() function
- `Theme.kt` - make PhoenixProtocolTheme accept accent/bg params, generate dynamic scheme
- `SettingsScreen.kt` - add Theme section UI
- `SettingsViewModel.kt` - add theme state + setters
- `MainActivity.kt` - read theme from DataStore, pass to theme composable
- `DashboardScreen.kt` - replace hardcoded #050505 with theme background

## [S8] Hardcoded Color Cleanup
Screens using hardcoded colors (BackgroundDark, #050505, #0D0D0D, etc.) must be replaced with `MaterialTheme.colorScheme.background` or `MaterialTheme.colorScheme.surface`. This ensures theme changes propagate everywhere.
