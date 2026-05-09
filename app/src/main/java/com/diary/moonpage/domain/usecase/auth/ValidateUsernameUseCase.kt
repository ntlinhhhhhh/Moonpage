package com.diary.moonpage.domain.usecase.auth

class ValidateUsernameUseCase {
    operator fun invoke(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Username cannot be empty."
            )
        }
        return ValidationResult(successful = true)
    }
}
