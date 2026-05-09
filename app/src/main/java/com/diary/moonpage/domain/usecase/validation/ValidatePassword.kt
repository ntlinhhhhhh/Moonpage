package com.diary.moonpage.domain.usecase.validation

import com.diary.moonpage.R
import com.diary.moonpage.core.util.UiText
import javax.inject.Inject

class ValidatePassword @Inject constructor() {
    fun execute(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.error_password_empty)
            )
        }
        if (password.length < 6) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.error_password_too_short)
            )
        }
        return ValidationResult(successful = true)
    }
}
