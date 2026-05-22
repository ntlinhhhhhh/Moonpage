package com.diary.moonpage.domain.usecase.validation

import com.diary.moonpage.R
import com.diary.moonpage.core.util.EmailValidator
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
        if (!EmailValidator.isValid(email)) {
            return ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.error_invalid_email)
            )
        }
        return ValidationResult(successful = true)
    }
}
