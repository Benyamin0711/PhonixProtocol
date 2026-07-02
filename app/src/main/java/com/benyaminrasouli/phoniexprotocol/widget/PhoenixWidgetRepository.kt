package com.benyaminrasouli.phoniexprotocol.widget

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class WidgetData(
    val activeTasks: List<WidgetTask>,
    val completedCount: Int,
    val totalCount: Int,
    val energy: Int,
    val streak: Int
)

data class WidgetTask(
    val title: String,
    val isCompleted: Boolean
)

@Singleton
class PhoenixWidgetRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val userStatsDao: UserStatsDao
) {
    suspend fun getWidgetData(categoryId: Long = -1L): WidgetData {
        val allTasks = taskDao.getAllTasks().first()
        val filteredTasks = if (categoryId > 0) {
            allTasks.filter { it.categoryId == categoryId }
        } else {
            allTasks
        }
        val stats = userStatsDao.getStatsOnce()

        val activeTasks = filteredTasks
            .filter { it.status != "COMPLETED" && it.status != "SKIPPED" }
            .take(4)
            .map { WidgetTask(title = it.title, isCompleted = false) }

        val completedTasks = filteredTasks.filter { it.status == "COMPLETED" }
        val todayCompleted = completedTasks.count {
            val today = java.time.LocalDate.now().toString()
            it.completedAt?.let { completedAt ->
                java.time.Instant.ofEpochMilli(completedAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().toString() == today
            } ?: false
        }

        return WidgetData(
            activeTasks = activeTasks,
            completedCount = todayCompleted,
            totalCount = filteredTasks.size,
            energy = stats?.phoenixEnergy ?: 0,
            streak = stats?.currentStreak ?: 0
        )
    }
}
