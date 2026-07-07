package com.benyaminrasouli.phoenixprotocol.feature.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.FocusSession
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.FocusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimerMode { POMODORO, CUSTOM }

data class FocusTimerState(
    val mode: TimerMode = TimerMode.POMODORO,
    val selectedDuration: Int = 25,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val totalFocusSeconds: Int = 0,
    val completedSessions: Int = 0
) {
    val remainingSeconds: Int
        get() = (selectedDuration * 60) - elapsedSeconds

    val progress: Float
        get() {
            val total = selectedDuration * 60
            return if (total > 0) elapsedSeconds.toFloat() / total else 0f
        }
}

@HiltViewModel
class FocusTimerViewModel @Inject constructor(
    private val focusRepository: FocusRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FocusTimerState())
    val state: StateFlow<FocusTimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadStatsFromDb()
    }

    private fun loadStatsFromDb() {
        viewModelScope.launch {
            val totalSeconds = focusRepository.getTotalFocusTimeSeconds()
            val completedCount = focusRepository.getCompletedSessionCount()
            _state.update {
                it.copy(
                    totalFocusSeconds = totalSeconds.toInt(),
                    completedSessions = completedCount
                )
            }
        }
    }

    fun setMode(mode: TimerMode) {
        if (_state.value.isRunning) return
        _state.update { it.copy(mode = mode) }
    }

    fun setDuration(minutes: Int) {
        if (_state.value.isRunning) return
        _state.update { it.copy(selectedDuration = minutes, elapsedSeconds = 0) }
    }

    fun start() {
        if (_state.value.isRunning) return
        _state.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            val durationMinutes = _state.value.selectedDuration
            val modeName = _state.value.mode.name
            val startedAt = System.currentTimeMillis()
            while (_state.value.isRunning && _state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
            if (_state.value.remainingSeconds <= 0) {
                val session = FocusSession(
                    startedAt = startedAt,
                    endedAt = System.currentTimeMillis(),
                    durationSeconds = durationMinutes * 60,
                    completed = true,
                    mode = modeName
                )
                focusRepository.insertSession(session)
                loadStatsFromDb()
                _state.update {
                    it.copy(
                        isRunning = false,
                        elapsedSeconds = 0
                    )
                }
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(isRunning = false) }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(isRunning = false, elapsedSeconds = 0) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
