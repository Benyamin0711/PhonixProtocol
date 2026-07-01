package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class SkipTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(task: Task) {
        taskRepository.skipTask(task.id)
        statsRepository.increaseShadowLevel(1)
    }
}
