package com.benyaminrasouli.phoenixprotocol.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.benyaminrasouli.phoenixprotocol.MainActivity
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhoenixWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var repository: PhoenixWidgetRepository

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            try {
                val theme = settingsDataStore.widgetTheme.first()
                val categoryId = settingsDataStore.widgetCategoryFilter.first()
                val data = repository.getWidgetData(categoryId)
                val layoutRes = if (theme == "LIGHT") R.layout.widget_phoenix_light else R.layout.widget_phoenix
                val views = RemoteViews(context.packageName, layoutRes)

                views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
                views.setTextViewText(R.id.widget_energy, context.getString(R.string.widget_energy, data.energy))

                views.removeAllViews(R.id.widget_task_list)

                if (data.activeTasks.isEmpty()) {
                    val noTasksView = RemoteViews(context.packageName, R.layout.widget_task_item)
                    noTasksView.setTextViewText(R.id.task_title, context.getString(R.string.widget_no_tasks))
                    noTasksView.setTextColor(R.id.task_title, 0xFFAAAAAA.toInt())
                    views.addView(R.id.widget_task_list, noTasksView)
                } else {
                    for (task in data.activeTasks) {
                        val taskView = RemoteViews(context.packageName, R.layout.widget_task_item)
                        taskView.setTextViewText(R.id.task_title, task.title)
                        taskView.setTextViewText(R.id.task_status, context.getString(R.string.widget_task_pending))
                        views.addView(R.id.widget_task_list, taskView)
                    }
                }

                views.setTextViewText(R.id.widget_progress, context.getString(R.string.widget_progress, data.completedCount, data.totalCount))
                views.setTextViewText(R.id.widget_streak, context.getString(R.string.widget_streak, data.streak))

                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        PhoenixWidgetWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        PhoenixWidgetWorker.cancel(context)
    }
}
