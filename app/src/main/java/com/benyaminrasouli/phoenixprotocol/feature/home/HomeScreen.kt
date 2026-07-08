package com.benyaminrasouli.phoenixprotocol.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.home.components.FeatureGrid
import com.benyaminrasouli.phoenixprotocol.feature.home.components.StorySection
import com.benyaminrasouli.phoenixprotocol.feature.home.components.SupportFooter
import com.benyaminrasouli.phoenixprotocol.feature.home.components.UserStatusCard
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "PHOENIX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = TextSecondary
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: notifications */ }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        StorySection(
            onNavigateToFeature = { feature ->
                when (feature) {
                    "focus_timer" -> navController.navigate(Screen.FocusTimer.route)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        UserStatusCard()

        Spacer(modifier = Modifier.height(16.dp))
        FeatureGrid(
            onNavigate = { route -> navController.navigate(route) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        SupportFooter(
            onNavigateToSupport = { navController.navigate(Screen.Support.route) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
