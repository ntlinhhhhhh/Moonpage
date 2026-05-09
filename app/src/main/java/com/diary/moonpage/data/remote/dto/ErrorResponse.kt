package com.diary.moonpage.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("measge") // Handling typo from server if exists
    val measge: String? = null
) {
    fun getDisplayMessage(): String {
        return message ?: error ?: measge ?: "An unknown error occurred"
    }
}
