package com.diary.moonpage.data.remote.dto.notification

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName(value = "id", alternate = ["_id", "ID"]) val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String?,
    @SerializedName(value = "isRead", alternate = ["is_read", "read"]) val isRead: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class NotificationListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<NotificationDto>
)

data class SingleNotificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: NotificationDto
)

data class CreateNotificationRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String?
)

data class SendPushRequest(
    @SerializedName("token") val token: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("imageUrl") val imageUrl: String? = null
)
