package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getActiveTasks()
    }

    fun getCompleted(): Flow<List<Task>> {
        return taskRepository.getCompletedTasks()
    }
}
