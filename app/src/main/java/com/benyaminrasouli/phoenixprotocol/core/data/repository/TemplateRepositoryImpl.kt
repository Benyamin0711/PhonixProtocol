package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TemplateDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val dao: TemplateDao
) : TemplateRepository {

    override suspend fun createTemplate(template: Template): Long {
        return dao.insertTemplate(template)
    }

    override suspend fun updateTemplate(template: Template) {
        dao.updateTemplate(template)
    }

    override suspend fun deleteTemplate(template: Template) {
        dao.deleteTemplate(template)
    }

    override fun getAllTemplates(): Flow<List<Template>> {
        return dao.getAllTemplates()
    }

    override suspend fun getTemplateById(templateId: Long): Template? {
        return dao.getTemplateById(templateId)
    }
}
