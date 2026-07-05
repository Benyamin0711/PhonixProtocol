package com.benyaminrasouli.phoenixprotocol.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.export.DataExportManager
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExportImportResult {
    EXPORT_SUCCESS, EXPORT_ERROR,
    IMPORT_SUCCESS, IMPORT_ERROR
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    val language: StateFlow<String> = settingsDataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val profile: Flow<UserProfile?> = userRepository.getProfile()

    val taskRemindersEnabled: StateFlow<Boolean> = settingsDataStore.taskRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bossAlertsEnabled: StateFlow<Boolean> = settingsDataStore.bossAlertsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val energyNotificationsEnabled: StateFlow<Boolean> = settingsDataStore.energyNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val accentColor: StateFlow<String> = settingsDataStore.accentColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "orange")

    val backgroundLevel: StateFlow<Int> = settingsDataStore.backgroundLevel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val brightness: StateFlow<Int> = settingsDataStore.brightness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    private val _exportMessage = MutableStateFlow<ExportImportResult?>(null)
    val exportMessage: StateFlow<ExportImportResult?> = _exportMessage.asStateFlow()

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
        }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setTaskRemindersEnabled(enabled)
        }
    }

    fun setBossAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setBossAlertsEnabled(enabled)
        }
    }

    fun setEnergyNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setEnergyNotificationsEnabled(enabled)
        }
    }

    fun setAccentColor(color: String) {
        viewModelScope.launch {
            settingsDataStore.setAccentColor(color)
        }
    }

    fun setBackgroundLevel(level: Int) {
        viewModelScope.launch {
            settingsDataStore.setBackgroundLevel(level)
        }
    }

    fun setBrightness(brightness: Int) {
        viewModelScope.launch {
            settingsDataStore.setBrightness(brightness)
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                settingsDataStore.setOnboardingComplete(false)
                userRepository.clearProfile()
                statsRepository.clearStats()
            } catch (_: Exception) {
            }
            onComplete()
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val result = dataExportManager.export()
            _exportMessage.value = if (result.success) {
                ExportImportResult.EXPORT_SUCCESS
            } else {
                ExportImportResult.EXPORT_ERROR
            }
        }
    }

    fun importData(jsonString: String) {
        viewModelScope.launch {
            val result = dataExportManager.import(jsonString)
            _exportMessage.value = if (result.success) {
                ExportImportResult.IMPORT_SUCCESS
            } else {
                ExportImportResult.IMPORT_ERROR
            }
        }
    }

    fun clearMessage() {
        _exportMessage.value = null
    }
}
