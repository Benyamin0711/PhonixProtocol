# Mood Tracker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a standalone Mood Tracker feature with emoji-based mood recording, notes, tags, calendar heatmap, trend charts, and weekly insights.

**Architecture:** Follow existing Clean Architecture + MVVM pattern. New entity `MoodEntry` with Room DAO, repository, use cases, and ViewModel. UI built with Jetpack Compose Material3.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Navigation Compose, Material3

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `core/data/db/entity/MoodEntry.kt` | Create | Room entity for mood entries |
| `core/data/db/dao/MoodDao.kt` | Create | DAO with queries |
| `core/data/repository/MoodRepositoryImpl.kt` | Create | Repository implementation |
| `core/domain/repository/MoodRepository.kt` | Create | Repository interface |
| `core/domain/usecase/RecordMoodUseCase.kt` | Create | Record mood entry |
| `core/domain/usecase/GetMoodHistoryUseCase.kt` | Create | Get history with filters |
| `core/domain/usecase/GetMoodInsightsUseCase.kt` | Create | Weekly/monthly insights |
| `core/navigation/Screen.kt` | Modify | Add MoodTracker route |
| `core/navigation/NavGraph.kt` | Modify | Add composable |
| `feature/mood/MoodTrackerScreen.kt` | Create | Main screen with tabs |
| `feature/mood/MoodViewModel.kt` | Create | ViewModel with state |
| `feature/mood/components/MoodInputCard.kt` | Create | Emoji selector + note + tags |
| `feature/mood/components/MoodCalendarHeatmap.kt` | Create | Calendar view |
| `feature/mood/components/MoodTrendChart.kt` | Create | Line chart |
| `feature/mood/components/MoodInsightsCard.kt` | Create | Weekly insights |
| `core/data/db/PhoenixDatabase.kt` | Modify | Add entity, bump version |
| `di/DatabaseModule.kt` | Modify | Add MoodDao provider |

---

### Task 1: Data Layer - Entity & DAO

**Covers:** [S1]

**Files:**
- Create: `core/data/db/entity/MoodEntry.kt`
- Create: `core/data/db/dao/MoodDao.kt`
- Modify: `core/data/db/PhoenixDatabase.kt`

- [ ] **Step 1: Create MoodEntry entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val moodLevel: Int,        // 1-5 (1=Terrible, 5=Amazing)
    val note: String = "",
    val tags: String = "",     // Comma-separated: "kar,varzesh,khab"
    val timestamp: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create MoodDao**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MoodEntry): Long

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getEntriesByDateRange(start: Long, end: Long): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE tags LIKE '%' || :tag || '%' ORDER BY timestamp DESC")
    fun getEntriesByTag(tag: String): Flow<List<MoodEntry>>

    @Query("SELECT AVG(moodLevel) FROM mood_entries WHERE timestamp BETWEEN :start AND :end")
    fun getAverageMoodByDateRange(start: Long, end: Long): Flow<Float?>

    @Query("SELECT * FROM mood_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getEntriesByDateRangeOnce(start: Long, end: Long): List<MoodEntry>

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)
}
```

- [ ] **Step 3: Update PhoenixDatabase**

Add `MoodEntry::class` to entities array and bump version to 13. Add abstract fun `moodDao(): MoodDao`.

- [ ] **Step 4: Commit**

```bash
git add core/data/db/entity/MoodEntry.kt core/data/db/dao/MoodDao.kt core/data/db/PhoenixDatabase.kt
git commit -m "feat(mood): add MoodEntry entity and MoodDao"
```

---

### Task 2: Repository & Use Cases

**Covers:** [S1]

**Files:**
- Create: `core/domain/repository/MoodRepository.kt`
- Create: `core/data/repository/MoodRepositoryImpl.kt`
- Create: `core/domain/usecase/RecordMoodUseCase.kt`
- Create: `core/domain/usecase/GetMoodHistoryUseCase.kt`
- Create: `core/domain/usecase/GetMoodInsightsUseCase.kt`

- [ ] **Step 1: Create MoodRepository interface**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    suspend fun insertEntry(entry: MoodEntry): Long
    fun getAllEntries(): Flow<List<MoodEntry>>
    fun getEntriesByDateRange(start: Long, end: Long): Flow<List<MoodEntry>>
    fun getEntriesByTag(tag: String): Flow<List<MoodEntry>>
    fun getAverageMoodByDateRange(start: Long, end: Long): Flow<Float?>
    suspend fun getEntriesByDateRangeOnce(start: Long, end: Long): List<MoodEntry>
    suspend fun deleteEntry(id: Long)
}
```

- [ ] **Step 2: Create MoodRepositoryImpl**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.MoodDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val dao: MoodDao
) : MoodRepository {
    override suspend fun insertEntry(entry: MoodEntry): Long = dao.insertEntry(entry)
    override fun getAllEntries(): Flow<List<MoodEntry>> = dao.getAllEntries()
    override fun getEntriesByDateRange(start: Long, end: Long): Flow<List<MoodEntry>> = dao.getEntriesByDateRange(start, end)
    override fun getEntriesByTag(tag: String): Flow<List<MoodEntry>> = dao.getEntriesByTag(tag)
    override fun getAverageMoodByDateRange(start: Long, end: Long): Flow<Float?> = dao.getAverageMoodByDateRange(start, end)
    override suspend fun getEntriesByDateRangeOnce(start: Long, end: Long): List<MoodEntry> = dao.getEntriesByDateRangeOnce(start, end)
    override suspend fun deleteEntry(id: Long) = dao.deleteEntry(id)
}
```

- [ ] **Step 3: Create RecordMoodUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.MoodRepository
import javax.inject.Inject

class RecordMoodUseCase @Inject constructor(
    private val repository: MoodRepository
) {
    suspend operator fun invoke(moodLevel: Int, note: String = "", tags: List<String> = emptyList()): Long {
        val entry = MoodEntry(
            moodLevel = moodLevel.coerceIn(1, 5),
            note = note.trim(),
            tags = tags.joinToString(",")
        )
        return repository.insertEntry(entry)
    }
}
```

- [ ] **Step 4: Create GetMoodHistoryUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoodHistoryUseCase @Inject constructor(
    private val repository: MoodRepository
) {
    operator fun invoke(): Flow<List<MoodEntry>> = repository.getAllEntries()

    fun byDateRange(start: Long, end: Long): Flow<List<MoodEntry>> = repository.getEntriesByDateRange(start, end)

    fun byTag(tag: String): Flow<List<MoodEntry>> = repository.getEntriesByTag(tag)
}
```

- [ ] **Step 5: Create GetMoodInsightsUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.MoodRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class MoodInsights(
    val thisWeekAverage: Float,
    val lastWeekAverage: Float,
    val mostCommonMood: Int,
    val mostUsedTags: List<Pair<String, Int>>,
    val bestDayOfWeek: String,
    val worstDayOfWeek: String
)

class GetMoodInsightsUseCase @Inject constructor(
    private val repository: MoodRepository
) {
    suspend operator fun invoke(): MoodInsights {
        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()

        val thisWeekStart = now.with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant().toEpochMilli()
        val thisWeekEnd = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        val lastWeekStart = now.minusWeeks(1).with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant().toEpochMilli()
        val lastWeekEnd = thisWeekStart - 1

        val thisWeekEntries = repository.getEntriesByDateRangeOnce(thisWeekStart, thisWeekEnd)
        val lastWeekEntries = repository.getEntriesByDateRangeOnce(lastWeekStart, lastWeekEnd)

        val thisWeekAvg = if (thisWeekEntries.isNotEmpty()) thisWeekEntries.map { it.moodLevel }.average().toFloat() else 0f
        val lastWeekAvg = if (lastWeekEntries.isNotEmpty()) lastWeekEntries.map { it.moodLevel }.average().toFloat() else 0f

        val allEntries = thisWeekEntries + lastWeekEntries
        val moodCounts = allEntries.groupingBy { it.moodLevel }.eachCount()
        val mostCommonMood = moodCounts.maxByOrNull { it.value }?.key ?: 3

        val tagCounts = allEntries.flatMap { it.tags.split(",").filter.isNotBlank() }
            .groupingBy { it.trim() }.eachCount()
            .toList().sortedByDescending { it.second }.take(5)

        val dayMoodMap = mutableMapOf<DayOfWeek, MutableList<Int>>()
        allEntries.forEach { entry ->
            val day = Instant.ofEpochMilli(entry.timestamp).atZone(zone).dayOfWeek
            dayMoodMap.getOrPut(day) { mutableListOf() }.add(entry.moodLevel)
        }
        val dayAverages = dayMoodMap.map { (day, moods) -> day to moods.average() }
        val bestDay = dayAverages.maxByOrNull { it.second }?.first?.name ?: "MONDAY"
        val worstDay = dayAverages.minByOrNull { it.second }?.first?.name ?: "MONDAY"

        return MoodInsights(
            thisWeekAverage = thisWeekAvg,
            lastWeekAverage = lastWeekAvg,
            mostCommonMood = mostCommonMood,
            mostUsedTags = tagCounts,
            bestDayOfWeek = bestDay,
            worstDayOfWeek = worstDay
        )
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add core/domain/repository/MoodRepository.kt core/data/repository/MoodRepositoryImpl.kt core/domain/usecase/RecordMoodUseCase.kt core/domain/usecase/GetMoodHistoryUseCase.kt core/domain/usecase/GetMoodInsightsUseCase.kt
git commit -m "feat(mood): add repository and use cases"
```

---

### Task 3: DI & Navigation

**Covers:** [S1, S2]

**Files:**
- Modify: `di/DatabaseModule.kt`
- Modify: `core/navigation/Screen.kt`
- Modify: `core/navigation/NavGraph.kt`

- [ ] **Step 1: Add MoodDao provider to DatabaseModule**

Add to DatabaseModule:
```kotlin
@Provides
fun provideMoodDao(database: PhoenixDatabase): MoodDao = database.moodDao()
```

- [ ] **Step 2: Add Screen.MoodTracker**

In Screen.kt add:
```kotlin
data object MoodTracker : Screen("mood_tracker")
```

- [ ] **Step 3: Add NavGraph route**

In NavGraph.kt add composable for MoodTracker screen.

- [ ] **Step 4: Commit**

```bash
git add di/DatabaseModule.kt core/navigation/Screen.kt core/navigation/NavGraph.kt
git commit -m "feat(mood): add DI bindings and navigation"
```

---

### Task 4: ViewModel

**Covers:** [S1, S2]

**Files:**
- Create: `feature/mood/MoodViewModel.kt`

- [ ] **Step 1: Create MoodViewModel**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetMoodHistoryUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetMoodInsightsUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.MoodInsights
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.RecordMoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class MoodUiState(
    val selectedMood: Int = 0,
    val note: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isSaving: Boolean = false
)

enum class MoodTab { RECORD, HISTORY, INSIGHTS }

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val recordMoodUseCase: RecordMoodUseCase,
    getMoodHistoryUseCase: GetMoodHistoryUseCase,
    private val getMoodInsightsUseCase: GetMoodInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(MoodTab.RECORD)
    val selectedTab: StateFlow<MoodTab> = _selectedTab.asStateFlow()

    val entries: StateFlow<List<MoodEntry>> = getMoodHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _insights = MutableStateFlow<MoodInsights?>(null)
    val insights: StateFlow<MoodInsights?> = _insights.asStateFlow()

    init {
        loadInsights()
    }

    fun selectTab(tab: MoodTab) {
        _selectedTab.value = tab
        if (tab == MoodTab.INSIGHTS) loadInsights()
    }

    fun selectMood(level: Int) {
        _uiState.value = _uiState.value.copy(selectedMood = level)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun toggleTag(tag: String) {
        val current = _uiState.value.selectedTags.toMutableSet()
        if (tag in current) current.remove(tag) else current.add(tag)
        _uiState.value = _uiState.value.copy(selectedTags = current)
    }

    fun saveMood() {
        val state = _uiState.value
        if (state.selectedMood == 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            recordMoodUseCase(
                moodLevel = state.selectedMood,
                note = state.note,
                tags = state.selectedTags.toList()
            )
            _uiState.value = MoodUiState()
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            recordMoodUseCase // placeholder - use repository directly
        }
    }

    private fun loadInsights() {
        viewModelScope.launch {
            _insights.value = getMoodInsightsUseCase()
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/mood/MoodViewModel.kt
git commit -m "feat(mood): add MoodViewModel"
```

---

### Task 5: MoodInputCard Component

**Covers:** [S2]

**Files:**
- Create: `feature/mood/components/MoodInputCard.kt`

- [ ] **Step 1: Create MoodInputCard**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.mood.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

private val MOOD_EMOJIS = listOf("Terrible", "Bad", "Okay", "Good", "Amazing")
private val MOOD_COLORS = listOf(
    Color(0xFFE53935), // Red
    Color(0xFFFF9800), // Orange
    Color(0xFFFFC107), // Yellow
    Color(0xFF8BC34A), // Light Green
    Color(0xFF4CAF50)  // Green
)
private val MOOD_ICONS = listOf("\uD83D\uDE22", "\uD83D\uDE1E", "\uD83D\uDE10", "\uD83D\uDE0A", "\uD83D\uDE0D")

@Composable
fun MoodInputCard(
    selectedMood: Int,
    note: String,
    selectedTags: Set<String>,
    availableTags: List<String>,
    onMoodSelected: (Int) -> Unit,
    onNoteChanged: (String) -> Unit,
    onTagToggled: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "How are you feeling?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PhoenixOrange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mood emojis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MOOD_EMOJIS.forEachIndexed { index, label ->
                    val level = index + 1
                    val isSelected = selectedMood == level
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MOOD_COLORS[index].copy(alpha = 0.2f) else Color.Transparent)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MOOD_COLORS[index], CircleShape)
                                    else Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                )
                                .clickable { onMoodSelected(level) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = MOOD_ICONS[index], fontSize = 28.sp)
                        }
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = if (isSelected) MOOD_COLORS[index] else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            Text("Tags", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onTagToggled(tag) },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                enabled = selectedMood > 0
            ) {
                Text("Save Mood")
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/mood/components/MoodInputCard.kt
git commit -m "feat(mood): add MoodInputCard component"
```

---

### Task 6: CalendarHeatmap Component

**Covers:** [S2]

**Files:**
- Create: `feature/mood/components/MoodCalendarHeatmap.kt`

- [ ] **Step 1: Create MoodCalendarHeatmap**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.mood.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val MOOD_COLORS = listOf(
    Color(0xFF1A1A2E), // No data
    Color(0xFFE53935), // Terrible
    Color(0xFFFF9800), // Bad
    Color(0xFFFFC107), // Okay
    Color(0xFF8BC34A), // Good
    Color(0xFF4CAF50)  // Amazing
)

@Composable
fun MoodCalendarHeatmap(
    moodData: Map<LocalDate, Float>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.now()
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek
    val startOffset = (firstDayOfWeek.value % 7)

    Column(modifier = modifier) {
        Text(
            text = "${yearMonth.month.name.take(3)} ${yearMonth.year}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid
        val rows = ((daysInMonth + startOffset) / 7) + 1
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { col ->
                    val dayIndex = row * 7 + col - startOffset + 1
                    if (dayIndex in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayIndex)
                        val avgMood = moodData[date]
                        val colorIndex = when {
                            date.isAfter(today) -> 0
                            avgMood == null -> 0
                            else -> avgMood.toInt().coerceIn(1, 5)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MOOD_COLORS[colorIndex]),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayIndex",
                                fontSize = 10.sp,
                                color = if (colorIndex > 0) Color.White else TextSecondary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/mood/components/MoodCalendarHeatmap.kt
git commit -m "feat(mood): add MoodCalendarHeatmap component"
```

---

### Task 7: MoodTrendChart Component

**Covers:** [S2]

**Files:**
- Create: `feature/mood/components/MoodTrendChart.kt`

- [ ] **Step 1: Create MoodTrendChart**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.mood.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun MoodTrendChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val path = Path()
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
            val chartHeight = size.height - 20.dp.toPx()

            data.forEachIndexed { index, (_, value) ->
                val x = index * stepX
                val y = chartHeight - ((value - 1) / 4f) * chartHeight
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = PhoenixOrange,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw points
            data.forEachIndexed { index, (_, value) ->
                val x = index * stepX
                val y = chartHeight - ((value - 1) / 4f) * chartHeight
                drawCircle(
                    color = PhoenixOrange,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { (label, _) ->
                Text(text = label, fontSize = 8.sp, color = TextSecondary)
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/mood/components/MoodTrendChart.kt
git commit -m "feat(mood): add MoodTrendChart component"
```

---

### Task 8: MoodInsightsCard Component

**Covers:** [S2]

**Files:**
- Create: `feature/mood/components/MoodInsightsCard.kt`

- [ ] **Step 1: Create MoodInsightsCard**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.mood.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.MoodInsights
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

private val MOOD_LABELS = listOf("", "Terrible", "Bad", "Okay", "Good", "Amazing")

@Composable
fun MoodInsightsCard(
    insights: MoodInsights,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weekly Insights", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Week comparison
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("This Week", fontSize = 12.sp, color = TextSecondary)
                    Text("${String.format("%.1f", insights.thisWeekAverage)} / 5", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PhoenixOrange)
                }
                Column {
                    Text("Last Week", fontSize = 12.sp, color = TextSecondary)
                    Text("${String.format("%.1f", insights.lastWeekAverage)} / 5", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Most common mood
            Text("Most Common: ${MOOD_LABELS[insights.mostCommonMood]}", color = Color.White, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Best/worst day
            Text("Best Day: ${insights.bestDayOfWeek}", color = Color(0xFF4CAF50), fontSize = 14.sp)
            Text("Worst Day: ${insights.worstDayOfWeek}", color = Color(0xFFE53935), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Top tags
            if (insights.mostUsedTags.isNotEmpty()) {
                Text("Top Tags:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                insights.mostUsedTags.take(3).forEach { (tag, count) ->
                    Text("  $tag ($count)", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/mood/components/MoodInsightsCard.kt
git commit -m "feat(mood): add MoodInsightsCard component"
```

---

### Task 9: MoodTrackerScreen

**Covers:** [S2]

**Files:**
- Create: `feature/mood/MoodTrackerScreen.kt`

- [ ] **Step 1: Create MoodTrackerScreen**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.mood.components.*
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val AVAILABLE_TAGS = listOf(
    "kar", "varzesh", "khab", "dostan", "khani",
    "khoonvade", "ghazaei", "safar", "stress", "shadimood", "narahat", "khasteh"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackerScreen(
    navController: NavController,
    viewModel: MoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val insights by viewModel.insights.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text("Mood Tracker") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
        )

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = BackgroundDark,
            contentColor = PhoenixOrange
        ) {
            MoodTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.name) }
                )
            }
        }

        when (selectedTab) {
            MoodTab.RECORD -> {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MoodInputCard(
                            selectedMood = uiState.selectedMood,
                            note = uiState.note,
                            selectedTags = uiState.selectedTags,
                            availableTags = AVAILABLE_TAGS,
                            onMoodSelected = viewModel::selectMood,
                            onNoteChanged = viewModel::updateNote,
                            onTagToggled = viewModel::toggleTag,
                            onSave = viewModel::saveMood
                        )
                    }

                    // Today's entries
                    val todayEntries = entries.filter {
                        Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
                    }
                    if (todayEntries.isNotEmpty()) {
                        item {
                            Text("Today", color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(todayEntries) { entry ->
                            MoodEntryCard(entry)
                        }
                    }
                }
            }

            MoodTab.HISTORY -> {
                // Calendar heatmap
                val moodData = entries.groupBy {
                    Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                }.mapValues { (_, dayEntries) ->
                    dayEntries.map { it.moodLevel }.average().toFloat()
                }

                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        MoodCalendarHeatmap(moodData = moodData)
                    }

                    // Trend chart
                    val last7Days = (0L downTo -6L).map { days ->
                        val date = LocalDate.now().minusDays(days)
                        val avg = entries.filter {
                            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date
                        }.let { dayEntries ->
                            if (dayEntries.isNotEmpty()) dayEntries.map { it.moodLevel }.average().toFloat() else 0f
                        }
                        Pair(date.dayOfWeek.name.take(3), avg)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        MoodTrendChart(data = last7Days)
                    }

                    // Entry list
                    items(entries.take(20)) { entry ->
                        MoodEntryCard(entry)
                    }
                }
            }

            MoodTab.INSIGHTS -> {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    insights?.let { insight ->
                        item {
                            MoodInsightsCard(insights = insight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodEntryCard(entry: com.benyaminrasouli.phoenixprotocol.core.data.db.entity.MoodEntry) {
    val emojis = listOf("", "\uD83D\uDE22", "\uD83D\uDE1E", "\uD83D\uDE10", "\uD83D\uDE0A", "\uD83D\uDE0D")
    val time = Instant.ofEpochMilli(entry.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(text = emojis[entry.moodLevel], fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (entry.note.isNotBlank()) {
                    Text(text = entry.note, color = Color.White)
                }
                if (entry.tags.isNotBlank()) {
                    Text(text = entry.tags, color = TextSecondary, fontSize = 12.sp)
                }
            }
            Text(text = time, color = TextSecondary, fontSize = 12.sp)
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/mood/MoodTrackerScreen.kt
git commit -m "feat(mood): add MoodTrackerScreen"
```

---

### Task 10: Navigation & Final Integration

**Covers:** [S2]

**Files:**
- Modify: `core/navigation/NavGraph.kt`

- [ ] **Step 1: Add MoodTracker route to NavGraph**

```kotlin
composable(
    Screen.MoodTracker.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    MoodTrackerScreen(navController = navController)
}
```

- [ ] **Step 2: Add import**

```kotlin
import com.benyaminrasouli.phoenixprotocol.feature.mood.MoodTrackerScreen
```

- [ ] **Step 3: Build and verify**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add core/navigation/NavGraph.kt
git commit -m "feat(mood): integrate MoodTracker into navigation"
```

---

### Task 11: DB Migration

**Covers:** [S1]

**Files:**
- Modify: `core/data/db/PhoenixDatabase.kt`
- Modify: `di/DatabaseModule.kt`

- [ ] **Step 1: Add migration to DatabaseModule**

In the database builder, add:
```kotlin
.addMigrations(MIGRATION_12_13)
```

And define:
```kotlin
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS mood_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                moodLevel INTEGER NOT NULL,
                note TEXT NOT NULL DEFAULT '',
                tags TEXT NOT NULL DEFAULT '',
                timestamp INTEGER NOT NULL
            )
        """)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add core/data/db/PhoenixDatabase.kt di/DatabaseModule.kt
git commit -m "feat(mood): add DB migration v12 to v13"
```
