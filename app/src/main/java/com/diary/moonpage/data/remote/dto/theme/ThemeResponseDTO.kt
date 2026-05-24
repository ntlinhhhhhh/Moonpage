package com.diary.moonpage.data.remote.dto.theme

import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class ThemeResponseDTO(
    @SerializedName(value = "id", alternate = ["Id"]) val id: String,
    @SerializedName(value = "name", alternate = ["Name"]) val name: String,
    @SerializedName(value = "price", alternate = ["Price"]) val price: Int,
    @SerializedName(value = "thumbnailUrl", alternate = ["ThumbnailUrl"]) val thumbnailUrl: String?,
    @SerializedName(value = "backgroundUrl", alternate = ["BackgroundUrl"]) val backgroundUrl: String?,
    @SerializedName(value = "backgroundDarkColor", alternate = ["BackgroundDarkColor"]) val backgroundDarkColor: String? = null,
    @SerializedName(value = "backgroundLightColor", alternate = ["BackgroundLightColor"]) val backgroundLightColor: String? = null,
    @SerializedName(value = "authorId", alternate = ["AuthorId"]) val authorId: String? = null,
    @SerializedName(value = "isOfficial", alternate = ["IsOfficial"]) val isOfficial: Boolean? = null,
    @SerializedName(value = "category", alternate = ["Category"]) val category: String? = "LIGHT",
    @SerializedName(value = "isActive", alternate = ["IsActive"]) val isActive: Boolean = false
) {
    fun toDomain(): Theme {
        val decorationName = id.replace("theme_", "").uppercase()
        val isOfficialTheme = isOfficial != false
        return Theme(
            id = id,
            name = name,
            collection = if (isOfficialTheme) "Collection" else "Custom Theme",
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
            decoration = if (isOfficialTheme) decorationName else "CUSTOM",
            category = category ?: "LIGHT"
        )
    }
}

data class ThemeMoodResponseDTO(
    @SerializedName(value = "baseMoodId", alternate = ["BaseMoodId"]) val baseMoodId: String,
    @SerializedName(value = "iconColor", alternate = ["IconColor", "iconUrl", "IconUrl"]) val iconUrl: String,
    @SerializedName(value = "customName", alternate = ["CustomName"]) val customName: String? = null
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
    val colors = color.toBackgroundColorStops()
    return if (colors.size >= 2) {
        put("backgroundFillMode", "Gradient")
            .put("solidBackgroundColor", colors.first())
            .put("gradientStartColor", colors[0])
            .put("gradientEndColor", colors[1])
    } else {
        put("backgroundFillMode", "Solid")
            .put("solidBackgroundColor", colors.firstOrNull() ?: color.trim())
    }
}

private fun String.toBackgroundColorStops(): List<String> {
    return trim()
        .removePrefix("gradient:")
        .removePrefix("Gradient:")
        .split(',', '|', ';')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
