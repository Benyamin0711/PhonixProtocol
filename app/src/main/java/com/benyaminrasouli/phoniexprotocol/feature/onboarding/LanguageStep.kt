package com.benyaminrasouli.phoniexprotocol.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@Composable
fun LanguageStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Language",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select your preferred language",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageOption(
                label = "English",
                isSelected = selectedLanguage == "en",
                onClick = { onLanguageSelected("en") },
                modifier = Modifier.weight(1f)
            )
            LanguageOption(
                label = "Persian",
                isSelected = selectedLanguage == "fa",
                onClick = { onLanguageSelected("fa") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = SurfaceDark,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PhoenixOrange else TextSecondary.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) PhoenixOrange else TextSecondary,
            modifier = Modifier.padding(24.dp)
        )
    }
}
