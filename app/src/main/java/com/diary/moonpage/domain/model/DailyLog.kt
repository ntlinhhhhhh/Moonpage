package com.diary.moonpage.domain.model

import androidx.compose.runtime.Immutable

@Immutable
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
    val musicTitle: String? = null,
    val artistName: String? = null,
    val albumArtUrl: String? = null,
    val dailyPhotos: List<String>?,
    val activityIds: List<String>?,
    val createdAt: String? = null,
    val calories: Int? = null,
    val distance: Double? = null,
    val wakeupTime: String? = null,
    val weather: String? = null,
    val temperature: Double? = null
)
