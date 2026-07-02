package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.CategoryDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Category
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override suspend fun createCategory(category: Category): Long {
        return dao.insertCategory(category)
    }

    override suspend fun updateCategory(category: Category) {
        dao.updateCategory(category)
    }

    override suspend fun deleteCategory(category: Category) {
        dao.deleteCategory(category)
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return dao.getAllCategories()
    }

    override suspend fun getCategoryById(categoryId: Long): Category? {
        return dao.getCategoryById(categoryId)
    }
}
