package com.diary.moonpage.presentation.screens.calendar

import java.time.LocalDate
import java.time.LocalTime

/**
 * Effects: VM -> UI
 */
sealed class DailyLogUiEffect {
    data class ShowSnackBar(val message: String) : DailyLogUiEffect()
    data class SaveSuccess(val message: String) : DailyLogUiEffect()
    object NavigateBack : DailyLogUiEffect()
    data class LaunchHealthPermissions(val permissions: Set<String>) : DailyLogUiEffect()
}
