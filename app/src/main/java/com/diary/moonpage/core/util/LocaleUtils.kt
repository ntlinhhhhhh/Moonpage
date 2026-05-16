package com.diary.moonpage.core.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

object LocaleUtils {
    // This is no longer needed with AppCompatDelegate, but we keep it for backwards compatibility 
    // if any external library calls it, returning the context unchanged.
    fun applyLocale(context: Context, languageCode: String): Context {
        return context
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
            // Fallback to system default if no per-app preference is set
            Locale.getDefault().language
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
