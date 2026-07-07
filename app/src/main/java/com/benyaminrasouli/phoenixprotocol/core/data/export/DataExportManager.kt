package com.benyaminrasouli.phoenixprotocol.core.data.export

import android.content.Context
import androidx.room.withTransaction
import com.benyaminrasouli.phoenixprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.*
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val exportVersion = 12

    suspend fun export(): ExportResult {
        return try {
            val json = JSONObject()
            json.put("version", exportVersion)
            json.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()))
            json.put("appName", "PhoenixProtocol")

            userProfileDao.getProfileOnce()?.let { json.put("profile", JSONObject(gson.toJson(it))) }
            userStatsDao.getStatsOnce()?.let { json.put("stats", JSONObject(gson.toJson(it))) }
            json.put("tasks", JSONArray(gson.toJson(taskDao.getAllTasksOnce())))
            json.put("categories", JSONArray(gson.toJson(categoryDao.getAllCategoriesOnce())))
            json.put("templates", JSONArray(gson.toJson(templateDao.getAllTemplatesOnce())))
            json.put("achievements", JSONArray(gson.toJson(achievementDao.getAllAchievementsOnce())))
            json.put("userAchievements", JSONArray(gson.toJson(achievementDao.getAllUnlockedOnce())))
            json.put("bosses", JSONArray(gson.toJson(bossDao.getAllBossesOnce())))
            json.put("dailyChallenges", JSONArray(gson.toJson(dailyChallengeDao.getAllChallengesOnce())))
            json.put("focusSessions", JSONArray(gson.toJson(focusSessionDao.getAllSessionsOnce())))
            json.put("shadowLog", JSONArray(gson.toJson(shadowLogDao.getAllLogsOnce())))
            campaignDao.getCampaignOnce()?.let { json.put("campaign", JSONObject(gson.toJson(it))) }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val dir = context.getExternalFilesDir(null)
            if (dir == null) {
                return ExportResult(success = false, error = "External storage not available")
            }
            val file = File(dir, "phoenix_protocol_backup_$timestamp.json")
            file.writeText(json.toString(2))
            ExportResult(success = true, filePath = file.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message)
        }
    }

    suspend fun import(jsonString: String): ImportResult {
        return try {
            val json = JSONObject(jsonString)
            val version = json.optInt("version", 0)
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
                json.optJSONObject("profile")?.let { userProfileDao.insertProfile(gson.fromJson(it.toString(), UserProfile::class.java)) }
                json.optJSONObject("stats")?.let { userStatsDao.insertStats(gson.fromJson(it.toString(), UserStats::class.java)) }
                json.optJSONArray("categories")?.let { arr -> for (i in 0 until arr.length()) categoryDao.insertCategory(gson.fromJson(arr[i].toString(), Category::class.java)) }
                json.optJSONArray("tasks")?.let { arr -> for (i in 0 until arr.length()) taskDao.insertTask(gson.fromJson(arr[i].toString(), Task::class.java)) }
                json.optJSONArray("templates")?.let { arr -> for (i in 0 until arr.length()) templateDao.insertTemplate(gson.fromJson(arr[i].toString(), Template::class.java)) }
                json.optJSONArray("achievements")?.let { arr -> for (i in 0 until arr.length()) achievementDao.insertAchievement(gson.fromJson(arr[i].toString(), Achievement::class.java)) }
                json.optJSONArray("userAchievements")?.let { arr -> for (i in 0 until arr.length()) achievementDao.insertUserAchievement(gson.fromJson(arr[i].toString(), UserAchievement::class.java)) }
                json.optJSONArray("bosses")?.let { arr -> for (i in 0 until arr.length()) bossDao.insertBoss(gson.fromJson(arr[i].toString(), Boss::class.java)) }
                json.optJSONArray("dailyChallenges")?.let { arr -> for (i in 0 until arr.length()) dailyChallengeDao.insertChallenge(gson.fromJson(arr[i].toString(), DailyChallenge::class.java)) }
                json.optJSONArray("focusSessions")?.let { arr -> for (i in 0 until arr.length()) focusSessionDao.insertSession(gson.fromJson(arr[i].toString(), FocusSession::class.java)) }
                json.optJSONArray("shadowLog")?.let { arr -> for (i in 0 until arr.length()) shadowLogDao.insert(gson.fromJson(arr[i].toString(), ShadowLog::class.java)) }
                json.optJSONObject("campaign")?.let { campaignDao.insertCampaign(gson.fromJson(it.toString(), Campaign::class.java)) }
            }

            ImportResult(success = true)
        } catch (e: Exception) {
            ImportResult(success = false, error = e.message)
        }
    }
}
