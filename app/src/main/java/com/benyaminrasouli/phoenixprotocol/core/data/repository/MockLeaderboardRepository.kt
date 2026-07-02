package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.domain.model.LeaderboardEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.model.Rank
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.LeaderboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MockLeaderboardRepository @Inject constructor(
    private val userStatsDao: UserStatsDao
) : LeaderboardRepository {

    private val mockPlayers = listOf(
        MockPlayer("ShadowHunter", "#9C27B0", 48200, 76),
        MockPlayer("PhoenixRider", "#FF6B35", 45100, 72),
        MockPlayer("VoidWalker", "#2196F3", 42800, 68),
        MockPlayer("NightFlame", "#E91E63", 39500, 64),
        MockPlayer("StormBreaker", "#4CAF50", 36200, 59),
        MockPlayer("IronPulse", "#FFC107", 33100, 54),
        MockPlayer("GhostStrike", "#00BCD4", 29800, 49),
        MockPlayer("BlazeMaster", "#FF5722", 26400, 44),
        MockPlayer("FrostEdge", "#607D8B", 23100, 39),
        MockPlayer("ThunderVolt", "#795548", 19800, 33),
        MockPlayer("DarkNova", "#673AB7", 16500, 28),
        MockPlayer("SteelFang", "#8BC34A", 13200, 22),
        MockPlayer("CrimsonAsh", "#F44336", 9900, 17),
        MockPlayer("SilverMist", "#9E9E9E", 6600, 12),
        MockPlayer("EmberSoul", "#FF9800", 3300, 6)
    )

    override fun getAllTimeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val stats = userStatsDao.getStatsOnce()
        val userEntry = LeaderboardEntry(
            id = 0,
            name = "You",
            avatarColor = "#FF6B35",
            xp = stats?.xp ?: 0,
            level = stats?.level ?: 1,
            rank = Rank.forLevel(stats?.level ?: 1),
            isCurrentUser = true
        )
        val allEntries = mockPlayers.map { p ->
            LeaderboardEntry(
                id = p.name.hashCode().toLong(),
                name = p.name,
                avatarColor = p.color,
                xp = p.xp,
                level = p.level,
                rank = Rank.forLevel(p.level)
            )
        } + userEntry
        emit(allEntries.sortedByDescending { it.xp })
    }

    override fun getWeeklyLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val stats = userStatsDao.getStatsOnce()
        val weeklyUserXp = (stats?.xp ?: 0) / 4
        val userEntry = LeaderboardEntry(
            id = 0,
            name = "You",
            avatarColor = "#FF6B35",
            xp = weeklyUserXp,
            level = stats?.level ?: 1,
            rank = Rank.forLevel(stats?.level ?: 1),
            isCurrentUser = true
        )
        val allEntries = mockPlayers.map { p ->
            val weeklyXp = p.xp / 4 + (p.name.hashCode() % 500)
            LeaderboardEntry(
                id = p.name.hashCode().toLong(),
                name = p.name,
                avatarColor = p.color,
                xp = weeklyXp,
                level = p.level,
                rank = Rank.forLevel(p.level)
            )
        } + userEntry
        emit(allEntries.sortedByDescending { it.xp })
    }

    private data class MockPlayer(
        val name: String,
        val color: String,
        val xp: Int,
        val level: Int
    )
}
