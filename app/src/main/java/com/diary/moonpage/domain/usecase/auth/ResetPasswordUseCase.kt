package com.diary.moonpage.domain.usecase.auth

import com.diary.moonpage.data.remote.dto.auth.ResetPasswordRequestDTO
import com.diary.moonpage.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, resetToken: String, newPassword: String): Result<Unit> {
        if (newPassword.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters."))
        }

        val request = ResetPasswordRequestDTO(email, resetToken, newPassword)
        return repository.resetPassword(request)
    }
}
