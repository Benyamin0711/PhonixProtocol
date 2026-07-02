package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
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
