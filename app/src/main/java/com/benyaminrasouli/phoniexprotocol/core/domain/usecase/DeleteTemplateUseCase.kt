package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TemplateRepository
import javax.inject.Inject

class DeleteTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(template: Template) {
        repository.deleteTemplate(template)
    }
}
