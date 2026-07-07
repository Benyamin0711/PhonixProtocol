package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.domain.model.Slogans
import android.util.Log
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetSloganUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    suspend operator fun invoke(): String {
        val index = settingsDataStore.dailySloganIndex
            .firstOr(0)
        val slogan = Slogans.getDailySlogan(index)
        settingsDataStore.setDailySloganIndex((index + 1) % Slogans.sloganCount)
        return slogan
    }

    private suspend fun kotlinx.coroutines.flow.Flow<Int>.firstOr(default: Int): Int {
        return try {
            first()
        } catch (e: Exception) {
            Log.e("GetSloganUseCase", "Error getting slogan index", e)
            default
        }
    }
}
