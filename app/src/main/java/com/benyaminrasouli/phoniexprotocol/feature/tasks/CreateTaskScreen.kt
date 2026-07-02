package com.benyaminrasouli.phoniexprotocol.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.core.domain.model.Difficulty
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskRecurrence
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskType
import com.benyaminrasouli.phoniexprotocol.core.ui.components.PhoenixButton
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.task_create)) },
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
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::setTitle,
                label = { Text(stringResource(R.string.task_title)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text(stringResource(R.string.task_description)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                minLines = 2
            )

            // Difficulty
            Text(stringResource(R.string.task_difficulty), color = TextSecondary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = state.difficulty == diff,
                        onClick = { viewModel.setDifficulty(diff) },
                        label = { Text(diff.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            // Task Type
            Text("Task Type", color = TextSecondary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskType.entries.forEach { type ->
                    FilterChip(
                        selected = state.taskType == type,
                        onClick = { viewModel.setTaskType(type) },
                        label = { Text(type.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            // Category
            Text(stringResource(R.string.task_category), color = TextSecondary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.categories.collectAsStateWithLifecycle().value.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            // Recurrence
            Text(stringResource(R.string.task_recurrence), color = TextSecondary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskRecurrence.entries.forEach { rec ->
                    FilterChip(
                        selected = state.recurrence == rec,
                        onClick = { viewModel.setRecurrence(rec) },
                        label = { Text(rec.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            // Priority toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Priority Task", color = TextSecondary)
                Switch(
                    checked = state.isPriority,
                    onCheckedChange = viewModel::setPriority,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PhoenixOrange,
                        checkedTrackColor = PhoenixOrange.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PhoenixButton(
                text = stringResource(R.string.task_create),
                onClick = {
                    viewModel.saveTask { navController.popBackStack() }
                },
                enabled = state.title.isNotBlank() && !state.isSaving
            )
        }
    }
}
