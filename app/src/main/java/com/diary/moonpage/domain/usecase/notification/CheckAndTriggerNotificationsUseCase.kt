package com.diary.moonpage.domain.usecase.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.diary.moonpage.ui.MainActivity
import com.diary.moonpage.R
import com.diary.moonpage.data.remote.dto.notification.CreateNotificationRequest
import com.diary.moonpage.data.remote.dto.notification.NotificationType
import com.diary.moonpage.domain.repository.*
import com.diary.moonpage.core.util.UserManager
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.LocaleUtils
import com.diary.moonpage.core.util.ThemePreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

class CheckAndTriggerNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val statsRepository: StatisticsRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val weatherRepository: WeatherRepository,
    private val locationTracker: com.diary.moonpage.core.util.LocationTracker,
    private val userManager: UserManager,
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val themePreferencesManager: ThemePreferencesManager,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() {
        val user = userManager.getUser().firstOrNull()
        val userId = user?.id ?: run {
            android.util.Log.e("TriggerNotify", "No user ID found")
            return
        }
        
        val language = settingsPreferencesManager.language.first()
        val localizedContext = LocaleUtils.applyLocale(context, language)
        
        android.util.Log.d("TriggerNotify", "Starting comprehensive check for user: $userId (lang: $language)")
        
        // 1. Daily Reminder: "How was your day?"
        checkDailyReminder(userId, localizedContext)
        
        // 2. Streak Milestones
        checkStreakMilestones(userId, localizedContext)
        
        // 3. Memory Lane
        checkMemoryLane(userId, localizedContext)
        
        // 4. Mood Trend
        checkMoodTrend(userId, localizedContext)
        
        // 5. Monthly Report
        checkMonthlyReport(userId, localizedContext)

        // 6. Weather Check
        checkWeather(userId, localizedContext)
    }

    private suspend fun checkDailyReminder(userId: String, context: Context) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        dailyLogRepository.getDailyLogByDate(today).onFailure {
            notificationRepository.createNotification(
                CreateNotificationRequest(
                    userId = userId,
                    title = context.getString(R.string.noti_reminder_title),
                    message = context.getString(R.string.noti_reminder_body),
                    type = NotificationType.REMINDER
                )
            )
        }
    }

    private suspend fun checkStreakMilestones(userId: String, context: Context) {
        try {
            val statsResponse = statsRepository.getGlobalSummary()
            if (statsResponse.isSuccessful) {
                val currentStreak = statsResponse.body()?.currentStreak ?: 0
                val (titleRes, bodyRes) = when (currentStreak) {
                    3 -> R.string.noti_milestone_3d_title to R.string.noti_milestone_3d_body
                    7 -> R.string.noti_milestone_7d_title to R.string.noti_milestone_7d_body
                    else -> null to null
                }
                
                if (titleRes != null && bodyRes != null) {
                    notificationRepository.createNotification(
                        CreateNotificationRequest(
                            userId = userId,
                            title = context.getString(titleRes),
                            message = context.getString(bodyRes),
                            type = NotificationType.STREAK
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TriggerNotify", "Streak check error", e)
        }
    }

    private suspend fun checkMemoryLane(userId: String, context: Context) {
        val oneYearAgo = LocalDate.now().minusYears(1).format(DateTimeFormatter.ISO_DATE)
        dailyLogRepository.getDailyLogByDate(oneYearAgo).onSuccess {
            notificationRepository.createNotification(
                CreateNotificationRequest(
                    userId = userId,
                    title = context.getString(R.string.noti_memory_title),
                    message = context.getString(R.string.noti_memory_body),
                    type = NotificationType.MEMORY_LANE
                )
            )
        }
    }

    private suspend fun checkMoodTrend(userId: String, context: Context) {
        val today = LocalDate.now()
        var negativeCount = 0
        for (i in 0..2) {
            val date = today.minusDays(i.toLong()).format(DateTimeFormatter.ISO_DATE)
            dailyLogRepository.getDailyLogByDate(date).onSuccess { log ->
                if (log.baseMoodId <= 2) negativeCount++
            }
        }
        
        if (negativeCount == 3) {
            notificationRepository.createNotification(
                CreateNotificationRequest(
                    userId = userId,
                    title = context.getString(R.string.noti_empathy_title),
                    message = context.getString(R.string.noti_empathy_body),
                    type = NotificationType.MOOD_TREND
                )
            )
        }
    }

    private suspend fun checkMonthlyReport(userId: String, context: Context) {
        val today = LocalDate.now()
        if (today.dayOfMonth == 1) {
            notificationRepository.createNotification(
                CreateNotificationRequest(
                    userId = userId,
                    title = context.getString(R.string.noti_report_monthly_title),
                    message = context.getString(R.string.noti_report_monthly_body),
                    type = NotificationType.MONTHLY_REPORT
                )
            )
        }
    }

    private suspend fun checkWeather(userId: String, context: Context) {
        try {
            // Deduplication: Only notify once per day for weather
            val lastWeatherNotiDate = themePreferencesManager.lastWeatherNotificationDate.first()
            val todayStr = LocalDate.now().toString()
            if (lastWeatherNotiDate == todayStr) {
                android.util.Log.d("TriggerNotify", "Weather notification already sent today")
                return
            }

            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                val today = LocalDate.now()
                weatherRepository.getWeatherConditions(location.latitude, location.longitude, today).onSuccess { result ->
                    val isRainy = result.conditions.any { it.contains("Rain", ignoreCase = true) }
                    
                    if (isRainy) {
                        val title = context.getString(R.string.noti_weather_rainy_title)
                        val message = context.getString(R.string.noti_weather_rainy_body)
                        
                        notificationRepository.createNotification(
                            CreateNotificationRequest(
                                userId = userId,
                                title = title,
                                message = message,
                                type = NotificationType.WEATHER
                            )
                        )
                        showSystemNotification(title, message, NotificationType.WEATHER)
                        themePreferencesManager.setLastWeatherNotificationDate(todayStr)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TriggerNotify", "Weather check error", e)
        }
    }

    private fun showSystemNotification(title: String, message: String, type: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "moonpage_notification_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Moonpage Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Weather and daily reminders"
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, Random.nextInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(Random.nextInt(), notification)
    }
}
