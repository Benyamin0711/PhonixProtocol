# Privacy Policy + Play Store Content Design Spec

## [S1] Problem
Play Store requires a Privacy Policy for apps that collect user data. The app currently has a link to an external URL that may not exist. Need an in-app Privacy Policy screen and Play Store listing content.

## [S2] Solution Overview
Create an in-app PrivacyPolicyScreen composable with bilingual content (EN/FA). Update About screen to navigate to in-app screen. Create Play Store listing content.

## [S3] Privacy Policy Content
- Data Collection: tasks, habits, mood entries, achievements, profile (all local)
- Data Storage: Room database on device, DataStore for settings
- No internet permission — no data transmitted externally
- No third-party analytics or advertising
- Children's privacy: not directed at children under 13
- Changes: policy updated via app updates

## [S4] Privacy Policy Screen
- LazyColumn with sections
- Material 3 styling matching app theme
- Back navigation via TopAppBar
- Language auto-detect from app settings (EN/FA)

## [S5] Navigation
- Route: `privacy_policy` in Screen.kt
- NavGraph composable with slide transition
- About screen Privacy Policy link updated to navigate in-app

## [S6] Play Store Content
- Short description: 80 chars max
- Full description: compelling feature list + keywords
- EN and FA versions

**Status: Complete** — See `docs/compose/specs/play-store-listing.md`
