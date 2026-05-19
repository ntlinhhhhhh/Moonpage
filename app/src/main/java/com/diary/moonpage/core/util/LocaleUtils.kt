package com.diary.moonpage.core.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

object LocaleUtils {
    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(android.os.LocaleList(locale))
        return context.createConfigurationContext(configuration)
    }

    /**
     * Reads the current active language directly from the modern AppCompatDelegate API.
     * This is the single source of truth for Per-App Language Preferences.
     */
    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            locales.get(0)?.language ?: "en"
        } else {
            "en"
        }
    }

    /**
     * Helper to get a readable month name, especially for Vietnamese where
     * standard formatters might return "Thg 1" which is ambiguous.
     */
    fun getFormattedMonthName(month: Int, languageCode: String): String {
        return if (languageCode == "vi") {
            "Tháng $month"
        } else {
            java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, Locale(languageCode))
        }
    }
}
