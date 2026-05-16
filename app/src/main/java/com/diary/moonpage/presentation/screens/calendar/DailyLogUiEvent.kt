package com.diary.moonpage.presentation.screens.calendar

import java.time.LocalDate
import java.time.LocalTime

/**
 * Events: UI -> VM
 */
sealed class DailyLogUiEvent {
    data class OnMoodSelected(val moodId: Int) : DailyLogUiEvent()
    data class OnActivityToggled(val activityId: String) : DailyLogUiEvent()
    data class OnNoteChanged(val note: String) : DailyLogUiEvent()
    data class OnSleepChanged(val hours: Float) : DailyLogUiEvent()
    data class OnSleepTimeConfirmed(val bedTime: LocalTime, val wakeTime: LocalTime) : DailyLogUiEvent()
    object OnSleepRecordClick : DailyLogUiEvent()
    object OnSleepDialogDismiss : DailyLogUiEvent()
    data class OnDateChanged(val date: LocalDate) : DailyLogUiEvent()
    data class OnMenstruationToggled(val isMenstruation: Boolean) : DailyLogUiEvent()
    data class OnPhotosChanged(val photos: List<String>) : DailyLogUiEvent()
    data class OnPhotoRemoved(val photoUri: String) : DailyLogUiEvent()
    data class OnMusicChanged(val musicTitle: String?) : DailyLogUiEvent()
    data class OnMusicSelected(val title: String, val artist: String, val imageUrl: String?) : DailyLogUiEvent()
    object OnSaveClick : DailyLogUiEvent()
    object OnExitClick : DailyLogUiEvent()
    object OnDismissExitDialog : DailyLogUiEvent()
    object OnDismissOverwriteDialog : DailyLogUiEvent()
    object OnConfirmOverwrite : DailyLogUiEvent()
    object OnDatePickerClick : DailyLogUiEvent()
    object OnDatePickerDismiss : DailyLogUiEvent()
    object OnImportSteps : DailyLogUiEvent()
    object OnLinkMusicAccount : DailyLogUiEvent()
    object OnSpotifyAuthConfirm : DailyLogUiEvent()
    object OnSpotifyAuthDismiss : DailyLogUiEvent()
    object DismissMessage : DailyLogUiEvent()
    object OnLocationPermissionGranted : DailyLogUiEvent()
}

/**
 * Effects: VM -> UI
 */
sealed class DailyLogUiEffect {
    data class ShowSnackBar(val message: String) : DailyLogUiEffect()
    data class SaveSuccess(val message: String) : DailyLogUiEffect()
    object NavigateBack : DailyLogUiEffect()
}
