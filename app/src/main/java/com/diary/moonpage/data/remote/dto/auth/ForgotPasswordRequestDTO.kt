package com.diary.moonpage.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequestDTO (
    @SerializedName("email")
    val email: String
)