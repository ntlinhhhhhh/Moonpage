package com.diary.moonpage.domain.usecase.auth

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)
