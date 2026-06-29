package com.benyaminrasouli.phoniexprotocol.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@Composable
fun ReviewStep(
    fullName: String,
    username: String,
    birthYear: String,
    identityPath: String,
    language: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Review Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Confirm your details",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        ReviewRow(label = "Name", value = fullName)
        ReviewRow(label = "Username", value = "@$username")
        if (birthYear.isNotBlank()) {
            ReviewRow(label = "Birth Year", value = birthYear)
        }
        ReviewRow(label = "Identity Path", value = identityPath)
        ReviewRow(label = "Language", value = if (language == "en") "English" else "Persian")
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = PhoenixOrange
        )
    }
}
