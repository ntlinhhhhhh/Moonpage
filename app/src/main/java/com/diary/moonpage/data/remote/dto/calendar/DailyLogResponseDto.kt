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
    @SerializedName(value = "musicRecord", alternate = ["MusicRecord"]) val musicRecord: String? = null,
    @SerializedName(value = "musicTitle", alternate = ["MusicTitle"]) val musicTitle: String? = null,
    @SerializedName(value = "artistName", alternate = ["ArtistName"]) val artistName: String? = null,
    @SerializedName(value = "albumArtUrl", alternate = ["AlbumArtUrl"]) val albumArtUrl: String? = null,
    @SerializedName(value = "dailyPhotos", alternate = ["DailyPhotos"]) val dailyPhotos: List<String>?,
    @SerializedName(value = "activityIds", alternate = ["ActivityIds"]) val activityIds: List<String>?,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName(value = "calories", alternate = ["Calories"]) val calories: Int? = null,
    @SerializedName(value = "distance", alternate = ["Distance"]) val distance: Double? = null,
    @SerializedName(value = "wakeupTime", alternate = ["WakeupTime"]) val wakeupTime: String? = null,
    @SerializedName(value = "weather", alternate = ["Weather"]) val weather: String? = null,
    @SerializedName(value = "temperature", alternate = ["Temperature"]) val temperature: Double? = null
) {
    fun toDomain(): DailyLog {
        val legacyParts = musicRecord
            ?.split(" - ", limit = 2)
            ?.map { it.trim() }
        val resolvedMusicTitle = musicTitle.nonBlankOrNull()
            ?: legacyParts?.getOrNull(0).nonBlankOrNull()
        val resolvedArtistName = artistName.nonBlankOrNull()
            ?: legacyParts?.getOrNull(1).nonBlankOrNull()

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
            musicRecord = musicRecord.nonBlankOrNull() ?: resolvedMusicTitle,
            musicTitle = resolvedMusicTitle,
            artistName = resolvedArtistName,
            albumArtUrl = albumArtUrl.nonBlankOrNull(),
            dailyPhotos = dailyPhotos,
            activityIds = activityIds,
            createdAt = createdAt,
            calories = calories,
            distance = distance,
            wakeupTime = wakeupTime,
            weather = weather,
            temperature = temperature
        )
    }
}

private fun String?.nonBlankOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
