package com.diary.moonpage.domain.repository

import com.diary.moonpage.data.remote.dto.notification.*
import retrofit2.Response

interface NotificationRepository {
    suspend fun getNotifications(): Response<NotificationListResponse>
    suspend fun createNotification(request: CreateNotificationRequest): Response<SingleNotificationResponse>
    suspend fun markAsRead(id: String): Response<Unit>
    suspend fun deleteNotification(id: String): Response<Unit>
    suspend fun deleteAllNotifications(): Response<com.diary.moonpage.data.remote.dto.ErrorResponse>
    suspend fun sendPushNotification(request: SendPushRequest): Response<Unit>
}
