package com.diary.moonpage.domain.model

import com.google.gson.annotations.SerializedName

data class DailyLogResponse(
    @SerializedName("id") val id: String,
    @SerializedName("baseMoodId") val baseMoodId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("note") val note: String?,
    @SerializedName("sleepHours") val sleepHours: Double?,
    @SerializedName("isMenstruation") val isMenstruation: Boolean,
    @SerializedName("menstruationPhase") val menstruationPhase: String?,
    @SerializedName("dailyPhotos") val dailyPhotos: List<String>?,
    @SerializedName("activityIds") val activityIds: List<String>?
)