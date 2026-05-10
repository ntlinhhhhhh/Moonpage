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
    @SerializedName("activityIds") val activityIds: List<String>?,
    @SerializedName("songTitle") val songTitle: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("albumArtUrl") val albumArtUrl: String? = null,
    @SerializedName("sleepBedTime") val sleepBedTime: String? = null,
    @SerializedName("sleepWakeTime") val sleepWakeTime: String? = null,
    @SerializedName("steps") val steps: Int? = null,
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
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            dailyPhotos = dailyPhotos,
            activityIds = activityIds,
            songTitle = songTitle,
            artistName = artistName,
            albumArtUrl = albumArtUrl,
            sleepBedTime = sleepBedTime,
            sleepWakeTime = sleepWakeTime,
            steps = steps,
            calories = calories,
            distance = distance
        )
    }
}
