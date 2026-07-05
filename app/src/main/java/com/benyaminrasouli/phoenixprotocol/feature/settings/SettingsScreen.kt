package com.benyaminrasouli.phoenixprotocol.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.BuildConfig
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.feature.settings.ExportImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.benyaminrasouli.phoenixprotocol.ui.theme.accentColorMap
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.EnergyGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixRed
import com.benyaminrasouli.phoenixprotocol.ui.theme.ShadowPurple
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceVariantDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

private val backgroundOptions = listOf(
    0 to "Dark",
    1 to "Darker",
    2 to "AMOLED"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle(initialValue = null)
    val taskRemindersEnabled by viewModel.taskRemindersEnabled.collectAsStateWithLifecycle()
    val bossAlertsEnabled by viewModel.bossAlertsEnabled.collectAsStateWithLifecycle()
    val energyNotificationsEnabled by viewModel.energyNotificationsEnabled.collectAsStateWithLifecycle()
    val currentAccentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val currentBackgroundLevel by viewModel.backgroundLevel.collectAsStateWithLifecycle()
    val currentBrightness by viewModel.brightness.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    val exportSuccessMsg = stringResource(R.string.settings_export_success)
    val exportErrorMsg = stringResource(R.string.settings_export_error)
    val importSuccessMsg = stringResource(R.string.settings_import_success)
    val importErrorMsg = stringResource(R.string.settings_import_error)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        reader.readText()
                    }
                }
                if (json != null) {
                    pendingImportJson = json
                    showImportDialog = true
                }
            }
        }
    }

    LaunchedEffect(exportMessage) {
        exportMessage?.let { result ->
            val message = when (result) {
                ExportImportResult.EXPORT_SUCCESS -> exportSuccessMsg
                ExportImportResult.EXPORT_ERROR -> exportErrorMsg
                ExportImportResult.IMPORT_SUCCESS -> importSuccessMsg
                ExportImportResult.IMPORT_ERROR -> importErrorMsg
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Section
            item {
                SettingsSection(title = stringResource(R.string.settings_language)) {
                    SettingsClickableItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.settings_language),
                        subtitle = if (language == "en") stringResource(R.string.settings_english) else stringResource(R.string.settings_persian),
                        onClick = {
                            viewModel.setLanguage(if (language == "en") "fa" else "en")
                        }
                    )
                }
            }

            // Theme Section
            item {
                SettingsSection(title = stringResource(R.string.settings_theme)) {
                    // Accent Color
                    Text(
                        text = stringResource(R.string.settings_accent_color),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val colorEntries = accentColorMap.entries.toList()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (col in 0..3) {
                                    val index = row * 4 + col
                                    if (index < colorEntries.size) {
                                        val (name, color) = colorEntries[index]
                                        val isSelected = currentAccentColor == name
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .then(
                                                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                                    else Modifier
                                                )
                                                .clickable { viewModel.setAccentColor(name) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Background
                    Text(
                        text = stringResource(R.string.settings_background),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    backgroundOptions.forEach { (level, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setBackgroundLevel(level) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentBackgroundLevel == level,
                                onClick = { viewModel.setBackgroundLevel(level) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PhoenixOrange,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Brightness
                    Text(
                        text = stringResource(R.string.settings_brightness),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = currentBrightness.toFloat(),
                            onValueChange = { viewModel.setBrightness(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = PhoenixOrange,
                                activeTrackColor = PhoenixOrange,
                                inactiveTrackColor = SurfaceVariantDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$currentBrightness%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Notifications Section
            item {
                SettingsSection(title = stringResource(R.string.settings_notifications)) {
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_task_reminders),
                        subtitle = null,
                        checked = taskRemindersEnabled,
                        enabled = true,
                        onCheckedChange = { viewModel.setTaskRemindersEnabled(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_boss_alerts),
                        subtitle = null,
                        checked = bossAlertsEnabled,
                        enabled = true,
                        onCheckedChange = { viewModel.setBossAlertsEnabled(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_energy_notifications),
                        subtitle = null,
                        checked = energyNotificationsEnabled,
                        enabled = true,
                        onCheckedChange = { viewModel.setEnergyNotificationsEnabled(it) }
                    )
                }
            }

            // Theme Preview Section
            item {
                SettingsSection(title = stringResource(R.string.settings_theme)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_dark_theme),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_theme_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EnergyGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ColorSwatch(color = BackgroundDark, label = "BG")
                                ColorSwatch(color = SurfaceDark, label = "Surface")
                                ColorSwatch(color = SurfaceVariantDark, label = "Variant")
                                ColorSwatch(color = PhoenixOrange, label = "Primary")
                                ColorSwatch(color = PhoenixGold, label = "Gold")
                            }
                        }
                    }
                }
            }

            // Account Section
            item {
                SettingsSection(title = stringResource(R.string.settings_account)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PhoenixOrange,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = profile?.fullName ?: stringResource(R.string.settings_default_user),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "@${profile?.username ?: stringResource(R.string.settings_default_username)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = profile?.identityPath ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PhoenixGold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.settings_edit_profile))
                            }
                        }
                    }
                }
            }

            // Data & Privacy Section
            item {
                SettingsSection(title = stringResource(R.string.settings_data_privacy)) {
                    SettingsClickableItem(
                        icon = Icons.Default.FileDownload,
                        title = stringResource(R.string.settings_export_data),
                        subtitle = null,
                        onClick = { viewModel.exportData() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsClickableItem(
                        icon = Icons.Default.FileDownload,
                        title = stringResource(R.string.settings_import_data),
                        subtitle = null,
                        onClick = { filePickerLauncher.launch(arrayOf("application/json", "*/*")) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsClickableItem(
                        icon = Icons.Default.Delete,
                        title = stringResource(R.string.settings_reset_data),
                        subtitle = null,
                        onClick = { showResetDialog = true },
                        titleColor = PhoenixRed
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = stringResource(R.string.settings_about)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.settings_about_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = PhoenixOrange,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.settings_about_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsClickableItem(
                                icon = Icons.AutoMirrored.Filled.OpenInNew,
                                title = stringResource(R.string.drawer_support),
                                subtitle = null,
                                onClick = { navController.navigate(Screen.Support.route) }
                            )
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_data)) },
            text = { Text(stringResource(R.string.settings_reset_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetAllData {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.settings_confirm), color = PhoenixRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = TextSecondary
        )
    }

    // Import Confirmation Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.settings_import_data)) },
            text = { Text(stringResource(R.string.settings_import_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        pendingImportJson?.let { json ->
                            viewModel.importData(json)
                        }
                        pendingImportJson = null
                    }
                ) {
                    Text(stringResource(R.string.settings_confirm), color = PhoenixOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = TextSecondary
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = PhoenixOrange,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PhoenixOrange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) PhoenixOrange else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else TextSecondary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PhoenixOrange,
                checkedTrackColor = PhoenixOrange.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceVariantDark
            )
        )
    }
}

@Composable
private fun ColorSwatch(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
