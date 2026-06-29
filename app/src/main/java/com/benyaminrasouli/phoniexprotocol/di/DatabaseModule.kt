package com.benyaminrasouli.phoniexprotocol.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN taskType TEXT NOT NULL DEFAULT 'CUSTOM'")
            db.execSQL("ALTER TABLE tasks ADD COLUMN isPriority INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS achievements (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, icon TEXT NOT NULL, rarity TEXT NOT NULL, condition TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS user_achievements (achievementId TEXT NOT NULL PRIMARY KEY, unlockedAt INTEGER NOT NULL)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhoenixDatabase {
        return Room.databaseBuilder(
            context,
            PhoenixDatabase::class.java,
            "phoenix_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideUserProfileDao(db: PhoenixDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideTaskDao(db: PhoenixDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideUserStatsDao(db: PhoenixDatabase): UserStatsDao = db.userStatsDao()

    @Provides
    fun provideAchievementDao(db: PhoenixDatabase): AchievementDao = db.achievementDao()
}
