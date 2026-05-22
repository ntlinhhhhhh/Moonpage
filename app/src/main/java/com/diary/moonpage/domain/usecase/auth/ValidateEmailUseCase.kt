package com.diary.moonpage.domain.usecase.auth

import com.diary.moonpage.core.util.EmailValidator

class ValidateEmailUseCase {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Email cannot be empty."
            )
        }
        if (!EmailValidator.isValid(email)) {
            return ValidationResult(
                successful = false,
                errorMessage = "Invalid email format."
            )
        }
        return ValidationResult(successful = true)
    }
}
