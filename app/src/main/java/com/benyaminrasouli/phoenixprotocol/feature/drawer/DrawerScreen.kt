package com.benyaminrasouli.phoenixprotocol.feature.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun DrawerScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFocusTimer: () -> Unit = {},
    onNavigateToShadow: () -> Unit = {},
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle(initialValue = null)
    val stats by viewModel.stats.collectAsStateWithLifecycle(initialValue = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        // Profile header
        profile?.let { p ->
            Text(
                text = p.fullName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@${p.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            stats?.let { s ->
                Text(
                    text = "${stringResource(R.string.level)} ${s.level} • ${s.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    color = PhoenixOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(color = SurfaceDark)

        Spacer(modifier = Modifier.height(16.dp))

        // Menu items
        DrawerMenuItem(
            icon = Icons.Filled.AccountCircle,
            label = stringResource(R.string.drawer_profile),
            onClick = onNavigateToProfile
        )
        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.List,
            label = stringResource(R.string.drawer_statistics),
            onClick = onNavigateToStatistics
        )
        DrawerMenuItem(
            icon = Icons.Filled.Lock,
            label = stringResource(R.string.drawer_shadow),
            onClick = onNavigateToShadow
        )
        DrawerMenuItem(
            icon = Icons.Filled.DateRange,
            label = stringResource(R.string.drawer_focus),
            onClick = onNavigateToFocusTimer
        )
        DrawerMenuItem(
            icon = Icons.Filled.Settings,
            label = stringResource(R.string.drawer_settings),
            onClick = onNavigateToSettings
        )
        DrawerMenuItem(
            icon = Icons.Filled.Info,
            label = stringResource(R.string.about_title),
            onClick = onNavigateToAbout
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = SurfaceDark)

        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(
            icon = Icons.Default.Info,
            label = stringResource(R.string.drawer_support),
            onClick = onNavigateToSupport
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
