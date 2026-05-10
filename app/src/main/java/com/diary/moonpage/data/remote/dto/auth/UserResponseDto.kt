package com.diary.moonpage.data.remote.dto.auth

import com.diary.moonpage.domain.model.User

data class UserResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val gender: String?,
    val birthday: String?,
    val coinBalance: Int? = 0
) {
    fun toDomain(token: String = ""): User {
        return User(
            token = token,
            userId = id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            gender = gender,
            birthday = birthday,
            coinBalance = coinBalance ?: 0
        )
    }
}
