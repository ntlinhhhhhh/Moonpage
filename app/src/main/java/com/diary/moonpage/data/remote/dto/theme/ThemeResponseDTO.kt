package com.diary.moonpage.data.remote.dto.theme

import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType

data class ThemeResponseDTO(
    val id: String,
    val name: String,
    val collection: String?,
    val price: Int,
    val thumbnailUrl: String?,
    val backgroundUrl: String?,
    val description: String?,
    val type: String? // "THEME" or "ICON_PACK"
) {
    fun toDomain(): Theme {
        return Theme(
            id = id,
            name = name,
            collection = collection ?: "Default",
            price = price,
            thumbnailUrl = thumbnailUrl,
            backgroundUrl = backgroundUrl,
            description = description,
            type = if (type == "ICON_PACK") ThemeType.ICON_PACK else ThemeType.THEME
        )
    }
}
