package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.diary.moonpage.widget.glance.MoonpageWidgets
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
        private val SHOW_MONTHLY_MOOD_STREAK_KEY = booleanPreferencesKey("show_monthly_mood_streak")
        private val SHOW_MONTHLY_MOOD_GRID_KEY = booleanPreferencesKey("show_monthly_mood_grid")

        // Quick Mood Keys
        private val SHOW_QUICK_MOOD_STREAK_KEY = booleanPreferencesKey("show_quick_mood_streak")
        private val SHOW_QUICK_MOOD_LABELS_KEY = booleanPreferencesKey("show_quick_mood_labels")

        // Weekly Mood Keys
        private val SHOW_WEEKLY_MOOD_STREAK_KEY = booleanPreferencesKey("show_weekly_mood_streak")
        private val SHOW_WEEKLY_MOOD_DATES_KEY = booleanPreferencesKey("show_weekly_mood_dates")

        private val LAST_UPDATE_TRIGGER_KEY = longPreferencesKey("last_update_trigger")
    }

    // Daily Summary Flows
    val showDailyStreak: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_DAILY_STREAK_KEY] ?: true }
    val showDailyNote: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_DAILY_NOTE_KEY] ?: true }
    val showDailyStats: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_DAILY_STATS_KEY] ?: true }

    // Photo Moment Flows
    val showPhotoStreak: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_PHOTO_STREAK_KEY] ?: true }
    val photoDisplayMode: Flow<String> = context.widgetSettingsDataStore.data.map { it[PHOTO_DISPLAY_MODE_KEY] ?: "CROP" }

    // Monthly Mood Flows
    val showMonthlyMoodStreak: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_MONTHLY_MOOD_STREAK_KEY] ?: true }
    val showMonthlyMoodGrid: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_MONTHLY_MOOD_GRID_KEY] ?: true }

    // Quick Mood Flows
    val showQuickMoodStreak: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_QUICK_MOOD_STREAK_KEY] ?: true }
    val showQuickMoodLabels: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_QUICK_MOOD_LABELS_KEY] ?: true }

    // Weekly Mood Flows
    val showWeeklyMoodStreak: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_WEEKLY_MOOD_STREAK_KEY] ?: true }
    val showWeeklyMoodDates: Flow<Boolean> = context.widgetSettingsDataStore.data.map { it[SHOW_WEEKLY_MOOD_DATES_KEY] ?: true }

    val lastUpdateTrigger: Flow<Long> = context.widgetSettingsDataStore.data.map { it[LAST_UPDATE_TRIGGER_KEY] ?: 0L }

    suspend fun setShowDailyStreak(show: Boolean) = updatePreference { it[SHOW_DAILY_STREAK_KEY] = show }
    suspend fun setShowDailyNote(show: Boolean) = updatePreference { it[SHOW_DAILY_NOTE_KEY] = show }
    suspend fun setShowDailyStats(show: Boolean) = updatePreference { it[SHOW_DAILY_STATS_KEY] = show }

    suspend fun setShowPhotoStreak(show: Boolean) = updatePreference { it[SHOW_PHOTO_STREAK_KEY] = show }
    suspend fun setPhotoDisplayMode(mode: String) = updatePreference { it[PHOTO_DISPLAY_MODE_KEY] = mode }

    suspend fun setShowMonthlyMoodStreak(show: Boolean) = updatePreference { it[SHOW_MONTHLY_MOOD_STREAK_KEY] = show }
    suspend fun setShowMonthlyMoodGrid(show: Boolean) = updatePreference { it[SHOW_MONTHLY_MOOD_GRID_KEY] = show }
    suspend fun setShowQuickMoodStreak(show: Boolean) = updatePreference { it[SHOW_QUICK_MOOD_STREAK_KEY] = show }
    suspend fun setShowQuickMoodLabels(show: Boolean) = updatePreference { it[SHOW_QUICK_MOOD_LABELS_KEY] = show }
    suspend fun setShowWeeklyMoodStreak(show: Boolean) = updatePreference { it[SHOW_WEEKLY_MOOD_STREAK_KEY] = show }
    suspend fun setShowWeeklyMoodDates(show: Boolean) = updatePreference { it[SHOW_WEEKLY_MOOD_DATES_KEY] = show }

    private suspend fun updatePreference(update: (MutablePreferences) -> Unit) {
        context.widgetSettingsDataStore.edit { prefs ->
            update(prefs)
            prefs[LAST_UPDATE_TRIGGER_KEY] = System.currentTimeMillis()
        }
        MoonpageWidgets.refreshAll(context.applicationContext)
    }
}
