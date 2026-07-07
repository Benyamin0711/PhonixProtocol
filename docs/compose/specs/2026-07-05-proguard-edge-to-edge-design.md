# ProGuard + Edge-to-edge Design Spec

## [S1] Problem
The release build has `isMinifyEnabled = false` — no code shrinking or obfuscation. The app uses deprecated `window.statusBarColor`/`window.navigationBarColor` instead of modern edge-to-edge APIs. These are required for Play Store submission and Android 15+ compliance.

## [S2] Solution Overview
Enable R8/ProGuard minification for release builds with proper keep rules. Switch to `enableEdgeToEdge()` API with proper system bars handling.

## [S3] ProGuard Configuration
- Enable `isMinifyEnabled = true` and `isShrinkResources = true` in release buildType
- Add keep rules for: Room entities, Gson serialization, Hilt, Kotlin Serialization, Compose
- Keep `SourceFile` and `LineNumberTable` attributes for crash reports

## [S4] Edge-to-edge
- Use `enableEdgeToEdge()` in Activity `onCreate` before `setContent`
- Remove deprecated `window.statusBarColor` / `window.navigationBarColor` from Theme.kt
- Use `WindowCompat.setDecorFitsSystemWindows(window, false)` for edge-to-edge
- Status bar icons: light on dark background (isAppearanceLightStatusBars = false)

## [S5] Files to Modify
- `app/build.gradle.kts` — enable minify + shrinkResources
- `app/proguard-rules.pro` — add keep rules
- `ui/theme/Theme.kt` — remove deprecated window color calls
- `MainActivity.kt` — add enableEdgeToEdge() call
