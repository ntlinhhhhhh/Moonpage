package com.diary.moonpage.presentation.screens.auth

import com.diary.moonpage.core.util.UiText

sealed class AuthUiEvent {
    // Actions from UI
    data class OnEmailChanged(val email: String) : AuthUiEvent()
    data class OnUsernameChanged(val username: String) : AuthUiEvent()
    data class OnPasswordChanged(val password: String) : AuthUiEvent()
    data class OnConfirmPasswordChanged(val confirmPassword: String) : AuthUiEvent()
    data class OnOtpCodeChanged(val otpCode: String) : AuthUiEvent()
    object OnLoginClicked : AuthUiEvent()
    object OnRegisterClicked : AuthUiEvent()
    data class OnGoogleLoginClicked(val idToken: String) : AuthUiEvent()
    object OnForgotPasswordClicked : AuthUiEvent()
    object OnVerifyOtpClicked : AuthUiEvent()
    object OnResetPasswordClicked : AuthUiEvent()
    object OnLogoutClicked : AuthUiEvent()

    // States/Effects for UI (Merged back)
    data class LoginSuccess(val token: String, val userId: String, val isNewUser: Boolean = false) : AuthUiEvent()
    data class RegisterSuccess(val message: String) : AuthUiEvent()
    data class ShowSnackBar(val message: UiText) : AuthUiEvent()
    data class NavigateToVerifyOtp(val email: String) : AuthUiEvent()
    data class NavigateToResetPassword(val email: String, val token: String) : AuthUiEvent()
    object NavigateToLogin : AuthUiEvent()
    object NavigateToRegister : AuthUiEvent()
    data class ResetPasswordSuccess(val message: String) : AuthUiEvent()
}

// Keep AuthUiEffect as empty or remove it if possible, but some files might still import it.
// I'll keep it as a typealias or just leave it for now to avoid breaking imports I didn't see.
typealias AuthUiEffect = AuthUiEvent
