package com.benyaminrasouli.phoenixprotocol.di

import com.benyaminrasouli.phoenixprotocol.core.data.repository.AchievementRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.AnalyticsRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.BossRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.DailyChallengeRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.FocusRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.ShadowRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.StatsRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.TaskRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.CategoryRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.TemplateRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.CampaignRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.UserRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AnalyticsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CategoryRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.DailyChallengeRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.FocusRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TemplateRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindShadowRepository(impl: ShadowRepositoryImpl): ShadowRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindBossRepository(impl: BossRepositoryImpl): BossRepository

    @Binds
    @Singleton
    abstract fun bindDailyChallengeRepository(impl: DailyChallengeRepositoryImpl): DailyChallengeRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindFocusRepository(impl: FocusRepositoryImpl): FocusRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindCampaignRepository(impl: CampaignRepositoryImpl): CampaignRepository
}
