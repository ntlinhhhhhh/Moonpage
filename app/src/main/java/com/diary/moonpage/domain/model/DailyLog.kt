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
    val activityIds: List<String>?,
    val songTitle: String? = null,
    val artistName: String? = null,
    val albumArtUrl: String? = null,
    val steps: Int? = null,
    val calories: Int? = null,
    val distance: Double? = null
    )