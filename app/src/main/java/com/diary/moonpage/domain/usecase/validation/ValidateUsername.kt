package com.diary.moonpage.domain.usecase.validation

import com.diary.moonpage.R
import com.diary.moonpage.core.util.UiText
import javax.inject.Inject

class ValidateUsername @Inject constructor() {
    fun execute(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.error_username_empty)
            )
        }
        return ValidationResult(successful = true)
    }
}
