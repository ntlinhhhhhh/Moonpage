package com.diary.moonpage.data.remote.dto.stats

import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName(value = "totalLogs", alternate = ["total_logs"]) val totalLogs: Int,
    @SerializedName(value = "totalPhotos", alternate = ["total_photos"]) val totalPhotos: Int,
    @SerializedName(value = "currentStreak", alternate = ["current_streak"]) val currentStreak: Int,
    @SerializedName(value = "longestStreak", alternate = ["longest_streak"]) val longestStreak: Int,
    @SerializedName("moodDistribution") val moodDistribution: List<MoodDistributionDto>,
    @SerializedName("moodFlow") val moodFlow: List<MoodFlowDto>,
    @SerializedName("bestActivities") val bestActivities: List<BestActivityDto>,
    @SerializedName("totalSteps") val totalSteps: Int? = 0,
    @SerializedName("averageSleepHours") val averageSleepHours: Double? = 0.0,
    @SerializedName("averageSleepStartTime") val averageSleepStartTime: String? = null,
    @SerializedName("sleepAnalysis") val sleepAnalysis: List<SleepAnalysisDto>? = null,
    @SerializedName("musicSummary") val musicSummary: List<MusicSummaryDto>? = null,
    @SerializedName("menstruationData") val menstruationData: List<String>? = null,
    @SerializedName("yearlyMoodGrid") val yearlyMoodGrid: List<MoodFlowDto>? = null
)

data class SleepAnalysisDto(
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String? = null,
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
    @SerializedName("label") val label: String,
    @SerializedName("count") val count: Int,
    @SerializedName("percentage") val percentage: Double
)

data class MoodFlowDto(
    @SerializedName("date") val date: String,
    @SerializedName("moodId") val moodId: Int
)

data class BestActivityDto(
    @SerializedName("activityId") val activityId: String,
    @SerializedName("activityName") val activityName: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("averageMoodScore") val averageMoodScore: Double,
    @SerializedName("occurrence") val occurrence: Int
)
