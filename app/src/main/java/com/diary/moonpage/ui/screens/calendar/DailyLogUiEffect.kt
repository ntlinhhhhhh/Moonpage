package com.diary.moonpage.ui.screens.calendar

/**
 * Effects: VM -> UI
 */
sealed class DailyLogUiEffect {
    data class ShowSnackBar(val message: String) : DailyLogUiEffect()
    data class SaveSuccess(val date: String, val snackbarMessage: String) : DailyLogUiEffect()
    object NavigateBack : DailyLogUiEffect()
    data class LaunchHealthPermissions(val permissions: Set<String>) : DailyLogUiEffect()
    data class NavigateToPlayStore(val packageName: String) : DailyLogUiEffect()
}
