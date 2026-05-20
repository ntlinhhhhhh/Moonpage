package com.diary.moonpage.data.remote.dto.auth

data class ConfirmPasswordRequestDTO(
    val password: String? = null,
    val googleIdToken: String? = null
)
