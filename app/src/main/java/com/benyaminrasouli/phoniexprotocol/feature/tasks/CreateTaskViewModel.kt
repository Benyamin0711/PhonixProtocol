package com.benyaminrasouli.phoniexprotocol.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.model.Difficulty
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskRecurrence
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskType
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CheckAchievementsUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CreateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTaskState(
    val title: String = "",
    val description: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val category: String = "",
    val taskType: TaskType = TaskType.CUSTOM,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val isPriority: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskState())
    val state: StateFlow<CreateTaskState> = _state.asStateFlow()

    fun setTitle(title: String) = _state.update { it.copy(title = title) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setDifficulty(d: Difficulty) = _state.update { it.copy(difficulty = d) }
    fun setCategory(c: String) = _state.update { it.copy(category = c) }
    fun setTaskType(t: TaskType) = _state.update { it.copy(taskType = t) }
    fun setRecurrence(r: TaskRecurrence) = _state.update { it.copy(recurrence = r) }
    fun setPriority(p: Boolean) = _state.update { it.copy(isPriority = p) }

    fun saveTask(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val task = Task(
                title = s.title.trim(),
                description = s.description.trim(),
                difficulty = s.difficulty.name,
                category = s.category.ifBlank { "General" },
                xpValue = s.difficulty.xpValue,
                recurrence = s.recurrence.name,
                status = "PENDING",
                taskType = s.taskType.name,
                isPriority = s.isPriority
            )
            createTaskUseCase(task)
            checkAchievementsUseCase()
            _state.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
