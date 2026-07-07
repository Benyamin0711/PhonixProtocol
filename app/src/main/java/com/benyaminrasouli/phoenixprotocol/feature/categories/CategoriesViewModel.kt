package com.benyaminrasouli.phoenixprotocol.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Category
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CreateCategoryUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.DeleteCategoryUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val showCreateDialog: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryColor: String = "#FF6B35"
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    val categories: StateFlow<List<Category>> = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    fun showCreateDialog() {
        _uiState.value = CategoriesUiState(showCreateDialog = true)
    }

    fun dismissCreateDialog() {
        _uiState.value = CategoriesUiState()
    }

    fun updateCategoryName(value: String) {
        _uiState.value = _uiState.value.copy(newCategoryName = value)
    }

    fun updateCategoryColor(value: String) {
        _uiState.value = _uiState.value.copy(newCategoryColor = value)
    }

    fun createCategory() {
        val state = _uiState.value
        if (state.newCategoryName.isBlank()) return

        viewModelScope.launch {
            try {
                val category = Category(
                    name = state.newCategoryName,
                    color = state.newCategoryColor
                )
                createCategoryUseCase(category)
            } catch (e: Exception) {
                Log.e("CategoriesViewModel", "Error creating category", e)
            }
            dismissCreateDialog()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(category)
            } catch (e: Exception) {
                Log.e("CategoriesViewModel", "Error deleting category", e)
            }
        }
    }
}
