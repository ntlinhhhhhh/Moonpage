package com.diary.moonpage.data.remote.dto.stats

import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName(value = "totalLogs", alternate = ["total_logs", "TotalLogs"]) val totalLogs: Int,
    @SerializedName(value = "totalPhotos", alternate = ["total_photos", "TotalPhotos"]) val totalPhotos: Int,
    @SerializedName(value = "currentStreak", alternate = ["current_streak", "CurrentStreak"]) val currentStreak: Int,
    @SerializedName(value = "longestStreak", alternate = ["longest_streak", "LongestStreak"]) val longestStreak: Int,
    @SerializedName(value = "streakFreezeCount", alternate = ["streak_freeze_count", "StreakFreezeCount"]) val streakFreezeCount: Int? = 0,
    @SerializedName(value = "moodDistribution", alternate = ["mood_distribution", "MoodDistribution"]) val moodDistribution: List<MoodDistributionDto>,
    @SerializedName(value = "moodFlow", alternate = ["mood_flow", "MoodFlow"]) val moodFlow: List<MoodFlowDto>,
    @SerializedName(value = "bestActivities", alternate = ["best_activities", "BestActivities", "influenceActivities", "influence_activities"]) val bestActivities: List<BestActivityDto>,
    @SerializedName(value = "worstActivities", alternate = ["worst_activities", "WorstActivities"]) val worstActivities: List<BestActivityDto>? = null,
    @SerializedName(value = "performedActivities", alternate = ["performed_activities", "PerformedActivities"]) val performedActivities: List<BestActivityDto>? = null,
    @SerializedName(value = "totalSteps", alternate = ["total_steps", "TotalSteps"]) val totalSteps: Int? = 0,
    @SerializedName(value = "averageSteps", alternate = ["average_steps", "AverageSteps"]) val averageSteps: Double? = 0.0,
    @SerializedName(value = "totalCalories", alternate = ["total_calories", "TotalCalories"]) val totalCalories: Int? = 0,
    @SerializedName(value = "averageCalories", alternate = ["average_calories", "AverageCalories"]) val averageCalories: Double? = 0.0,
    @SerializedName(value = "totalDistance", alternate = ["total_distance", "TotalDistance"]) val totalDistance: Double? = 0.0,
    @SerializedName(value = "averageDistance", alternate = ["average_distance", "AverageDistance"]) val averageDistance: Double? = 0.0,
    @SerializedName(value = "averageSleepHours", alternate = ["average_sleep_hours", "AverageSleepHours"]) val averageSleepHours: Double? = 0.0,
    @SerializedName(value = "averageSleepStartTime", alternate = ["average_sleep_start_time", "AverageSleepStartTime", "averageStartSleep"]) val averageSleepStartTime: String? = null,
    @SerializedName(value = "averageWakeupTime", alternate = ["average_wakeup_time", "AverageWakeupTime", "averageWakeUpTime"]) val averageWakeupTime: String? = null,
    @SerializedName(value = "sleepAnalysis", alternate = ["sleep_analysis", "SleepAnalysis"]) val sleepAnalysis: List<SleepAnalysisDto>? = null,
    @SerializedName(value = "musicSummary", alternate = ["music_summary", "MusicSummary"]) val musicSummary: List<MusicSummaryDto>? = null,
    @SerializedName(value = "menstruationData", alternate = ["menstruation_data", "MenstruationData"]) val menstruationData: List<String>? = null,
    @SerializedName(value = "yearlyMoodGrid", alternate = ["yearly_mood_grid", "YearlyMoodGrid"]) val yearlyMoodGrid: List<MoodFlowDto>? = null
)

data class SleepAnalysisDto(
    @SerializedName("date") val date: String,
    @SerializedName(value = "startTime", alternate = ["sleepStartTime", "SleepStartTime", "start_time"]) val startTime: String? = null,
    @SerializedName("duration") val duration: Double,
    @SerializedName("moodId") val moodId: Int
)

data class MusicSummaryDto(
    @SerializedName("songTitle") val songTitle: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("albumArtUrl") val albumArtUrl: String?,
    @SerializedName("occurrence") val occurrence: Int
)

data class MoodDistributionDto(
    @SerializedName("label") val label: String? = null,
    @SerializedName("baseMoodId") val baseMoodId: Int? = null,
    @SerializedName("count") val count: Int,
    @SerializedName("percentage") val percentage: Double
)

data class MoodFlowDto(
    @SerializedName("date") val date: String,
    @SerializedName("moodId") val moodId: Double
)

data class BestActivityDto(
    @SerializedName("activityId") val activityId: String,
    @SerializedName("activityName") val activityName: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("averageMoodScore") val averageMoodScore: Double,
    @SerializedName("occurrence") val occurrence: Int,
    @SerializedName(value = "moodDistribution", alternate = ["mood_distribution"]) val moodDistribution: List<MoodDistributionDto>? = null
)
