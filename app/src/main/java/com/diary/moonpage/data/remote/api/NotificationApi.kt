package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.notification.*
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getNotifications(): Response<NotificationListResponse>

    @POST("api/notifications")
    suspend fun createNotification(
        @Body request: CreateNotificationRequest
    ): Response<SingleNotificationResponse>

    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: String
    ): Response<Unit>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") id: String
    ): Response<Unit>

    @DELETE("api/notifications/all")
    suspend fun deleteAllNotifications(): Response<com.diary.moonpage.data.remote.dto.ErrorResponse>

    @POST("api/notifications/send")
    suspend fun sendPushNotification(
        @Body request: SendPushRequest
    ): Response<Unit>
}
