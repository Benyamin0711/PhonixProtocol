# Bottom Navigation Bar Redesign

## [S1] Problem
The current bottom navigation bar needs to be redesigned to match a mobile banking app style (similar to Bank Melli Iran). The user wants:
- A footer navigation bar with app logo in center
- Semi-transparent dark background
- Bank Melli style icons and layout
- Drawer menu should be narrower

## [S2] Solution Overview

### Bottom Navigation Bar
- **Layout**: 5 items (2 left, 1 center, 2 right)
- **Background**: Semi-transparent dark (like Bank Melli)
- **Center**: App logo image that navigates to Dashboard on click
- **Left items**: Account (Person icon), Settings (Gear icon)
- **Right items**: Leaderboard (Trophy icon), Support (Help icon)
- **Icons**: Material Icons with labels below
- **Active state**: PhoenixOrange color, inactive: TextSecondary

### Drawer Menu
- Keep existing drawer functionality
- Reduce width to ~280dp (from default ~360dp)
- Maintain all existing navigation options

## [S3] Components to Modify

1. **BottomNavBar.kt** - Complete redesign with new layout and styling
2. **MainScreen.kt** (NEW) - Main scaffold with bottom nav that wraps all main screens
3. **DashboardScreen.kt** - Remove BottomNavBar (moved to MainScreen)
4. **ProfileScreen.kt** - Remove BottomNavBar if present
5. **SettingsScreen.kt** - Remove BottomNavBar if present
6. **LeaderboardScreen.kt** - Remove BottomNavBar if present
7. **SupportScreen.kt** - Remove BottomNavBar if present
8. **NavGraph.kt** - Update to use MainScreen wrapper
9. **strings.xml** - Add/update bottom nav labels if needed

## [S4] Visual Design

```
┌─────────────────────────────────────┐
│  [Menu]                    [TopBar] │
├─────────────────────────────────────┤
│                                     │
│         Dashboard Content           │
│                                     │
├─────────────────────────────────────┤
│  👤        ⚙️     🏠     🏆     ❓  │
│ Account  Settings Logo Leaderboard │
│                      Support       │
└─────────────────────────────────────┘
```

## [S5] Implementation Details

### MainScreen.kt (NEW)
- Create a main scaffold that wraps Dashboard, Profile, Settings, Leaderboard, Support
- Include BottomNavBar in the scaffold
- Include TopAppBar with drawer menu
- Handle navigation between screens using NavHost

### BottomNavBar.kt Changes
- Replace current layout with Bank Melli style
- Add app logo image (drawable resource)
- Update icons to match banking app style
- Add semi-transparent background
- Keep navigation functionality

### App Logo
- Use existing Phoenix logo drawable
- Size: 56dp circular container
- Shadow and elevation for prominence

### Navigation Items
- Left: Account (Icons.Filled.Person), Settings (Icons.Filled.Settings)
- Center: App Logo (drawable)
- Right: Leaderboard (Icons.Filled.EmojiEvents), Support (Icons.AutoMirrored.Filled.Help)

### Drawer Menu
- Keep existing drawer functionality
- Reduce width to ~280dp (from default ~360dp)
- Maintain all existing navigation options

## [S6] Testing
- Verify all navigation routes work
- Check visual appearance on different screen sizes
- Test drawer menu width reduction
- Verify center logo tap navigates to dashboard