package com.benyaminrasouli.phoniexprotocol.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CreateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 0,
    val language: String = "en",
    val fullName: String = "",
    val username: String = "",
    val birthYear: String = "",
    val identityPath: String = "",
    val isSaving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val createProfileUseCase: CreateProfileUseCase,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setLanguage(language: String) {
        _state.update { it.copy(language = language) }
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
        }
    }

    fun setFullName(name: String) {
        _state.update { it.copy(fullName = name) }
    }

    fun setUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun setBirthYear(year: String) {
        _state.update { it.copy(birthYear = year) }
    }

    fun setIdentityPath(path: String) {
        _state.update { it.copy(identityPath = path) }
    }

    fun nextStep() {
        _state.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
    }

    fun previousStep() {
        _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.fullName.isBlank() || s.username.isBlank() || s.identityPath.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val profile = UserProfile(
                fullName = s.fullName.trim(),
                username = s.username.trim(),
                birthYear = s.birthYear.toIntOrNull(),
                identityPath = s.identityPath
            )
            createProfileUseCase(profile)
            _state.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
