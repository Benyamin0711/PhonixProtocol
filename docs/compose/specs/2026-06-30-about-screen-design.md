# About Screen Design Spec

> **Date:** 2026-06-30
> **Status:** Approved

## [S1] Problem

The About route exists in navigation but no screen is implemented. Users need a place to view app information, developer details, and legal links.

## [S2] Solution Overview

Implement a full About screen with App Header, Description, Developer Info, Social Links, and Legal sections. Follow existing dark theme patterns and bilingual support.

## [S3] App Header Section

- Phoenix Protocol logo/icon
- App name "Phoenix Protocol"
- Version from BuildConfig.VERSION_NAME

## [S4] Description Section

- App description text
- "A gamified task management app that turns discipline into an RPG experience"

## [S5] Developer Info Section

- Developer name
- Contact email (placeholder)

## [S6] Social Links Section

- GitHub link (placeholder)
- Website link (placeholder)

## [S7] Legal Section

- Privacy Policy link (placeholder)
- Terms of Service link (placeholder)

## [S8] Architecture

- Create: `AboutScreen.kt` in `feature/about/`
- Modify: `NavGraph.kt` - add About route composable
- Modify: `DashboardScreen.kt` - wire onNavigateToAbout callback
- Modify: `DrawerScreen.kt` - add About menu item (currently missing)

## [S9] UI Patterns

- BackgroundDark base
- SurfaceDark card containers
- PhoenixOrange for accents and icons
- TextSecondary for labels
- Bilingual strings (EN + FA) for all user-facing text

## [S10] String Resources

New strings needed:
- `about_title`, `about_description`, `about_developer`
- `about_contact`, `about_github`, `about_website`
- `about_privacy_policy`, `about_terms_of_service`
- Persian equivalents for all strings
