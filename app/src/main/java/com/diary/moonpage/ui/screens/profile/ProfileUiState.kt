package com.diary.moonpage.ui.screens.profile

import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.model.Theme

data class ProfileUiState(
    val user: User? = null,
    val localAvatarPath: String? = null,
    val tempAvatarPath: String? = null,
    val myThemes: List<Theme> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val totalLogs: Int = 0,
    val totalPhotos: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val streakFreezeCount: Int = 0
)
