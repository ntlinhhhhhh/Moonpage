package com.diary.moonpage.data.remote.dto.theme

import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class ThemeResponseDTO(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("backgroundUrl") val backgroundUrl: String?,
    @SerializedName("backgroundDarkColor") val backgroundDarkColor: String? = null,
    @SerializedName("backgroundLightColor") val backgroundLightColor: String? = null,
    @SerializedName("category") val category: String? = "LIGHT",
    @SerializedName("isActive") val isActive: Boolean = false
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
            isActive = isActive,
            description = buildAppearanceDescription(
                backgroundLightColor = backgroundLightColor,
                backgroundDarkColor = backgroundDarkColor
            ),
            type = ThemeType.THEME,
            icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
            primaryColor = thumbnailUrl,
            decoration = decorationName,
            category = category ?: "LIGHT"
        )
    }
}

data class ThemeMoodResponseDTO(
    @SerializedName("baseMoodId") val baseMoodId: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("customName") val customName: String
)

private fun buildAppearanceDescription(
    backgroundLightColor: String?,
    backgroundDarkColor: String?
): String? {
    if (backgroundLightColor.isNullOrBlank() && backgroundDarkColor.isNullOrBlank()) return null

    return JSONObject().apply {
        backgroundLightColor?.takeIf { it.isNotBlank() }?.let { color ->
            put("light", JSONObject().backgroundColorAppearance(color))
        }
        backgroundDarkColor?.takeIf { it.isNotBlank() }?.let { color ->
            put("dark", JSONObject().backgroundColorAppearance(color))
        }
    }.toString()
}

private fun JSONObject.backgroundColorAppearance(color: String): JSONObject {
    return put("backgroundFillMode", "Solid")
        .put("solidBackgroundColor", color)
}
