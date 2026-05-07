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
    val isMenstruation: Boolean,
    val menstruationPhase: String?,
    val dailyPhotosJson: String?, // Serialized List<String>
    val activityIdsJson: String?   // Serialized List<String>
) {
    fun toResponse(): DailyLogResponseDto {
        return DailyLogResponseDto(
            id = id,
            baseMoodId = baseMoodId,
            date = date,
            note = note,
            sleepHours = sleepHours,
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            dailyPhotos = dailyPhotosJson?.split(",")?.filter { it.isNotBlank() },
            activityIds = activityIdsJson?.split(",")?.filter { it.isNotBlank() }
        )
    }

    fun toDomain(): com.diary.moonpage.domain.model.DailyLog {
        return com.diary.moonpage.domain.model.DailyLog(
            id = id,
            baseMoodId = baseMoodId,
            date = date,
            note = note,
            sleepHours = sleepHours,
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            dailyPhotos = dailyPhotosJson?.split(",")?.filter { it.isNotBlank() },
            activityIds = activityIdsJson?.split(",")?.filter { it.isNotBlank() }
        )
    }

    companion object {
        fun fromResponse(response: DailyLogResponseDto): DailyLogEntity {
            return DailyLogEntity(
                id = response.id,
                baseMoodId = response.baseMoodId,
                date = response.date,
                note = response.note,
                sleepHours = response.sleepHours,
                isMenstruation = response.isMenstruation,
                menstruationPhase = response.menstruationPhase,
                dailyPhotosJson = response.dailyPhotos?.joinToString(","),
                activityIdsJson = response.activityIds?.joinToString(",")
            )
        }
    }
}
