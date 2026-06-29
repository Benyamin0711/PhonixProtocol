package com.benyaminrasouli.phoniexprotocol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixProtocolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoenixProtocolTheme {
                // NavGraph will be added in Task 5
            }
        }
    }
}
