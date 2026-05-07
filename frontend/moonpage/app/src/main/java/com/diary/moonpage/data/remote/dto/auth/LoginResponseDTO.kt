package com.diary.moonpage.data.remote.dto.auth

import com.diary.moonpage.domain.model.User

data class LoginResponseDTO (
    val token: String,
    val userId: String,
    val name: String,
    val avatarUrl: String?
) {
    fun toUser(): User {
        return User(
            token = token, 
            userId = userId, 
            name = name, 
            email = "", 
            avatarUrl = avatarUrl,
            gender = null,
            birthday = null
        )
    }
}
