package com.diary.moonpage.domain.usecase.validation

import com.diary.moonpage.R
import com.diary.moonpage.core.util.UiText
import javax.inject.Inject

class ValidateEmail @Inject constructor() {
    fun execute(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.error_email_empty)
            )
        }
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        if (!email.matches(Regex(emailPattern))) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.error_invalid_email)
            )
        }
        return ValidationResult(successful = true)
    }
}
