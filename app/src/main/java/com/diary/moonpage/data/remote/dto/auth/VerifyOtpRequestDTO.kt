package com.diary.moonpage.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequestDTO (
    @SerializedName("email")
    val email: String,
    @SerializedName("otpCode")
    val otpCode: String
)