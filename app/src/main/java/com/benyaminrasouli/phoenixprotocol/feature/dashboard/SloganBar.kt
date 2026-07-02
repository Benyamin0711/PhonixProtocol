package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

@Composable
fun SloganBar(slogan: String, modifier: Modifier = Modifier) {
    Text(
        text = ">> $slogan",
        style = MaterialTheme.typography.bodyLarge,
        color = PhoenixOrange,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
