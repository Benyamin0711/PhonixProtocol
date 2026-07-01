package com.benyaminrasouli.phoniexprotocol.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CancelTaskUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CheckAchievementsUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CompleteTaskUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.SkipTaskUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilter { ALL, ACTIVE, COMPLETED, SKIPPED }

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
    private val skipTaskUseCase: SkipTaskUseCase,
    private val cancelTaskUseCase: CancelTaskUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                taskRepository.getAllTasks(),
                _filter
            ) { tasks, filter ->
                val filtered = when (filter) {
                    TaskFilter.ALL -> tasks
                    TaskFilter.ACTIVE -> tasks.filter { it.status != "COMPLETED" && it.status != "SKIPPED" }
                    TaskFilter.COMPLETED -> tasks.filter { it.status == "COMPLETED" }
                    TaskFilter.SKIPPED -> tasks.filter { it.status == "SKIPPED" }
                }
                TaskListState(tasks = filtered, filter = filter)
            }.collect { _state.value = it }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            completeTaskUseCase(task)
            checkAchievementsUseCase()
        }
    }

    fun skipTask(task: Task) {
        viewModelScope.launch {
            skipTaskUseCase(task)
        }
    }

    fun cancelTask(task: Task) {
        viewModelScope.launch {
            cancelTaskUseCase(task)
        }
    }
}