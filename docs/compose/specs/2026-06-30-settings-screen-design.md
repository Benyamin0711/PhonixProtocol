# Settings Screen Design Spec

> **Date:** 2026-06-30
> **Status:** Approved

## [S1] Problem

The Settings screen is listed in navigation but not implemented. Users need a place to manage app preferences, view account info, and access app information.

## [S2] Solution Overview

Implement a full Settings screen with 6 sections: Language, Notifications (placeholder), Theme Preview, Account, Data & Privacy, and About. Follow existing dark theme patterns and bilingual support.

## [S3] Language Section

- Display current language (English/Persian)
- Tap to toggle between languages
- Persist via SettingsDataStore
- Update app locale immediately

## [S4] Notifications Section (Placeholder)

- Toggle for task reminders (disabled, "Coming soon")
- Toggle for boss mission alerts (disabled, "Coming soon")
- Visual indicator that feature is not yet available

## [S5] Theme Preview Section

- Display current theme colors as swatches
- Show: Background (#0D0D0D), Surface (#1A1A2E), Accent (#FF6B35)
- No toggle (dark-only by design)
- Informational only

## [S6] Account Section

- Display username and full name from UserProfile
- "Edit Profile" button (placeholder, shows toast/snackbar)

## [S7] Data & Privacy Section

- "Export Data" button (placeholder)
- "Reset All Data" button with confirmation dialog
- Reset clears database and DataStore, returns to onboarding

## [S8] About Section

- App version from BuildConfig
- "About Phoenix Protocol" - shows app description
- "Support Us" - navigates to support screen

## [S9] Architecture

- Create: `SettingsScreen.kt` in `feature/settings/`
- Create: `SettingsViewModel.kt` in `feature/settings/`
- Modify: `DashboardScreen.kt` - pass settings navigation callback
- Navigation routes already exist in Screen.kt and NavGraph.kt

## [S10] UI Patterns

- BackgroundDark base
- SurfaceDark card containers
- PhoenixOrange for accents and active states
- TextSecondary for labels
- Bilingual strings (EN + FA) for all user-facing text

## [S11] String Resources

New strings needed:
- `settings_language`, `settings_notifications`, `settings_theme`, `settings_account`
- `settings_data_privacy`, `settings_about`, `settings_version`
- `settings_edit_profile`, `settings_export_data`, `settings_reset_data`
- `settings_reset_confirm`, `settings_coming_soon`
- Persian equivalents for all strings
