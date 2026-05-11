package com.diary.moonpage.data.remote.dto.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.google.gson.annotations.SerializedName

data class DailyLogResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("baseMoodId") val baseMoodId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("note") val note: String?,
    @SerializedName("sleepHours") val sleepHours: Double?,
    @SerializedName("sleepStartTime") val sleepStartTime: String? = null,
    @SerializedName("isMenstruation") val isMenstruation: Boolean,
    @SerializedName("menstruationPhase") val menstruationPhase: String?,
    @SerializedName("steps") val steps: Int? = null,
    @SerializedName("musicRecord") val musicRecord: String? = null,
    @SerializedName("dailyPhotos") val dailyPhotos: List<String>?,
    @SerializedName("activityIds") val activityIds: List<String>?,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("distance") val distance: Double? = null
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
