package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Category
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Long {
        return repository.createCategory(category)
    }
}
