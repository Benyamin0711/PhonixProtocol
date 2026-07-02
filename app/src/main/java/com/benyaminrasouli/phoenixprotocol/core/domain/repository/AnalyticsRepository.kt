package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayTotal

interface AnalyticsRepository {
    suspend fun getWeeklyTaskCompletion(): List<DayCount>
    suspend fun getXpTrend(): List<DayTotal>
    suspend fun getCategoryBreakdown(): List<CategoryCount>
    suspend fun getWeeklyFocusMinutes(): List<DayTotal>
}
