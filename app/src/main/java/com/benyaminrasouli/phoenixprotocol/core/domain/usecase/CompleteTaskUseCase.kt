package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.DailyChallengeRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val trackDailyChallengeUseCase: TrackDailyChallengeUseCase,
    private val shadowRepository: ShadowRepository
) {
    suspend operator fun invoke(task: Task) {
        taskRepository.completeTask(task.id)
        statsRepository.addXp(task.xpValue)
        statsRepository.increasePhoenixEnergy(5)
        statsRepository.incrementCompletedTasks()

        // Shadow reduction
        val shadowReduction = when {
            task.isPriority -> 3
            task.difficulty == "HARD" || task.difficulty == "EXTREME" -> 2
            else -> 1
        }
        shadowRepository.decreaseShadow(shadowReduction)
        shadowRepository.logShadowChange(
            action = "TASK_COMPLETE",
            amount = -shadowReduction,
            description = "Completed: ${task.title}"
        )

        // Track daily challenge progress
        val todayChallenges = dailyChallengeRepository.getTodayChallengesOnce()
        trackDailyChallengeUseCase(
            challenges = todayChallenges,
            completedTaskType = task.difficulty,
            earnedXp = task.xpValue
        )
    }
}
