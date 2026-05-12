package com.diary.moonpage.data.remote.dto.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.google.gson.annotations.SerializedName

data class DailyLogResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("baseMoodId") val baseMoodId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("note") val note: String?,
    @SerializedName(value = "sleepHours", alternate = ["SleepHours"]) val sleepHours: Double?,
    @SerializedName(value = "sleepStartTime", alternate = ["startSleep", "StartSleep", "SleepStartTime"]) val sleepStartTime: String? = null,
    @SerializedName(value = "isMenstruation", alternate = ["IsMenstruation"]) val isMenstruation: Boolean,
    @SerializedName(value = "menstruationPhase", alternate = ["MenstruationPhase"]) val menstruationPhase: String?,
    @SerializedName(value = "steps", alternate = ["Steps"]) val steps: Int? = null,
    @SerializedName("musicRecord") val musicRecord: String? = null,
    @SerializedName(value = "dailyPhotos", alternate = ["DailyPhotos"]) val dailyPhotos: List<String>?,
    @SerializedName(value = "activityIds", alternate = ["ActivityIds"]) val activityIds: List<String>?,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName(value = "calories", alternate = ["Calories"]) val calories: Int? = null,
    @SerializedName(value = "distance", alternate = ["Distance"]) val distance: Double? = null
) {
    fun toDomain(): DailyLog {
        return DailyLog(
            id = id,
            baseMoodId = baseMoodId,
            date = date,
            note = note,
            sleepHours = sleepHours,
            sleepStartTime = sleepStartTime,
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            steps = steps,
            musicRecord = musicRecord,
            dailyPhotos = dailyPhotos,
            activityIds = activityIds,
            createdAt = createdAt,
            calories = calories,
            distance = distance
        )
    }
}
