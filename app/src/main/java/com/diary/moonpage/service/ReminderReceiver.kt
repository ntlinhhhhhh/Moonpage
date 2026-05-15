package com.diary.moonpage.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.diary.moonpage.MainActivity
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

    override fun onReceive(context: Context, intent: Intent) {
        val language = LocaleUtils.getSavedLanguage(context)
        val localizedContext = LocaleUtils.applyLocale(context, language)
        
        // Show the notification
        showNotification(localizedContext)

        // Reschedule for the next day
        CoroutineScope(Dispatchers.IO).launch {
            if (settingsPreferencesManager.isReminderEnabled.first()) {
                val timeStr = settingsPreferencesManager.reminderTime.first()
                val time = timeStr.split(":")
                if (time.size == 2) {
                    reminderManager.scheduleDailyReminder(time[0].toInt(), time[1].toInt())
                }
            }
        }
    }

    private fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to log your day"
            }
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(context.getString(R.string.noti_reminder_title))
            .setContentText(context.getString(R.string.noti_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
