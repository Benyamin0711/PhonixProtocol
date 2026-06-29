package com.benyaminrasouli.phoniexprotocol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.benyaminrasouli.phoniexprotocol.core.navigation.NavGraph
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixProtocolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoenixProtocolTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
