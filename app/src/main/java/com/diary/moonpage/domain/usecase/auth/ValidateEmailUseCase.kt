package com.diary.moonpage.domain.usecase.auth

class ValidateEmailUseCase {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")

    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Email cannot be empty."
            )
        }
        if (!emailRegex.matches(email)) {
            return ValidationResult(
                successful = false,
                errorMessage = "Invalid email format."
            )
        }
        return ValidationResult(successful = true)
    }
}
