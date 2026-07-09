package com.benyaminrasouli.phoenixprotocol.feature.home.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun HomeFABBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(SurfaceDark.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left 2 items
            FABNavItem(
                icon = Icons.Filled.PlayArrow,
                label = "Timer",
                route = "focus",
                currentRoute = currentRoute,
                onClick = { onNavigate("focus") }
            )
            FABNavItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Tasks",
                route = "tasklist",
                currentRoute = currentRoute,
                onClick = { onNavigate("tasklist") }
            )

            // Center spacer for FAB
            Spacer(modifier = Modifier.width(72.dp))

            // Right 2 items
            FABNavItem(
                icon = Icons.Filled.DateRange,
                label = "Stats",
                route = "statistics",
                currentRoute = currentRoute,
                onClick = { onNavigate("statistics") }
            )
            FABNavItem(
                icon = Icons.Filled.Person,
                label = "Profile",
                route = "profile",
                currentRoute = currentRoute,
                onClick = { onNavigate("profile") }
            )
        }

        // Center FAB - square with rounded corners, icon fills the square
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(60.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(PhoenixOrange)
                .clickable { onNavigate("home") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Home",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun FABNavItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    onClick: () -> Unit
) {
    val isSelected = currentRoute == route
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PhoenixOrange else TextSecondary,
        label = "fabNavItemColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PhoenixOrange.copy(alpha = 0.15f) else SurfaceDark,
        label = "fabNavItemBg"
    )

    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(contentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
