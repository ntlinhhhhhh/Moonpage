package com.diary.moonpage.domain.usecase.auth

import com.diary.moonpage.core.util.EmailValidator
import com.diary.moonpage.data.remote.dto.auth.ForgotPasswordRequestDTO
import com.diary.moonpage.domain.repository.AuthRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || !EmailValidator.isValid(normalizedEmail)) {
            return Result.failure(Exception("Invalid Email format"))
        }
        val request = ForgotPasswordRequestDTO(normalizedEmail)
        return repository.forgotPassword(request)
    }
}
