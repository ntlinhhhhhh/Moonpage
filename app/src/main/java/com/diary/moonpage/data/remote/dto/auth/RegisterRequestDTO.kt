package com.diary.moonpage.data.remote.dto.auth

data class RegisterRequestDTO (
    val email: String,
    val name: String,
    val password: String
)