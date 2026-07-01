package com.benyaminrasouli.phoniexprotocol

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.benyaminrasouli.phoniexprotocol.di.WorkerModule
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhoenixApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkerModule.enqueueEnergyRecovery(workManager)
        WorkerModule.enqueueDailyChallengeReset(workManager)
    }
}
