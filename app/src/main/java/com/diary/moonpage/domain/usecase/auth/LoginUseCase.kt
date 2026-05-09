package com.diary.moonpage.domain.usecase.auth

import com.diary.moonpage.data.remote.dto.auth.LoginRequestDTO
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor (
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LoginRequestDTO): Result<User> {
        return repository.login(request)
    }
}