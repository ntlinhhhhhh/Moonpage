package com.diary.moonpage.presentation.screens.moment

import com.diary.moonpage.core.util.UiText
import java.io.File

/**
 * Events: UI -> VM
 */
sealed class MomentUiEvent {
    object LoadMoments : MomentUiEvent()
    data class LoadMomentDetail(val id: String) : MomentUiEvent()
    data class UploadMoment(
        val imageFile: File,
        val caption: String,
        val location: String? = null,
        val weather: String? = null,
        val rating: Float? = null,
        val dailyLogId: String = "default_log_id",
        val isPublic: Boolean = true
    ) : MomentUiEvent()
    data class DeleteMoment(val id: String) : MomentUiEvent()
    data class DownloadMoment(val imageUrl: String) : MomentUiEvent()
    data class ShareMoment(val url: String) : MomentUiEvent()
    object DismissMessage : MomentUiEvent()
    data class ShowSnackBar(val message: UiText) : MomentUiEvent()
    object RefreshWeather : MomentUiEvent()
}

/**
 * Effects: VM -> UI
 */
sealed class MomentUiEffect {
    data class ShowSnackBar(val message: UiText) : MomentUiEffect()
    object UploadSuccess : MomentUiEffect()
    data class NavigateToDetail(val id: String) : MomentUiEffect()
    data class ShareMoment(val url: String) : MomentUiEffect()
    data class DownloadMoment(val url: String) : MomentUiEffect()
}
