package com.diary.moonpage.ui

import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.data.remote.dto.notification.NotificationDto
import com.diary.moonpage.ui.components.feedback.SnackbarType

import com.diary.moonpage.domain.model.Theme

data class MainUiState(
    val isReady: Boolean = false,
    val isAppLocked: Boolean = false,
    val themeType: MoonThemeType = MoonThemeType.DEFAULT,
    val activeTheme: Theme? = null,
    val isDarkMode: Boolean? = null,
    val snackbarMessage: UiText? = null,
    val snackbarType: SnackbarType = SnackbarType.INFO,
    val notifications: List<NotificationDto> = emptyList(),
    val language: String = "en"
)
