package com.benyaminrasouli.phoenixprotocol.core.data.export

import android.content.Context
import androidx.room.withTransaction
import com.benyaminrasouli.phoenixprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.*
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class ExportResult(val success: Boolean, val filePath: String? = null, val error: String? = null)
data class ImportResult(val success: Boolean, val error: String? = null)

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PhoenixDatabase,
    private val taskDao: TaskDao,
    private val userProfileDao: UserProfileDao,
    private val userStatsDao: UserStatsDao,
    private val achievementDao: AchievementDao,
    private val bossDao: BossDao,
    private val dailyChallengeDao: DailyChallengeDao,
    private val templateDao: TemplateDao,
    private val categoryDao: CategoryDao,
    private val focusSessionDao: FocusSessionDao,
    private val shadowLogDao: ShadowLogDao,
    private val campaignDao: CampaignDao
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val exportVersion = 12

    suspend fun export(): ExportResult {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put("version", exportVersion)
            jsonObject.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()))
            jsonObject.put("appName", "PhoenixProtocol")

            userProfileDao.getProfileOnce()?.let { jsonObject.put("profile", JSONObject(json.encodeToString(it))) }
            userStatsDao.getStatsOnce()?.let { jsonObject.put("stats", JSONObject(json.encodeToString(it))) }
            jsonObject.put("tasks", JSONArray(json.encodeToString(taskDao.getAllTasksOnce())))
            jsonObject.put("categories", JSONArray(json.encodeToString(categoryDao.getAllCategoriesOnce())))
            jsonObject.put("templates", JSONArray(json.encodeToString(templateDao.getAllTemplatesOnce())))
            jsonObject.put("achievements", JSONArray(json.encodeToString(achievementDao.getAllAchievementsOnce())))
            jsonObject.put("userAchievements", JSONArray(json.encodeToString(achievementDao.getAllUnlockedOnce())))
            jsonObject.put("bosses", JSONArray(json.encodeToString(bossDao.getAllBossesOnce())))
            jsonObject.put("dailyChallenges", JSONArray(json.encodeToString(dailyChallengeDao.getAllChallengesOnce())))
            jsonObject.put("focusSessions", JSONArray(json.encodeToString(focusSessionDao.getAllSessionsOnce())))
            jsonObject.put("shadowLog", JSONArray(json.encodeToString(shadowLogDao.getAllLogsOnce())))
            campaignDao.getCampaignOnce()?.let { jsonObject.put("campaign", JSONObject(json.encodeToString(it))) }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val dir = context.getExternalFilesDir(null)
            if (dir == null) {
                return ExportResult(success = false, error = "External storage not available")
            }
            val file = File(dir, "phoenix_protocol_backup_$timestamp.json")
            file.writeText(jsonObject.toString(2))
            ExportResult(success = true, filePath = file.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message)
        }
    }

    suspend fun import(jsonString: String): ImportResult {
        return try {
            val jsonObject = JSONObject(jsonString)
            val version = jsonObject.optInt("version", 0)
            if (version > exportVersion) return ImportResult(success = false, error = "Incompatible backup version")

            database.withTransaction {
                // Clear in FK-safe order
                shadowLogDao.deleteAll()
                focusSessionDao.deleteAll()
                dailyChallengeDao.deleteAll()
                bossDao.deleteAll()
                achievementDao.deleteAllUserAchievements()
                achievementDao.deleteAll()
                templateDao.deleteAll()
                taskDao.deleteAll()
                categoryDao.deleteAll()
                userStatsDao.deleteAllStats()
                userProfileDao.deleteAll()

                // Import in correct order
                jsonObject.optJSONObject("profile")?.let { userProfileDao.insertProfile(json.decodeFromString(it.toString())) }
                jsonObject.optJSONObject("stats")?.let { userStatsDao.insertStats(json.decodeFromString(it.toString())) }
                jsonObject.optJSONArray("categories")?.let { arr -> for (i in 0 until arr.length()) categoryDao.insertCategory(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("tasks")?.let { arr -> for (i in 0 until arr.length()) taskDao.insertTask(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("templates")?.let { arr -> for (i in 0 until arr.length()) templateDao.insertTemplate(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("achievements")?.let { arr -> for (i in 0 until arr.length()) achievementDao.insertAchievement(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("userAchievements")?.let { arr -> for (i in 0 until arr.length()) achievementDao.insertUserAchievement(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("bosses")?.let { arr -> for (i in 0 until arr.length()) bossDao.insertBoss(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("dailyChallenges")?.let { arr -> for (i in 0 until arr.length()) dailyChallengeDao.insertChallenge(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("focusSessions")?.let { arr -> for (i in 0 until arr.length()) focusSessionDao.insertSession(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONArray("shadowLog")?.let { arr -> for (i in 0 until arr.length()) shadowLogDao.insert(json.decodeFromString(arr[i].toString())) }
                jsonObject.optJSONObject("campaign")?.let { campaignDao.insertCampaign(json.decodeFromString(it.toString())) }
            }

            ImportResult(success = true)
        } catch (e: Exception) {
            ImportResult(success = false, error = e.message)
        }
    }
}