package com.diary.moonpage.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.diary.moonpage.ui.MainActivity
import com.diary.moonpage.R
import com.diary.moonpage.core.util.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: com.diary.moonpage.core.util.ReminderManager

    @Inject
    lateinit var settingsPreferencesManager: com.diary.moonpage.core.util.SettingsPreferencesManager

    @Inject
    lateinit var notificationBus: com.diary.moonpage.core.util.NotificationBus

    @Inject
    lateinit var notificationRepository: com.diary.moonpage.domain.repository.NotificationRepository

    override fun onReceive(context: Context, intent: Intent) {
        val language = context
            .getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        val localizedContext = LocaleUtils.applyLocale(context, language)
        val title = localizedContext.getString(R.string.noti_reminder_title)
        val body = localizedContext.getString(R.string.noti_reminder_body)
        val type = "REMINDER"

        // Show the system tray notification
        showNotification(localizedContext, title, body)

        // Process in-app logic
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Post to bus for in-app Snackbar (Real-time feedback)
            notificationBus.postEvent(title, body, type)

            // 2. Record in database for Notification Center
            try {
                val userId = com.diary.moonpage.core.util.TokenManager(context).getUserId()
                if (userId != null) {
                    notificationRepository.createNotification(
                        com.diary.moonpage.data.remote.dto.notification.CreateNotificationRequest(
                            userId = userId,
                            title = title,
                            message = body,
                            type = type
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderReceiver", "Failed to record reminder in DB", e)
            }

            // 3. Reschedule for the next day
            if (settingsPreferencesManager.isReminderEnabled.first()) {
                val timeStr = settingsPreferencesManager.reminderTime.first()
                val time = timeStr.split(":")
                if (time.size == 2) {
                    reminderManager.scheduleDailyReminder(time[0].toInt(), time[1].toInt())
                }
            }
        }
    }

    private fun showNotification(context: Context, title: String, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to log your day"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        manager.notify(1001, notification)
    }
}
