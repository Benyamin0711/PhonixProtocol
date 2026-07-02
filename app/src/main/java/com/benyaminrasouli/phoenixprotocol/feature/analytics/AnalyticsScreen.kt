package com.benyaminrasouli.phoenixprotocol.feature.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.ui.components.BarChart
import com.benyaminrasouli.phoenixprotocol.core.ui.components.DonutChart
import com.benyaminrasouli.phoenixprotocol.core.ui.components.LineChart
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

private val categoryColors: Map<Long?, Color> = mapOf(
    1L to Color(0xFF4A90D9),
    2L to Color(0xFF4CAF50),
    3L to Color(0xFF9C27B0),
    4L to Color(0xFFFF6B35),
    5L to Color(0xFFFFC107),
    6L to Color(0xFFE91E63),
    null to Color(0xFF607D8B)
)

private val categoryNames: Map<Long?, String> = mapOf(
    1L to "Work",
    2L to "Health",
    3L to "Learning",
    4L to "Personal",
    5L to "Finance",
    6L to "Social",
    null to "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.analytics_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Completion chart
            ChartCard(title = stringResource(R.string.analytics_tasks_completed)) {
                BarChart(
                    data = state.weeklyTaskCompletion.map { it.day to it.count }
                )
            }

            // XP Trend chart
            ChartCard(title = stringResource(R.string.analytics_xp_trend)) {
                LineChart(
                    data = state.xpTrend.map { it.day to it.total }
                )
            }

            // Category Breakdown chart
            ChartCard(title = stringResource(R.string.analytics_category_breakdown)) {
                DonutChart(
                    segments = state.categoryBreakdown.map { cc ->
                        Triple(
                            categoryNames[cc.categoryId] ?: "Other",
                            cc.count,
                            categoryColors[cc.categoryId] ?: Color(0xFF607D8B)
                        )
                    }
                )
            }

            // Focus Time chart
            ChartCard(title = stringResource(R.string.analytics_focus_time)) {
                BarChart(
                    data = state.weeklyFocusMinutes.map { it.day to it.total }
                )
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PhoenixOrange
            )
            content()
        }
    }
}
