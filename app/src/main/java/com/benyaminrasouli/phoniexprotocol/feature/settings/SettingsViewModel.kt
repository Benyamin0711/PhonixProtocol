package com.benyaminrasouli.phoniexprotocol.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository
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

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsDataStore.setOnboardingComplete(false)
            userRepository.clearProfile()
            statsRepository.clearStats()
            onComplete()
        }
    }
}
