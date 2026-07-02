package com.benyaminrasouli.phoniexprotocol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.navigation.NavGraph
import com.benyaminrasouli.phoniexprotocol.core.util.LocaleHelper
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixProtocolTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun attachBaseContext(newBase: android.content.Context) {
        val prefs = newBase.getSharedPreferences("phoenix_locale_prefs", MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        val updatedBase = LocaleHelper.onAttach(newBase, lang)
        super.attachBaseContext(updatedBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val language by settingsDataStore.language.collectAsState(initial = "en")

            LaunchedEffect(language) {
                val currentLang = getSharedPreferences("phoenix_locale_prefs", MODE_PRIVATE)
                    .getString("language", "en") ?: "en"
                if (language != currentLang) {
                    LocaleHelper.setLocale(this@MainActivity, language)
                    getSharedPreferences("phoenix_locale_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("language", language)
                        .apply()
                    recreate()
                }
            }

            PhoenixProtocolTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
