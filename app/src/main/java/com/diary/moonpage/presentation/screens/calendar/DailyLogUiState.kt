package com.diary.moonpage.presentation.screens.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.Activity
import com.diary.moonpage.core.theme.MoonThemeType
import java.time.LocalDate
import java.time.LocalTime

data class DailyLogUiState(
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val isImportingHealth: Boolean = false,
    val existingLog: DailyLog? = null,
    val selectedMood: Int? = null,
    val selectedActivities: List<String> = emptyList(),
    val noteText: String = "",
    val sleepHours: Float = 7f,
    val sleepBedTime: LocalTime = LocalTime.of(0, 0),
    val sleepWakeTime: LocalTime = LocalTime.of(7, 0),
    val showSleepDialog: Boolean = false,
    val isMenstruation: Boolean = false,
    val menstruationPhase: String? = null,
    val dailyPhotos: List<String> = emptyList(),
    val momentPhotos: List<String> = emptyList(),
    val musicTitle: String? = null,
    val artistName: String? = null,
    val albumArtUrl: String? = null,
    val steps: Int = 0,
    val calories: Int = 0,
    val distance: Double = 0.0,
    val isSpotifyLinked: Boolean = false,
    val enabledCategories: List<String> = emptyList(),
    val expandedCategories: Set<String> = emptySet(),
    val dynamicActivities: List<Activity> = emptyList(),
    val showExitDialog: Boolean = false,
    val showDatePicker: Boolean = false,
    val showOverwriteDialog: Boolean = false,
    val showSpotifyAuthDialog: Boolean = false,
    val pendingDate: LocalDate? = null,
    val themeType: MoonThemeType = MoonThemeType.DEFAULT,
    val customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null,
    val gender: String? = null,
    val suggestedWeather: com.diary.moonpage.domain.repository.WeatherData? = null,
    val recentTracks: List<com.diary.moonpage.data.remote.api.SpotifyTrack> = emptyList(),
    val isInitialized: Boolean = false,
    val zoomImageUrl: String? = null
)
