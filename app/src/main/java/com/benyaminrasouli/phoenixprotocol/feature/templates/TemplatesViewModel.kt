package com.benyaminrasouli.phoenixprotocol.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CreateTemplateUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.DeleteTemplateUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetTemplatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemplatesUiState(
    val showCreateDialog: Boolean = false,
    val newTemplateName: String = "",
    val newTemplateDescription: String = "",
    val newTemplateDifficulty: String = "EASY",
    val newTemplateCategory: String = "General",
    val newTemplateRecurrence: String = "NONE",
    val newTemplateTaskType: String = "CUSTOM",
    val newTemplateIsPriority: Boolean = false
)

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    getTemplatesUseCase: GetTemplatesUseCase,
    private val createTemplateUseCase: CreateTemplateUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase
) : ViewModel() {

    val templates: StateFlow<List<Template>> = getTemplatesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState

    fun showCreateDialog() {
        _uiState.value = TemplatesUiState(showCreateDialog = true)
    }

    fun dismissCreateDialog() {
        _uiState.value = TemplatesUiState()
    }

    fun updateTemplateName(value: String) {
        _uiState.value = _uiState.value.copy(newTemplateName = value)
    }

    fun updateTemplateDescription(value: String) {
        _uiState.value = _uiState.value.copy(newTemplateDescription = value)
    }

    fun updateTemplateDifficulty(value: String) {
        _uiState.value = _uiState.value.copy(newTemplateDifficulty = value)
    }

    fun updateTemplateCategory(value: String) {
        _uiState.value = _uiState.value.copy(newTemplateCategory = value)
    }

    fun updateTemplateRecurrence(value: String) {
        _uiState.value = _uiState.value.copy(newTemplateRecurrence = value)
    }

    fun updateTemplateTaskType(value: String) {
        _uiState.value = _uiState.value.copy(newTemplateTaskType = value)
    }

    fun updateTemplateIsPriority(value: Boolean) {
        _uiState.value = _uiState.value.copy(newTemplateIsPriority = value)
    }

    fun createTemplate() {
        val state = _uiState.value
        if (state.newTemplateName.isBlank()) return

        viewModelScope.launch {
            val template = Template(
                title = state.newTemplateName,
                description = state.newTemplateDescription,
                difficulty = state.newTemplateDifficulty,
                category = state.newTemplateCategory,
                recurrence = state.newTemplateRecurrence,
                taskType = state.newTemplateTaskType,
                isPriority = state.newTemplateIsPriority
            )
            createTemplateUseCase(template)
            dismissCreateDialog()
        }
    }

    fun deleteTemplate(template: Template) {
        viewModelScope.launch {
            deleteTemplateUseCase(template)
        }
    }
}
