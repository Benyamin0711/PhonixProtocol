package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    suspend fun createTemplate(template: Template): Long
    suspend fun updateTemplate(template: Template)
    suspend fun deleteTemplate(template: Template)
    fun getAllTemplates(): Flow<List<Template>>
    suspend fun getTemplateById(templateId: Long): Template?
}
