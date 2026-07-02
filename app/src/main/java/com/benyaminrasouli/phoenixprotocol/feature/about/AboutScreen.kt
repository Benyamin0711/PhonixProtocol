package com.benyaminrasouli.phoenixprotocol.feature.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.BuildConfig
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceVariantDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.about_title)) },
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
            item { AppHeader() }
            item { DescriptionSection() }
            item { DeveloperSection() }
            item { SocialLinksSection() }
            item { LegalSection() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(96.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "\uD83D\uDD25",
                    fontSize = 48.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.about_app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun DescriptionSection() {
    AboutSection(title = null) {
        Text(
            text = stringResource(R.string.about_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DeveloperSection() {
    AboutSection(title = stringResource(R.string.about_developer)) {
        AboutInfoItem(
            label = stringResource(R.string.about_developer_name),
            icon = { Icon(Icons.Default.Person, contentDescription = null, tint = PhoenixOrange, modifier = Modifier.size(24.dp)) }
        )
        HorizontalDivider(color = SurfaceVariantDark)
        AboutInfoItem(
            label = stringResource(R.string.about_contact_email),
            icon = { Icon(Icons.Default.Email, contentDescription = null, tint = PhoenixOrange, modifier = Modifier.size(24.dp)) }
        )
    }
}

@Composable
private fun SocialLinksSection() {
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.about_github_url)
    val websiteUrl = stringResource(R.string.about_website_url)

    AboutSection(title = null) {
        AboutLinkItem(
            title = stringResource(R.string.about_github),
            subtitle = githubUrl,
            icon = { Icon(Icons.Default.Code, contentDescription = null, tint = PhoenixOrange, modifier = Modifier.size(24.dp)) },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$githubUrl"))
                context.startActivity(intent)
            }
        )
        HorizontalDivider(color = SurfaceVariantDark)
        AboutLinkItem(
            title = stringResource(R.string.about_website),
            subtitle = websiteUrl,
            icon = { Icon(Icons.Default.Language, contentDescription = null, tint = PhoenixOrange, modifier = Modifier.size(24.dp)) },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$websiteUrl"))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun LegalSection() {
    val context = LocalContext.current
    val websiteUrl = stringResource(R.string.about_website_url)

    AboutSection(title = null) {
        AboutLinkItem(
            title = stringResource(R.string.about_privacy_policy),
            subtitle = null,
            icon = { Icon(Icons.Default.Policy, contentDescription = null, tint = PhoenixOrange, modifier = Modifier.size(24.dp)) },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$websiteUrl/privacy"))
                context.startActivity(intent)
            }
        )
        HorizontalDivider(color = SurfaceVariantDark)
        AboutLinkItem(
            title = stringResource(R.string.about_terms_of_service),
            subtitle = null,
            icon = { Icon(Icons.Default.Web, contentDescription = null, tint = PhoenixOrange, modifier = Modifier.size(24.dp)) },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$websiteUrl/terms"))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun AboutSection(
    title: String?,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = PhoenixOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun AboutInfoItem(
    label: String,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AboutLinkItem(
    title: String,
    subtitle: String?,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
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
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
