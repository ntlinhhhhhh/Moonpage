package com.diary.moonpage.presentation.screens.profile

import com.diary.moonpage.core.theme.MoonThemeType

data class SettingsUiState(
    val language: String = "en",
    val themeType: MoonThemeType = MoonThemeType.DEFAULT,
    val isDarkMode: Boolean? = null, // null for System
    val isPasscodeEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isDeleteAccountDialogShown: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
