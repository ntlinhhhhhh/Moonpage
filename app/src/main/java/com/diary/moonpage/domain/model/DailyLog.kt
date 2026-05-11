package com.diary.moonpage.domain.model

data class DailyLog(
    val id: String,
    val baseMoodId: Int,
    val date: String,
    val note: String?,
    val sleepHours: Double?,
    val sleepStartTime: String? = null,
    val isMenstruation: Boolean,
    val menstruationPhase: String?,
    val steps: Int? = null,
    val musicRecord: String? = null,
    val dailyPhotos: List<String>?,
    val activityIds: List<String>?,
    val createdAt: String? = null,
    val calories: Int? = null,
    val distance: Double? = null
)