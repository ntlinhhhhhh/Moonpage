package com.diary.moonpage.presentation.screens.calendar

/**
 * Effects: VM -> UI
 */
sealed class DailyLogUiEffect {
    data class ShowSnackBar(val message: String) : DailyLogUiEffect()
    data class SaveSuccess(val message: String, val msg: String) : DailyLogUiEffect()
    object NavigateBack : DailyLogUiEffect()
    data class LaunchHealthPermissions(val permissions: Set<String>) : DailyLogUiEffect()
    data class NavigateToPlayStore(val packageName: String) : DailyLogUiEffect()
}
