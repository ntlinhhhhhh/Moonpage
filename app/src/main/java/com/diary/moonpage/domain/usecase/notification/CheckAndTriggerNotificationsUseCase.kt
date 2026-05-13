package com.diary.moonpage.domain.usecase.notification

import com.diary.moonpage.data.remote.dto.notification.CreateNotificationRequest
import com.diary.moonpage.data.remote.dto.notification.NotificationType
import com.diary.moonpage.domain.repository.*
import com.diary.moonpage.core.util.UserManager
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CheckAndTriggerNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val statsRepository: StatisticsRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val userManager: UserManager
) {
    suspend operator fun invoke() {
        val user = userManager.getUser().firstOrNull()
        val userId = user?.id ?: run {
            android.util.Log.e("TriggerNotify", "No user ID found")
            return
        }
        
        android.util.Log.d("TriggerNotify", "Starting check for user: $userId")
        
        // 1. Check Streak Milestones
        checkStreakMilestones(userId)
        
        // 2. Check Memory Lane (1 year ago)
        checkMemoryLane(userId)
        
        // 3. Check Mood Trend (Last 3 days negative)
        checkMoodTrend(userId)
    }

    private suspend fun checkStreakMilestones(userId: String) {
        try {
            val statsResponse = statsRepository.getGlobalSummary()
            if (statsResponse.isSuccessful) {
                val currentStreak = statsResponse.body()?.currentStreak ?: 0
                android.util.Log.d("TriggerNotify", "Current streak: $currentStreak")
                
                if (currentStreak > 0 && (currentStreak % 7 == 0 || currentStreak == 3 || currentStreak == 30)) {
                    val response = notificationRepository.createNotification(
                        CreateNotificationRequest(
                            userId = userId,
                            title = "Amazing Streak!",
                            message = "You've recorded for $currentStreak days in a row! Keep it up! 🔥",
                            type = NotificationType.STREAK
                        )
                    )
                    android.util.Log.d("TriggerNotify", "Streak notification response: ${response.code()}")
                }
            } else {
                android.util.Log.e("TriggerNotify", "Failed to get stats: ${statsResponse.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("TriggerNotify", "Streak check error", e)
        }
    }

    private suspend fun checkMemoryLane(userId: String) {
        try {
            val oneYearAgo = LocalDate.now().minusYears(1)
            val dateStr = oneYearAgo.format(DateTimeFormatter.ISO_DATE)
            
            dailyLogRepository.getDailyLogByDate(dateStr).onSuccess { log ->
                android.util.Log.d("TriggerNotify", "Found log from last year")
                val response = notificationRepository.createNotification(
                    CreateNotificationRequest(
                        userId = userId,
                        title = "Memory Lane",
                        message = "Take a look at what you were doing on this day last year! ✨",
                        type = NotificationType.MEMORY_LANE
                    )
                )
                android.util.Log.d("TriggerNotify", "Memory Lane notification response: ${response.code()}")
            }.onFailure {
                android.util.Log.d("TriggerNotify", "No log found for last year ($dateStr)")
            }
        } catch (e: Exception) {
            android.util.Log.e("TriggerNotify", "Memory Lane check error", e)
        }
    }

    private suspend fun checkMoodTrend(userId: String) {
        try {
            val today = LocalDate.now()
            var negativeCount = 0
            
            android.util.Log.d("TriggerNotify", "Checking mood trend...")
            for (i in 0..2) {
                val date = today.minusDays(i.toLong()).format(DateTimeFormatter.ISO_DATE)
                dailyLogRepository.getDailyLogByDate(date).onSuccess { log ->
                    if (log.baseMoodId <= 2) { // 1: Very Sad, 2: Sad
                        negativeCount++
                    }
                }
            }
            
            android.util.Log.d("TriggerNotify", "Negative count: $negativeCount")
            if (negativeCount == 3) {
                val response = notificationRepository.createNotification(
                    CreateNotificationRequest(
                        userId = userId,
                        title = "Mood Trend Check-in",
                        message = "I noticed you've been feeling a bit down lately. Remember to be kind to yourself. You're doing great! 💙",
                        type = NotificationType.MOOD_TREND
                    )
                )
                android.util.Log.d("TriggerNotify", "Mood Trend notification response: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("TriggerNotify", "Mood Trend check error", e)
        }
    }
}
