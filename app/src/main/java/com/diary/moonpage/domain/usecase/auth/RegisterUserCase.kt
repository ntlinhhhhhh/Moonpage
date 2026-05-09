package com.diary.moonpage.domain.usecase.auth

import com.diary.moonpage.data.remote.dto.auth.RegisterRequestDTO
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.repository.AuthRepository
import jakarta.inject.Inject

class RegisterUserCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: RegisterRequestDTO): Result<User> {
        return repository.register(request)
    }
}