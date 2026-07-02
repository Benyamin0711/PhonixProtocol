package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun createCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(categoryId: Long): Category?
}
