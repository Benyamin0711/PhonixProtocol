package com.benyaminrasouli.phoniexprotocol

import android.app.Application
import androidx.work.WorkManager
import com.benyaminrasouli.phoniexprotocol.di.WorkerModule
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhoenixApp : Application() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()
        WorkerModule.enqueueEnergyRecovery(workManager)
        WorkerModule.enqueueDailyChallengeReset(workManager)
    }
}
