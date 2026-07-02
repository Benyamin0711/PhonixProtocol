package com.benyaminrasouli.phoenixprotocol.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var categoryDao: CategoryDao

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var themeSelector: RadioGroup
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        setResult(RESULT_CANCELED)

        val intent = intent
        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        themeSelector = findViewById(R.id.theme_selector)
        categorySpinner = findViewById(R.id.category_spinner)
        saveButton = findViewById(R.id.save_button)

        loadCurrentSettings()

        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadCurrentSettings() {
        lifecycleScope.launch {
            val currentTheme = settingsDataStore.widgetTheme.first()
            when (currentTheme) {
                "DARK" -> themeSelector.check(R.id.radio_dark)
                "LIGHT" -> themeSelector.check(R.id.radio_light)
                "ACCENT" -> themeSelector.check(R.id.radio_accent)
            }

            val categories = categoryDao.getAllCategories().first()
            val categoryNames = mutableListOf(getString(R.string.widget_all_categories))
            val categoryIds = mutableListOf(-1L)

            for (category in categories) {
                categoryNames.add(category.name)
                categoryIds.add(category.id)
            }

            val adapter = ArrayAdapter(
                this@WidgetConfigActivity,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            val currentFilterId = settingsDataStore.widgetCategoryFilter.first()
            val selectedIndex = categoryIds.indexOf(currentFilterId).takeIf { it >= 0 } ?: 0
            categorySpinner.setSelection(selectedIndex)
        }
    }

    private fun saveSettings() {
        val theme = when (themeSelector.checkedRadioButtonId) {
            R.id.radio_dark -> "DARK"
            R.id.radio_light -> "LIGHT"
            R.id.radio_accent -> "ACCENT"
            else -> "DARK"
        }

        val categories = categoryDao.getAllCategories()
        lifecycleScope.launch {
            val categoryList = categories.first()
            val categoryIds = mutableListOf(-1L)
            for (category in categoryList) {
                categoryIds.add(category.id)
            }

            val selectedIndex = categorySpinner.selectedItemPosition
            val selectedCategoryId = categoryIds[selectedIndex]

            settingsDataStore.setWidgetTheme(theme)
            settingsDataStore.setWidgetCategoryFilter(selectedCategoryId)

            val updateIntent = Intent(this@WidgetConfigActivity, PhoenixWidgetProvider::class.java)
            updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            sendBroadcast(updateIntent)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
