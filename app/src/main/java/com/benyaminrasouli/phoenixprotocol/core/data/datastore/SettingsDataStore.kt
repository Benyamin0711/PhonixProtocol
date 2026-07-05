package com.benyaminrasouli.phoenixprotocol.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "phoenix_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val DAILY_SLOGAN_INDEX = intPreferencesKey("daily_slogan_index")
        val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        val BOSS_ALERTS_ENABLED = booleanPreferencesKey("boss_alerts_enabled")
        val ENERGY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("energy_notifications_enabled")
        val WIDGET_THEME = stringPreferencesKey("widget_theme")
        val WIDGET_CATEGORY_FILTER = longPreferencesKey("widget_category_filter")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val BACKGROUND_LEVEL = intPreferencesKey("background_level")
        val BRIGHTNESS = intPreferencesKey("brightness")
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "en"
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_ONBOARDING_COMPLETE] ?: false
    }

    val dailySloganIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_SLOGAN_INDEX] ?: 0
    }

    val taskRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.TASK_REMINDERS_ENABLED] ?: false
    }

    val bossAlertsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.BOSS_ALERTS_ENABLED] ?: false
    }

    val energyNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ENERGY_NOTIFICATIONS_ENABLED] ?: false
    }

    val widgetTheme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.WIDGET_THEME] ?: "DARK"
    }

    val widgetCategoryFilter: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.WIDGET_CATEGORY_FILTER] ?: -1L
    }

    val accentColor: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACCENT_COLOR] ?: "orange"
    }

    val backgroundLevel: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.BACKGROUND_LEVEL] ?: 0
    }

    val brightness: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.BRIGHTNESS] ?: 100
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun setDailySloganIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_SLOGAN_INDEX] = index
        }
    }

    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASK_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setBossAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BOSS_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setEnergyNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENERGY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setWidgetTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WIDGET_THEME] = theme
        }
    }

    suspend fun setWidgetCategoryFilter(categoryId: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WIDGET_CATEGORY_FILTER] = categoryId
        }
    }

    suspend fun setAccentColor(color: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCENT_COLOR] = color
        }
    }

    suspend fun setBackgroundLevel(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_LEVEL] = level
        }
    }

    suspend fun setBrightness(brightness: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BRIGHTNESS] = brightness
        }
    }
}
