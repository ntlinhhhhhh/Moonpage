package com.diary.moonpage.domain.model

data class User(
    val token: String,
    val userId: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val gender: String?,
    val birthday: String?
) {
    // Alias for UI compatibility if needed
    val id: String get() = userId
}
