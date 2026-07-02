package com.benyaminrasouli.phoenixprotocol.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.domain.model.IdentityPath
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val fullName: String = "",
    val username: String = "",
    val birthYear: String = "",
    val identityPath: String = IdentityPath.WARRIOR.name,
    val level: Int = 1,
    val rank: String = "",
    val xp: Int = 0,
    val streak: Int = 0,
    val completedTasks: Int = 0,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val profileId: Long = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = userRepository.getProfile().first()
            val stats = statsRepository.getStats().first()
            _state.value = ProfileUiState(
                fullName = profile?.fullName ?: "",
                username = profile?.username ?: "",
                birthYear = profile?.birthYear?.toString() ?: "",
                identityPath = profile?.identityPath ?: IdentityPath.WARRIOR.name,
                level = stats?.level ?: 1,
                rank = stats?.rank ?: "INITIATE",
                xp = stats?.xp ?: 0,
                streak = stats?.currentStreak ?: 0,
                completedTasks = stats?.completedTasks ?: 0,
                profileId = profile?.id ?: 0
            )
        }
    }

    fun toggleEdit() {
        _state.value = _state.value.copy(isEditing = !_state.value.isEditing)
    }

    fun updateFullName(value: String) {
        _state.value = _state.value.copy(fullName = value)
    }

    fun updateUsername(value: String) {
        _state.value = _state.value.copy(username = value)
    }

    fun updateBirthYear(value: String) {
        _state.value = _state.value.copy(birthYear = value)
    }

    fun updateIdentityPath(value: String) {
        _state.value = _state.value.copy(identityPath = value)
    }

    fun saveProfile() {
        val currentState = _state.value
        if (currentState.fullName.isBlank() || currentState.username.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val profile = UserProfile(
                id = currentState.profileId,
                fullName = currentState.fullName,
                username = currentState.username,
                birthYear = currentState.birthYear.toIntOrNull(),
                identityPath = currentState.identityPath
            )
            userRepository.updateProfile(profile)
            _state.value = _state.value.copy(isEditing = false, isSaving = false)
        }
    }
}
