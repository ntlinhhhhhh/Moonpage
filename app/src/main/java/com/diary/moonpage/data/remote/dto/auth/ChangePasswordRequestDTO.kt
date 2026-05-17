package com.diary.moonpage.data.remote.dto.auth

data class ChangePasswordRequestDTO(
    val oldPassword: String,
    val newPassword: String
)
