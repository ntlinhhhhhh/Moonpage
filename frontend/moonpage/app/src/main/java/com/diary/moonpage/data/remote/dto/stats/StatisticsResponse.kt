package com.diary.moonpage.data.remote.dto.stats

import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName("totalLogs") val totalLogs: Int,
    @SerializedName("totalPhotos") val totalPhotos: Int,
    @SerializedName("currentStreak") val currentStreak: Int,
    @SerializedName("longestStreak") val longestStreak: Int,
    @SerializedName("moodDistribution") val moodDistribution: List<MoodDistributionDto>,
    @SerializedName("moodFlow") val moodFlow: List<MoodFlowDto>,
    @SerializedName("bestActivities") val bestActivities: List<BestActivityDto>
)

data class MoodDistributionDto(
    @SerializedName("label") val label: String,
    @SerializedName("count") val count: Int,
    @SerializedName("percentage") val percentage: Int
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
