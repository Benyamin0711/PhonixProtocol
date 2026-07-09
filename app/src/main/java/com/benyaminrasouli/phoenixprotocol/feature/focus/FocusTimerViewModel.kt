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
    val completedSessions: Int = 0,
    val isFullScreen: Boolean = false,
    val strictMode: Boolean = false,
    val isWarningActive: Boolean = false,
    val warningCountdown: Int = 7,
    val lastWarningDismissTime: Long = 0L
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
    private var warningJob: Job? = null

    companion object {
        const val WARNING_GRACE_MILLIS = 2000L
    }

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

    // FIX: وقتی مود عوض شد، اگه Pomodoro هست duration رو ۲۵ کن و elapsed رو صفر
    fun setMode(mode: TimerMode) {
        if (_state.value.isRunning) return
        _state.update {
            it.copy(
                mode = mode,
                selectedDuration = if (mode == TimerMode.POMODORO) 25 else it.selectedDuration,
                elapsedSeconds = 0
            )
        }
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
            // فقط اگه تایمر به صفر رسیده (نه pause شده)
            if (_state.value.remainingSeconds <= 0 && !_state.value.isWarningActive) {
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

    fun toggleFullScreen() {
        _state.update { it.copy(isFullScreen = !it.isFullScreen) }
    }

    fun toggleStrictMode() {
        _state.update { it.copy(strictMode = !it.strictMode) }
    }

    fun onPhonePickupDetected() {
        val currentState = _state.value
        if (!currentState.strictMode || !currentState.isRunning || currentState.isWarningActive) return

        val now = System.currentTimeMillis()
        if (now < currentState.lastWarningDismissTime + WARNING_GRACE_MILLIS) return

        _state.update {
            it.copy(
                isWarningActive = true,
                warningCountdown = 7,
                isRunning = false
            )
        }
        startWarningCountdown()
    }

    private fun startWarningCountdown() {
        warningJob?.cancel()
        warningJob = viewModelScope.launch {
            while (_state.value.isWarningActive && _state.value.warningCountdown > 0) {
                delay(1000)
                _state.update { it.copy(warningCountdown = it.warningCountdown - 1) }
            }
            // ۷ ثانیه تموم شد و کاربر گوشی رو نذاشت → ریست از اول
            if (_state.value.isWarningActive && _state.value.warningCountdown == 0) {
                _state.update {
                    it.copy(
                        isWarningActive = false,
                        lastWarningDismissTime = System.currentTimeMillis(),
                        isRunning = false,
                        elapsedSeconds = 0
                    )
                }
            }
        }
    }

    fun dismissWarning() {
        warningJob?.cancel()
        _state.update {
            it.copy(
                isWarningActive = false,
                lastWarningDismissTime = System.currentTimeMillis(),
                isRunning = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        warningJob?.cancel()
    }
}