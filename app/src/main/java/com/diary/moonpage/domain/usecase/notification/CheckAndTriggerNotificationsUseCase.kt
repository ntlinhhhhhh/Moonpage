package com.diary.moonpage.domain.usecase.notification

import android.content.Context
import com.diary.moonpage.R
import com.diary.moonpage.data.remote.dto.notification.CreateNotificationRequest
import com.diary.moonpage.data.remote.dto.notification.NotificationType
import com.diary.moonpage.domain.repository.*
import com.diary.moonpage.core.util.UserManager
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.LocaleUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CheckAndTriggerNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val statsRepository: StatisticsRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val userManager: UserManager,
    private val settingsPreferencesManager: SettingsPreferencesManager,
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

        // 6. Weather Check (Morning)
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
        val now = LocalTime.now()
        if (now.hour in 7..8) {
            // Simulate weather based on month (Winter/Rainy)
            val month = LocalDate.now().monthValue
            val (titleRes, bodyRes) = if (month in 5..10) {
                R.string.noti_weather_rainy_title to R.string.noti_weather_rainy_body
            } else {
                R.string.noti_weather_sunny_title to R.string.noti_weather_sunny_body
            }

            notificationRepository.createNotification(
                CreateNotificationRequest(
                    userId = userId,
                    title = context.getString(titleRes),
                    message = context.getString(bodyRes),
                    type = NotificationType.WEATHER
                )
            )
        }
    }
}
