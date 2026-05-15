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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MoonFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        android.util.Log.d("FCMService", "Message received from: ${message.from}")

        // Handle Data payload for custom routing
        val type = message.data["type"]
        val targetId = message.data["targetId"]
        
        android.util.Log.d("FCMService", "Data payload: type=$type, targetId=$targetId")

        message.notification?.let {
            android.util.Log.d("FCMService", "Notification payload: title=${it.title}, body=${it.body}")
            showNotification(
                it.title ?: "Moonpage", 
                it.body ?: "Bạn có một thông điệp mới!",
                type,
                targetId
            )
        } ?: run {
            // If only data payload is present
            val title = message.data["title"] ?: "Moonpage"
            val body = message.data["body"] ?: "Check your updates!"
            android.util.Log.d("FCMService", "Handling data-only message: title=$title, body=$body")
            showNotification(title, body, type, targetId)
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
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Add extras for deep linking
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
}