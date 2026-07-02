package com.benyaminrasouli.phoenixprotocol.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.FocusSession
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.FocusRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusTimerService : android.app.Service() {

    @Inject
    lateinit var focusRepository: FocusRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timerJob: Job? = null
    private var currentSessionId: Long? = null

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.focus_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.focus_notification_channel_description)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startTimer() {
        if (_isRunning.value) return

        _isRunning.value = true

        serviceScope.launch {
            val session = FocusSession(
                startedAt = System.currentTimeMillis(),
                durationSeconds = 0,
                completed = false
            )
            currentSessionId = focusRepository.insertSession(session)
        }

        timerJob = serviceScope.launch {
            startForeground(NOTIFICATION_ID, buildNotification(0))
            while (_isRunning.value) {
                delay(1000L)
                _elapsedSeconds.value++
                updateNotification(_elapsedSeconds.value)
            }
        }
    }

    private fun stopTimer() {
        _isRunning.value = false
        timerJob?.cancel()

        val elapsed = _elapsedSeconds.value
        if (elapsed > 0 && currentSessionId != null) {
            serviceScope.launch {
                currentSessionId?.let { sessionId ->
                    val session = focusRepository.getSessionById(sessionId)
                    session?.let {
                        focusRepository.updateSession(
                            it.copy(
                                endedAt = System.currentTimeMillis(),
                                durationSeconds = elapsed,
                                completed = true
                            )
                        )
                    }
                }
            }
        }

        _elapsedSeconds.value = 0
        currentSessionId = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(elapsedSeconds: Int): android.app.Notification {
        val timeString = formatTime(elapsedSeconds)
        val stopIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this, 0, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.focus_notification_title))
            .setContentText(timeString)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.focus_stop), stopPendingIntent)
            .build()
    }

    private fun updateNotification(elapsedSeconds: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(elapsedSeconds))
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    companion object {
        const val ACTION_START = "com.benyaminrasouli.phoenixprotocol.ACTION_START_FOCUS"
        const val ACTION_STOP = "com.benyaminrasouli.phoenixprotocol.ACTION_STOP_FOCUS"

        private const val CHANNEL_ID = "focus_timer_channel"
        private const val NOTIFICATION_ID = 1004
    }
}
