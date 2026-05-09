package com.diary.moonpage.presentation.screens.moment

import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.domain.model.Moment

data class MomentUiState(
    val moments: List<Moment> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val localPaths: Map<String, String> = emptyMap(),
    val selectedMoment: Moment? = null,
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
)
