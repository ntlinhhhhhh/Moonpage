package com.diary.moonpage.data.remote.dto.moment

data class MomentResponseDTO(
    val id: String,
    val imageUrl: String,
    val caption: String?,
    val capturedAt: String,
    val isPublic: Boolean
)
