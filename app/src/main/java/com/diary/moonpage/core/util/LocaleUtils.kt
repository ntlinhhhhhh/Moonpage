package com.diary.moonpage.core.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleUtils {
    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        return context.createConfigurationContext(configuration)
    }

    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getString("language", "en") ?: "en"
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
