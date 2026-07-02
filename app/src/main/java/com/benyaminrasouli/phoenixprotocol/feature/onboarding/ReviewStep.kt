package com.benyaminrasouli.phoenixprotocol.feature.onboarding

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

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
            text = stringResource(R.string.onboarding_review),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_confirm),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        ReviewRow(label = stringResource(R.string.onboarding_full_name), value = fullName)
        ReviewRow(label = stringResource(R.string.onboarding_username), value = "@$username")
        if (birthYear.isNotBlank()) {
            ReviewRow(label = stringResource(R.string.onboarding_birth_year), value = birthYear)
        }
        ReviewRow(label = stringResource(R.string.onboarding_choose_path), value = identityPath)
        ReviewRow(
            label = stringResource(R.string.drawer_language),
            value = if (language == "en") stringResource(R.string.onboarding_english) else stringResource(R.string.onboarding_persian)
        )
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
