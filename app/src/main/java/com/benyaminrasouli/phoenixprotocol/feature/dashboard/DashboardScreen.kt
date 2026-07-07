package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.AcademicSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.CampaignGrid
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.DailyNoteSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.DayNavigation
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.DopamineControl
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.HeroSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.MissionsSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.SpiritSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.StatsGrid

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        HeroSection()

        DayNavigation(
            currentDay = state.currentDay,
            onPrevDay = { viewModel.prevDay() },
            onNextDay = { viewModel.nextDay() }
        )

        StatsGrid(
            rank = state.rank,
            level = state.level,
            totalXp = state.totalXp,
            discipline = state.discipline,
            xpProgress = state.xpProgress
        )

        MissionsSection(
            missions = state.missions,
            onToggle = { viewModel.toggleMission(it) }
        )

        SpiritSection(
            prayers = state.prayers,
            onToggle = { viewModel.togglePrayer(it) }
        )

        DailyNoteSection(
            note = state.note,
            onNoteChange = { viewModel.saveNote(it) }
        )

        AcademicSection(subjects = state.subjects)

        CampaignGrid(
            currentDay = state.currentDay,
            allDays = state.allDays,
            onDayClick = { viewModel.goToDay(it) }
        )

        DopamineControl(
            isAsh = state.isAsh,
            onRelapse = { viewModel.recordRelapse() },
            onRecover = { viewModel.recover() },
            onReset = { viewModel.resetDay() }
        )
    }
}