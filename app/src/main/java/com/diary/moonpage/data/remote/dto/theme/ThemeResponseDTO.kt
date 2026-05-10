package com.diary.moonpage.data.remote.dto.theme

import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.google.gson.annotations.SerializedName

data class ThemeResponseDTO(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("backgroundUrl") val backgroundUrl: String?
) {
    fun toDomain(): Theme {
        val decorationName = id.replace("theme_", "").uppercase()
        return Theme(
            id = id,
            name = name,
            collection = "Collection",
            price = price,
            isFree = price == 0,
            thumbnailUrl = thumbnailUrl,
            backgroundUrl = backgroundUrl,
            isOwned = false,
            isActive = false,
            description = null,
            type = ThemeType.THEME,
            icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
            primaryColor = thumbnailUrl,
            decoration = decorationName
        )
    }
}

data class ThemeMoodResponseDTO(
    @SerializedName("baseMoodId") val baseMoodId: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("customName") val customName: String
)
