package com.diary.moonpage.domain.usecase.validation

import com.diary.moonpage.core.util.UiText

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: UiText? = null
)
