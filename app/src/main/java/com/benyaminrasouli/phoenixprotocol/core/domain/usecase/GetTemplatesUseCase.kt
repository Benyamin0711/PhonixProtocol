package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTemplatesUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    operator fun invoke(): Flow<List<Template>> {
        return repository.getAllTemplates()
    }
}
