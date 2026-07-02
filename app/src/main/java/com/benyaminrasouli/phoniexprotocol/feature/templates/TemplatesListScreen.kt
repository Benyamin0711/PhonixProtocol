package com.benyaminrasouli.phoniexprotocol.feature.templates

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TemplatesListScreen(
    navController: NavController,
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.templates_title)) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = PhoenixOrange
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.templates_create))
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (templates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.List,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.templates_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onDelete = { viewModel.deleteTemplate(it) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Create Template Dialog
    if (uiState.showCreateDialog) {
        CreateTemplateDialog(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

@Composable
private fun TemplateCard(
    template: Template,
    onDelete: (Template) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    if (template.isPriority) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Priority",
                            tint = PhoenixOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onDelete(template) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.templates_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (template.description.isNotEmpty()) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text(template.difficulty, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = PhoenixOrange.copy(alpha = 0.2f),
                        labelColor = PhoenixOrange
                    ),
                    enabled = false
                )
                if (template.category.isNotEmpty()) {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(template.category, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = TextSecondary
                        ),
                        enabled = false
                    )
                }
                if (template.recurrence != "NONE") {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(template.recurrence, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = TextSecondary
                        ),
                        enabled = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateTemplateDialog(
    uiState: TemplatesUiState,
    viewModel: TemplatesViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissCreateDialog() },
        title = { Text(stringResource(R.string.templates_create)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.newTemplateName,
                    onValueChange = { viewModel.updateTemplateName(it) },
                    label = { Text(stringResource(R.string.templates_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PhoenixOrange,
                        unfocusedBorderColor = TextSecondary
                    )
                )
                OutlinedTextField(
                    value = uiState.newTemplateDescription,
                    onValueChange = { viewModel.updateTemplateDescription(it) },
                    label = { Text(stringResource(R.string.task_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PhoenixOrange,
                        unfocusedBorderColor = TextSecondary
                    )
                )
                OutlinedTextField(
                    value = uiState.newTemplateCategory,
                    onValueChange = { viewModel.updateTemplateCategory(it) },
                    label = { Text(stringResource(R.string.task_category)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PhoenixOrange,
                        unfocusedBorderColor = TextSecondary
                    )
                )
                Text(
                    text = stringResource(R.string.task_difficulty),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EASY", "MEDIUM", "HARD", "EXTREME").forEach { difficulty ->
                        FilterChip(
                            selected = uiState.newTemplateDifficulty == difficulty,
                            onClick = { viewModel.updateTemplateDifficulty(difficulty) },
                            label = { Text(difficulty) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PhoenixOrange,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.task_recurrence),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NONE", "DAILY", "WEEKLY", "MONTHLY").forEach { recurrence ->
                        FilterChip(
                            selected = uiState.newTemplateRecurrence == recurrence,
                            onClick = { viewModel.updateTemplateRecurrence(recurrence) },
                            label = { Text(recurrence) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PhoenixOrange,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = uiState.newTemplateIsPriority,
                        onCheckedChange = { viewModel.updateTemplateIsPriority(it) },
                        colors = CheckboxDefaults.colors(checkedColor = PhoenixOrange)
                    )
                    Text(
                        text = stringResource(R.string.task_priority),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.createTemplate() }) {
                Text(stringResource(R.string.templates_create), color = PhoenixOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissCreateDialog() }) {
                Text(stringResource(R.string.settings_cancel), color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
