package com.diary.moonpage.data.remote.dto.auth

import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserResponseDto(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName(value = "avatarUrl", alternate = ["avatar_url", "AvatarUrl"]) val avatarUrl: String?,
    val gender: String?,
    val birthday: String?,
    val coinBalance: Int? = 0,
    val authProvider: String? = null,
    @SerializedName(value = "streakFreezes", alternate = ["streakFreezeCount", "streak_freeze_count"]) val streakFreezeCount: Int? = 0,
    @SerializedName(value = "currentStreak", alternate = ["current_streak", "CurrentStreak"]) val currentStreak: Int? = 0,
    @SerializedName(value = "longestStreak", alternate = ["longest_streak", "LongestStreak"]) val longestStreak: Int? = 0
) {
    fun toDomain(token: String = ""): User {
        return User(
            token = token,
            userId = id,
            name = name,
            email = email,
            avatarUrl = normalizeAppImageUrl(avatarUrl),
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
