package com.diary.moonpage.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.diary.moonpage.MainActivity
import com.diary.moonpage.R
import com.diary.moonpage.core.util.NotificationBus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCMService", "New token: $token")
        
        // Sync token with server using existing push endpoint
        scope.launch(Dispatchers.IO) {
            try {
                // We use the push endpoint which accepts a token as a registration/check-in mechanism
                notificationRepository.sendPushNotification(
                    com.diary.moonpage.data.remote.dto.notification.SendPushRequest(
                        token = token,
                        title = "Token Refreshed",
                        body = "Your notification token has been updated.",
                        imageUrl = null
                    )
                )
                android.util.Log.d("FCMService", "Token check-in successful")
            } catch (e: Exception) {
                android.util.Log.e("FCMService", "Token check-in failed: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        android.util.Log.d("FCMService", "Message received from: ${message.from}")

        val type = message.data["type"]
        val targetId = message.data["targetId"]
        val title = message.notification?.title ?: message.data["title"] ?: "Moonpage"
        val body = message.notification?.body ?: message.data["body"] ?: "Bạn có một thông điệp mới!"

        android.util.Log.d("FCMService", "Payload: title=$title, body=$body, type=$type, targetId=$targetId")

        // Post to bus for in-app snackbar
        scope.launch {
            notificationBus.postEvent(title, body, type, targetId)
            
            // Record this notification in the backend database for Notification Center
            val userId = userRepository.currentUser.value?.userId
            if (userId != null) {
                try {
                    notificationRepository.createNotification(
                        com.diary.moonpage.data.remote.dto.notification.CreateNotificationRequest(
                            userId = userId,
                            title = title,
                            message = body,
                            type = type ?: "SYSTEM"
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FCMService", "Failed to record in-app notification", e)
                }
            } else {
                android.util.Log.w("FCMService", "No user logged in, skipping database record")
            }
        }

        // Always show system notification as well
        showNotification(title, body, type, targetId)
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
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type)
            putExtra("target_id", targetId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, Random.nextInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        manager.notify(Random.nextInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
