package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTemplatesUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    operator fun invoke(): Flow<List<Template>> {
        return repository.getAllTemplates()
    }
}
