package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TemplateRepository
import javax.inject.Inject

class CreateTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(template: Template): Long {
        return repository.createTemplate(template)
    }
}
