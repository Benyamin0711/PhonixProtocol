package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns WHERE id = 1")
    fun getCampaign(): Flow<Campaign?>

    @Query("SELECT * FROM campaigns WHERE id = 1")
    suspend fun getCampaignOnce(): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Query("SELECT * FROM campaign_days WHERE dayNumber = :day")
    suspend fun getDay(day: Int): CampaignDay?

    @Query("SELECT * FROM campaign_days")
    fun getAllDays(): Flow<List<CampaignDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: CampaignDay)

    @Update
    suspend fun updateDay(day: CampaignDay)

    @Query("SELECT * FROM daily_missions WHERE isActive = 1")
    fun getMissions(): Flow<List<DailyMission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: DailyMission)

    @Query("SELECT * FROM prayers WHERE isActive = 1")
    fun getPrayers(): Flow<List<Prayer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayer: Prayer)
}
