package com.diary.moonpage.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequestDTO (
    @SerializedName("email")
    val email: String,
    @SerializedName("resetToken")
    val resetToken: String,
    @SerializedName("newPassword")
    val newPassword: String
)
