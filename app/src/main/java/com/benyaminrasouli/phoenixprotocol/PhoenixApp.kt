package com.benyaminrasouli.phoenixprotocol

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.benyaminrasouli.phoenixprotocol.core.notification.NotificationHelper
import com.benyaminrasouli.phoenixprotocol.di.WorkerModule
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@HiltAndroidApp
class PhoenixApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                if (::workerFactory.isInitialized) workerFactory 
                else EntryPointAccessors.fromApplication(
                    this, 
                    WorkerFactoryEntryPoint::class.java
                ).workerFactory()
            )
            .build()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        
        try {
            val workManager = WorkManager.getInstance(this)
            WorkerModule.enqueueEnergyRecovery(workManager)
            WorkerModule.enqueueDailyChallengeReset(workManager)
            WorkerModule.enqueueTaskReminder(workManager)
            WorkerModule.enqueueBossDeadlineCheck(workManager)
            WorkerModule.enqueueEnergyFullCheck(workManager)
        } catch (e: Exception) {
            android.util.Log.e("PhoenixApp", "Failed to initialize WorkManager", e)
        }
    }
}
