package com.diary.moonpage.data.remote.dto.auth

data class UpdateProfileRequestDto(
    val name: String,
    val avatarUrl: String? = null,
    val gender: String? = null,
    val birthday: String? = null
)
