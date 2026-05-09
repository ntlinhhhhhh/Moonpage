package com.diary.moonpage.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequestDTO (
    @SerializedName("idToken")
    val idToken: String
)