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
    @SerializedName("sleepData") val sleepData: List<SleepDataDto>? = null,
    @SerializedName("stepsData") val stepsData: List<StepsDataDto>? = null,
    @SerializedName("menstruationData") val menstruationData: List<String>? = null,
    @SerializedName("moodBySleep") val moodBySleep: List<MoodBySleepDto>? = null,
    @SerializedName("yearlyMoodGrid") val yearlyMoodGrid: List<MoodFlowDto>? = null
)

data class SleepDataDto(
    @SerializedName("date") val date: String,
    @SerializedName("hours") val hours: Double
)

data class StepsDataDto(
    @SerializedName("date") val date: String,
    @SerializedName("count") val count: Int
)

data class MoodBySleepDto(
    @SerializedName("range") val range: String, // "< 5h", "6-7h", "8h+"
    @SerializedName("moodDistribution") val moodDistribution: List<MoodDistributionDto>
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
