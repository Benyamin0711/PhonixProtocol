package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Query("SELECT * FROM categories WHERE isDefault = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesOnce(): List<Category>

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
