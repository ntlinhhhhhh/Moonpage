package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.widgetSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_settings_prefs")

class WidgetPreferencesManager(
    private val context: Context
) {
    companion object {
        // Daily Summary Keys
        private val SHOW_DAILY_STREAK_KEY = booleanPreferencesKey("show_daily_streak")
        private val SHOW_DAILY_NOTE_KEY = booleanPreferencesKey("show_daily_note")
        private val SHOW_DAILY_STATS_KEY = booleanPreferencesKey("show_daily_stats")

        // Photo Moment Keys
        private val SHOW_PHOTO_STREAK_KEY = booleanPreferencesKey("show_photo_streak")
        private val PHOTO_DISPLAY_MODE_KEY = stringPreferencesKey("photo_display_mode") // "CROP" or "FIT"

        // Monthly Mood Keys
        private val SHOW_MONTHLY_MOOD_GRID_KEY = booleanPreferencesKey("show_monthly_mood_grid")

        // Quick Mood Keys
        private val SHOW_QUICK_MOOD_LABELS_KEY = booleanPreferencesKey("show_quick_mood_labels")

        // Weekly Mood Keys
        private val SHOW_WEEKLY_MOOD_DATES_KEY = booleanPreferencesKey("show_weekly_mood_dates")
    }

    // Daily Summary Flows
    val showDailyStreak: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_DAILY_STREAK_KEY] ?: true }
    val showDailyNote: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_DAILY_NOTE_KEY] ?: true }
    val showDailyStats: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_DAILY_STATS_KEY] ?: true }

    // Photo Moment Flows
    val showPhotoStreak: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_PHOTO_STREAK_KEY] ?: true }
    val photoDisplayMode: Flow<String> = context.widgetSettingsDataStore.data.map { it[PHOTO_DISPLAY_MODE_KEY] ?: "CROP" }

    // Monthly Mood Flows
    val showMonthlyMoodGrid: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_MONTHLY_MOOD_GRID_KEY] ?: true }

    // Quick Mood Flows
    val showQuickMoodLabels: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_QUICK_MOOD_LABELS_KEY] ?: true }

    // Weekly Mood Flows
    val showWeeklyMoodDates: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_WEEKLY_MOOD_DATES_KEY] ?: true }

    suspend fun setShowDailyStreak(show: Boolean) = context.widgetSettingsDataStore.edit { it[SHOW_DAILY_STREAK_KEY] = show }
    suspend fun setShowDailyNote(show: Boolean) = context.widgetSettingsDataStore.edit { it[SHOW_DAILY_NOTE_KEY] = show }
    suspend fun setShowDailyStats(show: Boolean) = context.widgetSettingsDataStore.edit { it[SHOW_DAILY_STATS_KEY] = show }

    suspend fun setShowPhotoStreak(show: Boolean) = context.widgetSettingsDataStore.edit { it[SHOW_PHOTO_STREAK_KEY] = show }
    suspend fun setPhotoDisplayMode(mode: String) = context.widgetSettingsDataStore.edit { it[PHOTO_DISPLAY_MODE_KEY] = mode }
    
    suspend fun setShowMonthlyMoodGrid(show: Boolean) = context.widgetSettingsDataStore.edit { it[SHOW_MONTHLY_MOOD_GRID_KEY] = show }
    suspend fun setShowQuickMoodLabels(show: Boolean) = context.widgetSettingsDataStore.edit { it[SHOW_QUICK_MOOD_LABELS_KEY] = show }
    suspend fun setShowWeeklyMoodDates(show: Boolean) = context.widgetSettingsDataStore.edit { it[SHOW_WEEKLY_MOOD_DATES_KEY] = show }
}
