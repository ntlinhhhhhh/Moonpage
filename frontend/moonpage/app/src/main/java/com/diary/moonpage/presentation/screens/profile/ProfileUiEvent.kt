package com.diary.moonpage.presentation.screens.profile

sealed class ProfileUiEvent {
    data class ShowSnackBar(val message: String) : ProfileUiEvent()
    object UpdateSuccess : ProfileUiEvent()
}
