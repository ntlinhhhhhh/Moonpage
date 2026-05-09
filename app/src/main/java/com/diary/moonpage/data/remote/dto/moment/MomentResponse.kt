package com.diary.moonpage.data.remote.dto.moment

import com.diary.moonpage.domain.model.Moment

data class MomentResponse(
    val id: String,
    val imageUrl: String,
    val caption: String?,
    val capturedAt: String,
    val isPublic: Boolean,
    val location: String? = null,
    val weather: String? = null,
    val rating: Float? = null
) {
    fun toDomain(): Moment {
        return Moment(
            id = id,
            imageUrl = imageUrl,
            caption = caption,
            capturedAt = capturedAt,
            isPublic = isPublic,
            location = location,
            weather = weather,
            rating = rating
        )
    }
}
