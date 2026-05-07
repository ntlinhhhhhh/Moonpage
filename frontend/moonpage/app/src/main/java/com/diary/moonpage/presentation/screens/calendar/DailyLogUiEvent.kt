package com.diary.moonpage.presentation.screens.calendar

import java.time.LocalDate

/**
 * Events: UI -> VM
 */
sealed class DailyLogUiEvent {
    data class OnMoodSelected(val moodId: Int) : DailyLogUiEvent()
    data class OnActivityToggled(val activityId: String) : DailyLogUiEvent()
    data class OnNoteChanged(val note: String) : DailyLogUiEvent()
    data class OnSleepChanged(val hours: Float) : DailyLogUiEvent()
    data class OnDateChanged(val date: LocalDate) : DailyLogUiEvent()
    object OnSaveClick : DailyLogUiEvent()
    object OnExitClick : DailyLogUiEvent()
    object OnDismissExitDialog : DailyLogUiEvent()
    object OnDismissOverwriteDialog : DailyLogUiEvent()
    object OnConfirmOverwrite : DailyLogUiEvent()
    object OnDatePickerClick : DailyLogUiEvent()
    object OnDatePickerDismiss : DailyLogUiEvent()
    object DismissMessage : DailyLogUiEvent()
}

/**
 * Effects: VM -> UI
 */
sealed class DailyLogUiEffect {
    data class ShowSnackBar(val message: String) : DailyLogUiEffect()
    data class SaveSuccess(val message: String) : DailyLogUiEffect()
    object NavigateBack : DailyLogUiEffect()
}
