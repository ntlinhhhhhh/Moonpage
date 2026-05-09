package com.diary.moonpage.data.remote.dto.auth

import com.diary.moonpage.domain.model.User

data class UserResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val gender: String?,
    val birthday: String?
) {
    fun toDomain(token: String = ""): User {
        return User(
            token = token,
            userId = id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            gender = gender,
            birthday = birthday
        )
    }
}
