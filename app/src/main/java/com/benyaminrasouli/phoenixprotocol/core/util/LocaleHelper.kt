package com.benyaminrasouli.phoenixprotocol.core.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    private fun getLocale(language: String): Locale {
        return when (language) {
            "fa" -> Locale.Builder().setLanguage("fa").setRegion("IR").build()
            else -> Locale("en")
        }
    }

    fun onAttach(base: Context, language: String): Context {
        val locale = getLocale(language)
        Locale.setDefault(locale)

        val configuration = base.resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        return base.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    fun setLocale(activity: Activity, language: String) {
        val locale = getLocale(language)
        Locale.setDefault(locale)

        val configuration = activity.resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            configuration.locale = locale
        }

        activity.resources.updateConfiguration(configuration, activity.resources.displayMetrics)
    }
}
