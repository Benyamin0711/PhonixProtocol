package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ActiveBoss(
    val boss: Boss,
    val goals: List<BossGoal>,
    val progress: Float,
    val isCompleted: Boolean,
    val isFailed: Boolean
)

class GetActiveBossUseCase @Inject constructor(
    private val bossRepository: BossRepository
) {
    operator fun invoke(): Flow<ActiveBoss?> {
        return bossRepository.getActiveBossFlow().map { boss ->
            if (boss == null) return@map null

            val goals = BossGoal.fromJson(boss.goals)
            val isFailed = System.currentTimeMillis() > boss.deadline && boss.status == "ACTIVE"
            val isCompleted = goals.all { it.isCompleted }

            val totalProgress = goals.map { it.progress }.average().toFloat()

            ActiveBoss(
                boss = boss,
                goals = goals,
                progress = totalProgress,
                isCompleted = isCompleted,
                isFailed = isFailed
            )
        }
    }
}
