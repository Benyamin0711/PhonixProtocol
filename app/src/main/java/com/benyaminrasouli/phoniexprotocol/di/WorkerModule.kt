package com.benyaminrasouli.phoniexprotocol.di

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.benyaminrasouli.phoniexprotocol.core.work.EnergyRecoveryWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    fun enqueueEnergyRecovery(workManager: WorkManager) {
        val request = PeriodicWorkRequestBuilder<EnergyRecoveryWorker>(
            30, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "energy_recovery",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
