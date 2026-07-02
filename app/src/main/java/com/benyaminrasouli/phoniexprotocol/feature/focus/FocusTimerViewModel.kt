package com.benyaminrasouli.phoniexprotocol.feature.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

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
class FocusTimerViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FocusTimerState())
    val state: StateFlow<FocusTimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

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
            while (_state.value.isRunning && _state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
            if (_state.value.remainingSeconds <= 0) {
                _state.update {
                    it.copy(
                        isRunning = false,
                        totalFocusSeconds = it.totalFocusSeconds + it.selectedDuration * 60,
                        completedSessions = it.completedSessions + 1,
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
