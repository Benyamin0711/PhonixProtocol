package com.benyaminrasouli.phoniexprotocol.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.RecoverEnergyUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EnergyRecoveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recoverEnergyUseCase: RecoverEnergyUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            recoverEnergyUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
