package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.NotificationApi
import com.diary.moonpage.data.remote.dto.notification.*
import com.diary.moonpage.domain.repository.NotificationRepository
import retrofit2.Response
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi
) : NotificationRepository {
    override suspend fun getNotifications(): Response<NotificationListResponse> = api.getNotifications()
    
    override suspend fun createNotification(request: CreateNotificationRequest): Response<SingleNotificationResponse> = 
        api.createNotification(request)
    
    override suspend fun markAsRead(id: String): Response<Unit> = api.markAsRead(id)
    
    override suspend fun deleteNotification(id: String): Response<Unit> = api.deleteNotification(id)

    override suspend fun deleteAllNotifications(): Response<com.diary.moonpage.data.remote.dto.ErrorResponse> = 
        api.deleteAllNotifications()
    
    override suspend fun sendPushNotification(request: SendPushRequest): Response<Unit> = 
        api.sendPushNotification(request)
}
