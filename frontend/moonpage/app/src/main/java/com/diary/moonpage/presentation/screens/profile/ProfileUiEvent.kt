package com.diary.moonpage.presentation.screens.profile

import android.content.Context
import android.net.Uri

/**
 * Events: UI -> VM
 */
sealed class ProfileUiEvent {
    object LoadProfile : ProfileUiEvent()
    data class UpdateProfile(val name: String, val gender: String?, val birthday: String?) : ProfileUiEvent()
    data class UpdateAvatar(val context: Context, val uri: Uri) : ProfileUiEvent()
    data class DeleteAccount(val id: String) : ProfileUiEvent()
}

/**
 * Effects: VM -> UI
 */
sealed class ProfileUiEffect {
    data class ShowSnackBar(val message: String) : ProfileUiEffect()
    object UpdateSuccess : ProfileUiEffect()
    object AccountDeleted : ProfileUiEffect()
}
