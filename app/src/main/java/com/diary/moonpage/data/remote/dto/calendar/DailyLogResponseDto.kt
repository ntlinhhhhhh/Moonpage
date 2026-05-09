package com.diary.moonpage.data.remote.dto.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.google.gson.annotations.SerializedName

data class DailyLogResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("baseMoodId") val baseMoodId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("note") val note: String?,
    @SerializedName("sleepHours") val sleepHours: Double?,
    @SerializedName("isMenstruation") val isMenstruation: Boolean,
    @SerializedName("menstruationPhase") val menstruationPhase: String?,
    @SerializedName("dailyPhotos") val dailyPhotos: List<String>?,
    @SerializedName("activityIds") val activityIds: List<String>?
) {
    fun toDomain(): DailyLog {
        return DailyLog(
            id = id,
            baseMoodId = baseMoodId,
            date = date,
            note = note,
            sleepHours = sleepHours,
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            dailyPhotos = dailyPhotos,
            activityIds = activityIds
        )
    }
}
