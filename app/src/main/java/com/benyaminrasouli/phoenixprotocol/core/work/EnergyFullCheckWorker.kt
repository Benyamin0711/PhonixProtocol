package com.benyaminrasouli.phoenixprotocol.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class EnergyFullCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val enabled = settingsDataStore.energyNotificationsEnabled.first()
            if (enabled) {
                val stats = statsRepository.getStatsOnce()
                if (stats != null && stats.phoenixEnergy >= 100) {
                    NotificationHelper.showEnergyFull(applicationContext)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
