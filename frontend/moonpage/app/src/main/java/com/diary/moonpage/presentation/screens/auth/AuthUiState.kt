package com.diary.moonpage.presentation.screens.auth

import com.diary.moonpage.core.util.UiText

data class AuthUiState (
    val emailInput: String = "",
    val usernameInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val otpCodeInput: String = "",

    val savedEmailForOtp: String = "",
    val resetToken: String = "",


    val emailError: UiText? = null,
    val usernameError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null,
    val otpCodeError: UiText? = null,

    val isLoading: Boolean = false,
    val loadingProgress: Float = 0f,
    val generalError: UiText? = null
)
