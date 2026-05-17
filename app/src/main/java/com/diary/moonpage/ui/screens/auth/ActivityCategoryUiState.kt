package com.diary.moonpage.ui.screens.auth

import com.diary.moonpage.core.util.ActivityPreferencesManager

data class ActivityCategoryUiState(
    val enabledCategories: Set<String> = ActivityPreferencesManager.DEFAULT_ENABLED,
    val isLoading: Boolean = false
)
