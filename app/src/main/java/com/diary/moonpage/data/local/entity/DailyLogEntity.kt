package com.diary.moonpage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val id: String,
    val baseMoodId: Int,
    val date: String, // Format: yyyy-MM-dd
    val note: String?,
    val sleepHours: Double?,
    val sleepStartTime: String? = null,
    val isMenstruation: Boolean,
    val menstruationPhase: String?,
    val dailyPhotosJson: String?, // Serialized List<String>
    val activityIdsJson: String?,   // Serialized List<String>
    val steps: Int? = null,
    val musicRecord: String? = null,
    val musicTitle: String? = null,
    val artistName: String? = null,
    val albumArtUrl: String? = null,
    val createdAt: String? = null,
    val calories: Int? = null,
    val distance: Double? = null,
    val wakeupTime: String? = null,
    val weather: String? = null,
    val temperature: Double? = null
) {
    fun toResponse(): DailyLogResponseDto {
        val domain = toDomain()
        return DailyLogResponseDto(
            id = id,
            baseMoodId = baseMoodId,
            date = date,
            note = note,
            sleepHours = sleepHours,
            sleepStartTime = sleepStartTime,
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            dailyPhotos = dailyPhotosJson?.split(",")?.filter { it.isNotBlank() },
            activityIds = activityIdsJson?.split(",")?.filter { it.isNotBlank() },
            steps = steps,
            musicRecord = domain.musicRecord,
            musicTitle = domain.musicTitle,
            artistName = domain.artistName,
            albumArtUrl = domain.albumArtUrl,
            createdAt = createdAt,
            calories = calories,
            distance = distance,
            wakeupTime = wakeupTime,
            weather = weather,
            temperature = temperature
        )
    }

    fun toDomain(): com.diary.moonpage.domain.model.DailyLog {
        val legacyParts = musicRecord
            ?.split(" - ", limit = 2)
            ?.map { it.trim() }
        val resolvedMusicTitle = musicTitle.nonBlankOrNull()
            ?: legacyParts?.getOrNull(0).nonBlankOrNull()
        val resolvedArtistName = artistName.nonBlankOrNull()
            ?: legacyParts?.getOrNull(1).nonBlankOrNull()

        return com.diary.moonpage.domain.model.DailyLog(
            id = id,
            baseMoodId = baseMoodId,
            date = date,
            note = note,
            sleepHours = sleepHours,
            sleepStartTime = sleepStartTime,
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            dailyPhotos = dailyPhotosJson?.split(",")?.filter { it.isNotBlank() },
            activityIds = activityIdsJson?.split(",")?.filter { it.isNotBlank() },
            steps = steps,
            musicRecord = musicRecord.nonBlankOrNull() ?: resolvedMusicTitle,
            musicTitle = resolvedMusicTitle,
            artistName = resolvedArtistName,
            albumArtUrl = albumArtUrl.nonBlankOrNull(),
            createdAt = createdAt,
            calories = calories,
            distance = distance,
            wakeupTime = wakeupTime,
            weather = weather,
            temperature = temperature
        )
    }

    companion object {
        fun fromResponse(response: DailyLogResponseDto): DailyLogEntity {
            val domain = response.toDomain()
            return DailyLogEntity(
                id = domain.id,
                baseMoodId = domain.baseMoodId,
                date = domain.date,
                note = domain.note,
                sleepHours = domain.sleepHours,
                sleepStartTime = domain.sleepStartTime,
                isMenstruation = domain.isMenstruation,
                menstruationPhase = domain.menstruationPhase,
                dailyPhotosJson = domain.dailyPhotos?.joinToString(","),
                activityIdsJson = domain.activityIds?.joinToString(","),
                steps = domain.steps,
                musicRecord = domain.musicRecord,
                musicTitle = domain.musicTitle,
                artistName = domain.artistName,
                albumArtUrl = domain.albumArtUrl,
                createdAt = domain.createdAt,
                calories = domain.calories,
                distance = domain.distance,
                wakeupTime = domain.wakeupTime,
                weather = domain.weather,
                temperature = domain.temperature
            )
        }
    }
}

private fun String?.nonBlankOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
