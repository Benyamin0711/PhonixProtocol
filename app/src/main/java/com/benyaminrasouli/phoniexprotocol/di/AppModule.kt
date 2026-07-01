package com.benyaminrasouli.phoniexprotocol.di

import com.benyaminrasouli.phoniexprotocol.core.data.repository.AchievementRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.BossRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.DailyChallengeRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.StatsRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.TaskRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.UserRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.DailyChallengeRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.UserRepository
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
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindBossRepository(impl: BossRepositoryImpl): BossRepository

    @Binds
    @Singleton
    abstract fun bindDailyChallengeRepository(impl: DailyChallengeRepositoryImpl): DailyChallengeRepository
}
