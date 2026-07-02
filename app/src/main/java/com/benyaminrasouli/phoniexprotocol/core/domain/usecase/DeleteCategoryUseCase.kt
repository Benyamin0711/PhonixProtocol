package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Category
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) {
        repository.deleteCategory(category)
    }
}
