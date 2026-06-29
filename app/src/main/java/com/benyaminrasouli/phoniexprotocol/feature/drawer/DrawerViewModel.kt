package com.benyaminrasouli.phoniexprotocol.feature.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.UserRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val profile: Flow<UserProfile?> = userRepository.getProfile()
    val stats: Flow<UserStats?> = statsRepository.getStats()
    val language: Flow<String> = settingsDataStore.language

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
        }
    }
}
