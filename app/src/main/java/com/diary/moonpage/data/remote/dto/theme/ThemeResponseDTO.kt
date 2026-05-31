package com.diary.moonpage.data.remote.dto.theme

import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeMood
import com.diary.moonpage.domain.model.ThemeType
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class ThemeResponseDTO(
    @SerializedName(value = "id", alternate = ["Id"]) val id: String,
    @SerializedName(value = "name", alternate = ["Name"]) val name: String,
    @SerializedName(value = "price", alternate = ["Price"]) val price: Int,
    @SerializedName(value = "thumbnailUrl", alternate = ["ThumbnailUrl"]) val thumbnailUrl: String?,
    @SerializedName(value = "backgroundUrl", alternate = ["BackgroundUrl"]) val backgroundUrl: String?,
    @SerializedName(value = "primaryLightColor", alternate = ["PrimaryLightColor"]) val primaryLightColor: String? = null,
    @SerializedName(value = "primaryDarkColor", alternate = ["PrimaryDarkColor"]) val primaryDarkColor: String? = null,
    @SerializedName(value = "backgroundDarkColor", alternate = ["BackgroundDarkColor"]) val backgroundDarkColor: String? = null,
    @SerializedName(value = "backgroundLightColor", alternate = ["BackgroundLightColor"]) val backgroundLightColor: String? = null,
    @SerializedName(value = "description", alternate = ["Description"]) val description: JsonElement? = null,
    @SerializedName(value = "authorId", alternate = ["AuthorId"]) val authorId: String? = null,
    @SerializedName(value = "isOfficial", alternate = ["IsOfficial"]) val isOfficial: Boolean? = null,
    @SerializedName(value = "category", alternate = ["Category"]) val category: String? = "LIGHT",
    @SerializedName(value = "isActive", alternate = ["IsActive"]) val isActive: Boolean = false,
    @SerializedName(value = "moods", alternate = ["Moods"]) val moods: List<ThemeMoodResponseDTO>? = null
) {
    fun toDomain(): Theme {
        val decorationName = id.replace("theme_", "").uppercase()
        val isOfficialTheme = isOfficial != false
        // Resolve primaryColor: prefer light, then dark
        val resolvedPrimaryColor = primaryLightColor ?: primaryDarkColor
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
                description = description?.let {
                    if (it.isJsonPrimitive) it.asString else it.toString()
                },
                backgroundLightColor = backgroundLightColor,
                backgroundDarkColor = backgroundDarkColor,
                backgroundUrl = backgroundUrl
            ),
            type = ThemeType.THEME,
            moods = moods?.map { it.toDomain() } ?: emptyList(),
            primaryColor = resolvedPrimaryColor ?: thumbnailUrl,
            primaryLightColor = primaryLightColor,
            primaryDarkColor = primaryDarkColor,
            decoration = if (isOfficialTheme) decorationName else "CUSTOM",
            category = category ?: "LIGHT",
            backgroundLightColor = backgroundLightColor,
            backgroundDarkColor = backgroundDarkColor,
            isOfficial = isOfficialTheme
        )
    }
}

data class ThemeMoodResponseDTO(
    @SerializedName(value = "baseMoodId", alternate = ["BaseMoodId"]) val baseMoodId: Int,
    @SerializedName(value = "iconColor", alternate = ["IconColor", "iconUrl", "IconUrl"]) val iconColor: String,
    @SerializedName(value = "customName", alternate = ["CustomName"]) val customName: String? = null
) {
    fun toDomain() = ThemeMood(
        baseMoodId = baseMoodId.toLong(),
        customName = customName ?: "",
        iconColor = iconColor
    )
}

private fun buildAppearanceDescription(
    description: String?,
    backgroundLightColor: String?,
    backgroundDarkColor: String?,
    backgroundUrl: String?
): String? {
    val root = description
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { JSONObject(it) }.getOrNull() }
        ?: JSONObject()
    var hasAppearance = root.length() > 0

    backgroundLightColor?.takeIf { it.isNotBlank() }?.let { color ->
        val light = root.optJSONObject("light") ?: JSONObject()
        light.applyBackgroundColorAppearance(color, backgroundUrl)
        root.put("light", light)
        hasAppearance = true
    }
    backgroundDarkColor?.takeIf { it.isNotBlank() }?.let { color ->
        val dark = root.optJSONObject("dark") ?: JSONObject()
        dark.applyBackgroundColorAppearance(color, backgroundUrl)
        root.put("dark", dark)
        hasAppearance = true
    }

    return if (hasAppearance) root.toString() else null
}

private fun JSONObject.applyBackgroundColorAppearance(color: String, backgroundUrl: String?): JSONObject {
    if (isImageBackgroundAppearance()) {
        backgroundUrl.takeIfRemoteThemeAsset()?.let { remoteUrl ->
            val existingUri = optString("backgroundUri")
            if (existingUri.isBlank() || existingUri.isLocalThemeFilePath()) {
                put("backgroundUri", remoteUrl)
            }
        }
        val fallbackColor = color.toBackgroundColorStops().firstOrNull() ?: color.trim()
        if (!has("solidBackgroundColor")) put("solidBackgroundColor", fallbackColor)
        return this
    }
    return backgroundColorAppearance(color)
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

private fun JSONObject.isImageBackgroundAppearance(): Boolean {
    val fillMode = optString("backgroundFillMode")
    if (fillMode.equals("Solid", ignoreCase = true) || fillMode.equals("Gradient", ignoreCase = true)) {
        return false
    }
    val uri = optString("backgroundUri")
    return fillMode.equals("Image", ignoreCase = true) ||
        fillMode.equals("background", ignoreCase = true) ||
        (fillMode.isBlank() && uri.isThemeAssetReference())
}

private fun String?.takeIfRemoteThemeAsset(): String? {
    val value = this?.trim()?.takeIf { it.isThemeAssetReference() } ?: return null
    return value.takeIf {
        it.startsWith("http://", ignoreCase = true) ||
            it.startsWith("https://", ignoreCase = true)
    }
}

private fun String?.isLocalThemeFilePath(): Boolean {
    val value = this?.trim() ?: return false
    return value.startsWith("/data/") ||
        value.startsWith("/storage/") ||
        value.startsWith("/sdcard/") ||
        value.startsWith("file://", ignoreCase = true)
}

private fun String?.isThemeAssetReference(): Boolean {
    val value = this?.trim() ?: return false
    if (value.isBlank() || value.equals("null", ignoreCase = true) || value.equals("pending", ignoreCase = true)) {
        return false
    }
    return !value.contains(",") && !value.isThemeColor()
}

private fun String.isThemeColor(): Boolean {
    val value = when {
        startsWith("#") -> drop(1)
        startsWith("0x", ignoreCase = true) -> drop(2)
        else -> this
    }
    return (value.length == 6 || value.length == 8) &&
        value.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
}

private fun String.toBackgroundColorStops(): List<String> {
    return trim()
        .removePrefix("gradient:")
        .removePrefix("Gradient:")
        .split(',', '|', ';')
        .map { it.trim().replace("[", "").replace("]", "").replace("\"", "").replace("'", "").trim() }
        .filter { it.isNotEmpty() }
}
