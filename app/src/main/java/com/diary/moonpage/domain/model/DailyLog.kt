package com.diary.moonpage.domain.model

data class DailyLog(
    val id: String,
    val baseMoodId: Int,
    val date: String,
    val note: String?,
    val sleepHours: Double?,
    val isMenstruation: Boolean,
    val menstruationPhase: String?,
    val dailyPhotos: List<String>?,
    val activityIds: List<String>?
)