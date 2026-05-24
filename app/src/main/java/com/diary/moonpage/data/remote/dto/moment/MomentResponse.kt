package com.diary.moonpage.data.remote.dto.moment

import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.domain.model.Moment
import com.google.gson.annotations.SerializedName

data class MomentResponse(
    val id: String,
    val imageUrl: String,
    val caption: String?,
    val capturedAt: String,
    val isPublic: Boolean,
    @SerializedName(value = "dailyLogId", alternate = ["DailyLogId", "daily_log_id"])
    val dailyLogId: String? = null,
    val location: String? = null,
    val weather: String? = null,
    val rating: Float? = null
) {
    fun toDomain(): Moment {
        return Moment(
            id = id,
            imageUrl = normalizeAppImageUrl(imageUrl).orEmpty(),
            caption = caption,
            capturedAt = capturedAt,
            isPublic = isPublic,
            dailyLogId = dailyLogId,
            location = location,
            weather = weather,
            rating = rating
        )
    }
}
