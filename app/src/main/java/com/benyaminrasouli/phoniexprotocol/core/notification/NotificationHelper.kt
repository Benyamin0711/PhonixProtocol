package com.benyaminrasouli.phoniexprotocol.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.benyaminrasouli.phoniexprotocol.R

object NotificationHelper {

    private const val CHANNEL_TASK_REMINDERS = "task_reminders"
    private const val CHANNEL_BOSS_DEADLINES = "boss_deadlines"
    private const val CHANNEL_ENERGY_FULL = "energy_full"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val taskChannel = NotificationChannel(
                CHANNEL_TASK_REMINDERS,
                context.getString(R.string.notifications_channel_tasks),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val bossChannel = NotificationChannel(
                CHANNEL_BOSS_DEADLINES,
                context.getString(R.string.notifications_channel_boss),
                NotificationManager.IMPORTANCE_HIGH
            )

            val energyChannel = NotificationChannel(
                CHANNEL_ENERGY_FULL,
                context.getString(R.string.notifications_channel_energy),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            manager.createNotificationChannel(taskChannel)
            manager.createNotificationChannel(bossChannel)
            manager.createNotificationChannel(energyChannel)
        }
    }

    fun showTaskReminder(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notifications_task_reminder_title))
            .setContentText(context.getString(R.string.notifications_task_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
    }

    fun showBossDeadline(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_BOSS_DEADLINES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notifications_boss_deadline_title))
            .setContentText(context.getString(R.string.notifications_boss_deadline_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(1002, notification)
    }

    fun showEnergyFull(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ENERGY_FULL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notifications_energy_full_title))
            .setContentText(context.getString(R.string.notifications_energy_full_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(1003, notification)
    }
}
