# Phase 11: Data Export/Backup — Design Spec

## [S1] Problem

Users have no way to backup or export their data. If they switch devices or lose their phone, all progress (tasks, achievements, boss history, focus sessions, etc.) is lost. The Settings screen has a "Data Reset" but no export/import.

## [S2] Solution Overview

JSON export/import of all database tables. Export creates a single JSON file that can be shared/saved. Import parses a JSON file and restores data. Both accessible from Settings > Data & Privacy section.

## [S3] Export Format

Single JSON file with metadata:
```json
{
  "version": 10,
  "exportedAt": "ISO-8601 timestamp",
  "appName": "PhoenixProtocol",
  "profile": { UserProfile object },
  "stats": { UserStats object },
  "tasks": [ Task objects ],
  "categories": [ Category objects ],
  "templates": [ Template objects ],
  "achievements": [ Achievement objects ],
  "userAchievements": [ UserAchievement objects ],
  "bosses": [ Boss objects ],
  "dailyChallenges": [ DailyChallenge objects ],
  "focusSessions": [ FocusSession objects ],
  "shadowLog": [ ShadowLog objects ]
}
```

## [S4] DataExportManager

Responsibilities:
- Serialize all DAO data to JSON using Gson (already in dependencies)
- Deserialize JSON back to data objects
- Write JSON to app's external files directory
- Read JSON from file picker URI
- Validate JSON structure and version before import

### Export Flow
1. Query all DAOs for current data
2. Build JSONObject with metadata + data arrays
3. Write to `getExternalFilesDir(null)/phoenix_protocol_backup_<timestamp>.json`
4. Share via Intent.ACTION_SEND

### Import Flow
1. User selects JSON file via file picker (ActivityResultContracts.OpenDocument)
2. Parse JSON, validate version field
3. Show confirmation dialog ("This will replace all current data")
4. Clear all tables (in correct order to respect FK constraints)
5. Insert imported data
6. Show success message

## [S5] Settings Integration

Add to Settings screen "Data & Privacy" section:
- **Export Data** button → triggers export + share
- **Import Data** button → opens file picker → confirm → import

## [S6] Navigation

No new screen — buttons added to existing Settings screen.

## [S7] Localization

| Key | English | Persian |
|-----|---------|---------|
| settings_export_data | Export Data | خروجی داده‌ها |
| settings_import_data | Import Data | ورودی داده‌ها |
| settings_export_success | Data exported successfully | داده‌ها با موفقیت خروجی گرفته شد |
| settings_export_error | Export failed | خروجی ناموفق بود |
| settings_import_confirm | This will replace all current data. Continue? | این تمام داده‌های فعلی رو جایگزین می‌کنه. ادامه؟ |
| settings_import_success | Data imported successfully | داده‌ها با موفقیت وارد شد |
| settings_import_error | Import failed — invalid file | ورودی ناموفق — فایل نامعتبر |
| settings_import_wrong_version | Incompatible backup version | نسخه بکاپ سازگار نیست |

## [S8] Testing Strategy

- Unit test: export produces valid JSON, import parses correctly
- Unit test: import validates version, rejects incompatible files
- Integration test: export → import round-trip preserves data
- Build verification: `./gradlew assembleDebug` after each task
