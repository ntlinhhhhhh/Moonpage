package com.diary.moonpage.ui.screens.calendar

import androidx.annotation.StringRes
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.ui.components.feedback.SnackbarType

/**
 * Effects: VM -> UI
 */
sealed class DailyLogUiEffect {
    data class ShowSnackBar(
        val message: UiText,
        val type: SnackbarType = SnackbarType.INFO
    ) : DailyLogUiEffect()
    data class SaveSuccess(val date: String, @StringRes val snackbarMessageResId: Int) : DailyLogUiEffect()
    object NavigateBack : DailyLogUiEffect()
    data class LaunchHealthPermissions(val permissions: Set<String>) : DailyLogUiEffect()
    data class NavigateToPlayStore(val packageName: String) : DailyLogUiEffect()
}
