package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left buttons
            BottomNavItem(
                icon = Icons.Filled.Person,
                label = stringResource(R.string.bottom_nav_account),
                route = "profile",
                currentRoute = currentRoute,
                onClick = { onNavigate("profile") }
            )

            BottomNavItem(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.bottom_nav_settings),
                route = "settings",
                currentRoute = currentRoute,
                onClick = { onNavigate("settings") }
            )

            Spacer(modifier = Modifier.width(48.dp))

            // Right buttons
            BottomNavItem(
                icon = Icons.Filled.EmojiEvents,
                label = stringResource(R.string.bottom_nav_leaderboard),
                route = "leaderboard",
                currentRoute = currentRoute,
                onClick = { onNavigate("leaderboard") }
            )

            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.Help,
                label = stringResource(R.string.bottom_nav_support),
                route = "support",
                currentRoute = currentRoute,
                onClick = { onNavigate("support") }
            )
        }

        // Center FAB (App Logo)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(64.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(PhoenixOrange)
                .clickable { onNavigate("dashboard") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.bottom_nav_dashboard),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    onClick: () -> Unit
) {
    val isSelected = currentRoute == route
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PhoenixOrange else TextSecondary,
        label = "bottomNavItemColor"
    )

    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
