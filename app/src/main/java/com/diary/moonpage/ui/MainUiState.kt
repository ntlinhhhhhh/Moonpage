package com.diary.moonpage.ui

import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.data.remote.dto.notification.NotificationDto

data class MainUiState(
    val isReady: Boolean = false,
    val isAppLocked: Boolean = false,
    val themeType: MoonThemeType = MoonThemeType.DEFAULT,
    val isDarkMode: Boolean? = null,
    val snackbarMessage: String? = null,
    val notifications: List<NotificationDto> = emptyList(),
    val language: String = "en"
)
