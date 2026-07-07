package com.benyaminrasouli.phoenixprotocol.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Category
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.domain.model.Difficulty
import com.benyaminrasouli.phoenixprotocol.core.domain.model.TaskRecurrence
import com.benyaminrasouli.phoenixprotocol.core.domain.model.TaskType
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CategoryRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CheckAchievementsUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CreateTaskUseCase
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTaskState(
    val title: String = "",
    val description: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val category: String = "",
    val selectedCategoryId: Long? = null,
    val taskType: TaskType = TaskType.CUSTOM,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val isPriority: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskState())
    val state: StateFlow<CreateTaskState> = _state.asStateFlow()

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTitle(title: String) = _state.update { it.copy(title = title) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setDifficulty(d: Difficulty) = _state.update { it.copy(difficulty = d) }
    fun setCategory(category: Category) = _state.update {
        it.copy(category = category.name, selectedCategoryId = category.id)
    }
    fun setTaskType(t: TaskType) = _state.update { it.copy(taskType = t) }
    fun setRecurrence(r: TaskRecurrence) = _state.update { it.copy(recurrence = r) }
    fun setPriority(p: Boolean) = _state.update { it.copy(isPriority = p) }

    fun saveTask(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val task = Task(
                    title = s.title.trim(),
                    description = s.description.trim(),
                    difficulty = s.difficulty.name,
                    category = s.category.ifBlank { "General" },
                    categoryId = s.selectedCategoryId,
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
            } catch (e: Exception) {
                Log.e("CreateTaskViewModel", "Error saving task", e)
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
