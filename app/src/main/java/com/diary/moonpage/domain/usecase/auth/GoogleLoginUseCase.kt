package com.diary.moonpage.domain.usecase.auth

import com.diary.moonpage.data.remote.dto.auth.GoogleLoginRequestDTO
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        if (idToken.isBlank()) {
            return Result.failure(Exception("Google ID Token is missing."))
        }

        val request = GoogleLoginRequestDTO(idToken)
        return repository.googleLogin(request)
    }
}