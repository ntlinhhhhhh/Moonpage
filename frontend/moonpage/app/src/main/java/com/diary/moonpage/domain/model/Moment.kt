package com.diary.moonpage.domain.model

data class Moment(
    val id: String,
    val imageUrl: String,
    val caption: String?,
    val capturedAt: String,
    val isPublic: Boolean,
    val location: String? = null,
    val weather: String? = null,
    val rating: Float? = null
)

