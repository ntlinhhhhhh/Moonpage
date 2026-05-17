package com.diary.moonpage.data.remote.dto.auth

import com.diary.moonpage.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val gender: String?,
    val birthday: String?,
    val coinBalance: Int? = 0,
    val authProvider: String? = null,
    @SerializedName("streakFreezes") val streakFreezeCount: Int? = 0,
    val currentStreak: Int? = 0,
    val longestStreak: Int? = 0
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
            coinBalance = coinBalance ?: 0,
            authProvider = authProvider ?: "Password",
            streakFreezeCount = streakFreezeCount ?: 0,
            currentStreak = currentStreak ?: 0,
            longestStreak = longestStreak ?: 0
        )
    }
}
