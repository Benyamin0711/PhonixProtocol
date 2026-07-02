package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayTotal
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.FocusSessionDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AnalyticsRepository
import java.util.Calendar
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val focusSessionDao: FocusSessionDao
) : AnalyticsRepository {

    private fun sevenDaysAgo(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        return cal.timeInMillis
    }

    override suspend fun getWeeklyTaskCompletion(): List<DayCount> {
        return taskDao.getCompletedTasksByDay(sevenDaysAgo())
    }

    override suspend fun getXpTrend(): List<DayTotal> {
        return taskDao.getXpByDay(sevenDaysAgo())
    }

    override suspend fun getCategoryBreakdown(): List<CategoryCount> {
        return taskDao.getTaskCountByCategory()
    }

    override suspend fun getWeeklyFocusMinutes(): List<DayTotal> {
        return focusSessionDao.getFocusMinutesByDay(sevenDaysAgo())
    }
}
