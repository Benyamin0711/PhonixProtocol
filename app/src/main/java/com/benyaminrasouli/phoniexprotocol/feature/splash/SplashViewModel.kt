package com.benyaminrasouli.phoniexprotocol.feature.splash

import androidx.lifecycle.ViewModel
import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    suspend fun isOnboardingComplete(): Boolean {
        return settingsDataStore.isOnboardingComplete.first()
    }
}
