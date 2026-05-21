package com.diary.moonpage.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.diary.moonpage.ui.MainActivity
import com.diary.moonpage.R
import com.diary.moonpage.core.util.NotificationBus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MoonFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationBus: NotificationBus

    @Inject
    lateinit var notificationRepository: com.diary.moonpage.domain.repository.NotificationRepository

    @Inject
    lateinit var userRepository: com.diary.moonpage.domain.repository.UserRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCMService", "New token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        android.util.Log.d("FCMService", "Message received from: ${message.from}")

        val data = message.data
        val type = data["type"] ?: "SYSTEM"
        val targetId = data["targetId"] ?: data["target_id"]
        val title = data["title"] ?: message.notification?.title ?: "Moonpage"
        val body = data["body"] ?: data["message"] ?: message.notification?.body ?: "You have a new message."

        android.util.Log.d("FCMService", "Payload: title=$title, body=$body, type=$type, targetId=$targetId")

        showNotification(title, body, type, targetId)

        scope.launch {
            withContext(Dispatchers.Main) {
                notificationBus.postEvent(title, body, type, targetId)
            }
            
            try {
                val tokenManager = com.diary.moonpage.core.util.TokenManager(applicationContext)
                val userId = tokenManager.getUserId()

                if (userId != null) {
                    val response = notificationRepository.createNotification(
                        com.diary.moonpage.data.remote.dto.notification.CreateNotificationRequest(
                            userId = userId,
                            title = title,
                            message = body,
                            type = type
                        )
                    )
                    if (response.isSuccessful) {
                        android.util.Log.d("FCMService", "Successfully recorded in-app notification in DB")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FCMService", "Exception while recording in-app notification", e)
            }
        }
    }

    private fun showNotification(title: String, body: String, type: String? = null, targetId: String? = null) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "moonpage_notification_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Moonpage Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders and system notifications"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("notification_type", type)
            putExtra("target_id", targetId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, Random.nextInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        manager.notify(Random.nextInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
