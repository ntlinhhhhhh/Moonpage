package com.diary.moonpage.presentation.screens.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.Activity
import java.time.LocalDate

data class DailyLogUiState(
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val existingLog: DailyLog? = null,
    val selectedMood: Int? = null,
    val selectedActivities: List<String> = emptyList(),
    val noteText: String = "",
    val sleepHours: Float = 7f,
    val isMenstruation: Boolean = false,
    val menstruationPhase: String? = null,
    val dailyPhotos: List<String> = emptyList(),
    val musicTitle: String? = null,
    val enabledCategories: List<String> = emptyList(),
    val dynamicActivities: List<Activity> = emptyList(),
    val showExitDialog: Boolean = false,
    val showDatePicker: Boolean = false,
    val showOverwriteDialog: Boolean = false,
    val pendingDate: LocalDate? = null,
    val snackbarMessage: String? = null
)
