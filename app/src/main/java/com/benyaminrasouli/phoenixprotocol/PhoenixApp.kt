package com.benyaminrasouli.phoenixprotocol

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.benyaminrasouli.phoenixprotocol.core.notification.NotificationHelper
import com.benyaminrasouli.phoenixprotocol.di.WorkerModule
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhoenixApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            NotificationHelper.createChannels(this)
        } catch (e: Exception) {
            android.util.Log.e("PhoenixApp", "Notification channels creation failed", e)
        }
    }
}
