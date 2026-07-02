package com.benyaminrasouli.phoniexprotocol.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoniexprotocol.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class BossDeadlineCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bossRepository: BossRepository,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val enabled = settingsDataStore.bossAlertsEnabled.first()
            if (enabled) {
                val activeBoss = bossRepository.getActiveBoss()
                if (activeBoss != null) {
                    val deadline = Instant.ofEpochMilli(activeBoss.deadline)
                    val now = Instant.now()
                    val daysUntilDeadline = ChronoUnit.DAYS.between(now, deadline)
                    if (daysUntilDeadline in 0..1) {
                        NotificationHelper.showBossDeadline(applicationContext)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
