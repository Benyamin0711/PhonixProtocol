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
enum class SessionPhase { WORK, SHORT_BREAK, LONG_BREAK }

data class FocusTimerState(
    val mode: TimerMode = TimerMode.POMODORO,
    val selectedDuration: Int = 25,
    val phase: SessionPhase = SessionPhase.WORK,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isFullScreen: Boolean = false,
    val strictMode: Boolean = false,
    val isWarningActive: Boolean = false,
    val warningCountdown: Int = 7,
    val lastWarningDismissTime: Long = 0L,
    val sessionsCompleted: Int = 0,
    val totalFocusSeconds: Int = 0,
    val completedSessions: Int = 0
) {
    val totalDurationSeconds: Int
        get() = when (mode) {
            TimerMode.POMODORO -> selectedDuration * 60
            TimerMode.CUSTOM -> selectedDuration * 60
        }

    val remainingSeconds: Int
        get() = totalDurationSeconds - elapsedSeconds

    val progress: Float
        get() = if (totalDurationSeconds > 0) elapsedSeconds.toFloat() / totalDurationSeconds else 0f

    val displayMinutes: Int
        get() = remainingSeconds / 60

    val displaySeconds: Int
        get() = remainingSeconds % 60
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
        const val SHORT_BREAK_MINUTES = 5
        const val LONG_BREAK_MINUTES = 15
        const val SESSIONS_BEFORE_LONG_BREAK = 4
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

    fun setMode(mode: TimerMode) {
        if (_state.value.isRunning) return
        _state.update { it.copy(mode = mode, phase = SessionPhase.WORK, elapsedSeconds = 0) }
    }

    fun setDuration(minutes: Int) {
        if (_state.value.isRunning) return
        _state.update { it.copy(selectedDuration = minutes, elapsedSeconds = 0) }
    }

    fun start() {
        if (_state.value.isRunning) return
        _state.update { it.copy(isRunning = true) }
        startTimerJob()
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            while (_state.value.isRunning && _state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
            if (_state.value.remainingSeconds <= 0) {
                onTimerComplete(startedAt)
            }
        }
    }

    private fun onTimerComplete(startedAt: Long) {
        val currentState = _state.value

        // Save work session to DB
        if (currentState.phase == SessionPhase.WORK) {
            viewModelScope.launch {
                val session = FocusSession(
                    startedAt = startedAt,
                    endedAt = System.currentTimeMillis(),
                    durationSeconds = currentState.totalDurationSeconds,
                    completed = true,
                    mode = currentState.mode.name
                )
                focusRepository.insertSession(session)
                loadStatsFromDb()
            }
        }

        // Handle phase transition
        val newSessionsCompleted = if (currentState.phase == SessionPhase.WORK) {
            currentState.sessionsCompleted + 1
        } else {
            currentState.sessionsCompleted
        }

        val nextPhase = when (currentState.phase) {
            SessionPhase.WORK -> {
                if (newSessionsCompleted % SESSIONS_BEFORE_LONG_BREAK == 0) {
                    SessionPhase.LONG_BREAK
                } else {
                    SessionPhase.SHORT_BREAK
                }
            }
            SessionPhase.SHORT_BREAK, SessionPhase.LONG_BREAK -> SessionPhase.WORK
        }

        val nextDuration = when (nextPhase) {
            SessionPhase.WORK -> currentState.selectedDuration
            SessionPhase.SHORT_BREAK -> SHORT_BREAK_MINUTES
            SessionPhase.LONG_BREAK -> LONG_BREAK_MINUTES
        }

        _state.update {
            it.copy(
                isRunning = false,
                elapsedSeconds = 0,
                phase = nextPhase,
                selectedDuration = nextDuration,
                sessionsCompleted = newSessionsCompleted
            )
        }

        // Auto-start next phase
        start()
    }

    fun pause() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(isRunning = false) }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _state.update {
            it.copy(
                isRunning = false,
                elapsedSeconds = 0,
                phase = SessionPhase.WORK,
                selectedDuration = if (it.mode == TimerMode.POMODORO) 25 else it.selectedDuration
            )
        }
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
        timerJob?.cancel()
        startWarningCountdown()
    }

    private fun startWarningCountdown() {
        warningJob?.cancel()
        warningJob = viewModelScope.launch {
            while (_state.value.isWarningActive && _state.value.warningCountdown > 0) {
                delay(1000)
                _state.update { it.copy(warningCountdown = it.warningCountdown - 1) }
            }
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
        startTimerJob()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        warningJob?.cancel()
    }
}
