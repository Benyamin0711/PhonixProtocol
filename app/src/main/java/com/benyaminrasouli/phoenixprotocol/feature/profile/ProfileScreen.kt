package com.benyaminrasouli.phoenixprotocol.feature.profile

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.domain.model.IdentityPath
import com.benyaminrasouli.phoenixprotocol.core.ui.components.EnergyBar
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.profile_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (state.isEditing) {
                    TextButton(onClick = { viewModel.saveProfile() }) {
                        Text(
                            text = stringResource(R.string.profile_save),
                            color = PhoenixOrange
                        )
                    }
                } else {
                    IconButton(onClick = { viewModel.toggleEdit() }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.profile_edit),
                            tint = PhoenixOrange
                        )
                    }
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
            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = { viewModel.updateFullName(it) },
                            label = { Text(stringResource(R.string.profile_full_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhoenixOrange,
                                unfocusedBorderColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.username,
                            onValueChange = { viewModel.updateUsername(it) },
                            label = { Text(stringResource(R.string.profile_username)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhoenixOrange,
                                unfocusedBorderColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.birthYear,
                            onValueChange = { viewModel.updateBirthYear(it) },
                            label = { Text(stringResource(R.string.profile_birth_year)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhoenixOrange,
                                unfocusedBorderColor = TextSecondary
                            )
                        )
                    } else {
                        Text(
                            text = state.fullName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "@${state.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        if (state.birthYear.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.profile_birth_year, state.birthYear),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_stats),
                        style = MaterialTheme.typography.titleMedium,
                        color = PhoenixOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileStatRow(
                        label = stringResource(R.string.profile_level, state.level),
                        value = state.rank
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.profile_xp, state.xp),
                        value = ""
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.profile_streak, state.streak),
                        value = ""
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.profile_tasks_completed, state.completedTasks),
                        value = ""
                    )
                }
            }

            // Identity Path Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_identity_path),
                        style = MaterialTheme.typography.titleMedium,
                        color = PhoenixOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (state.isEditing) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IdentityPath.entries.forEach { path ->
                                FilterChip(
                                    selected = state.identityPath == path.name,
                                    onClick = { viewModel.updateIdentityPath(path.name) },
                                    label = { Text(path.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PhoenixOrange,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            text = state.identityPath,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
