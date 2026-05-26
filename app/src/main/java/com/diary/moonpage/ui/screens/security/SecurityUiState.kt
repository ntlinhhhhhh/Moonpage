package com.diary.moonpage.ui.screens.security

data class SecurityUiState(
    val isPasscodeEnabled: Boolean = false,
    val savedPasscode: String? = null,
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isLoaded: Boolean = false
)
