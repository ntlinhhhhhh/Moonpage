package com.diary.moonpage.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Theme(
    val id: String,
    val name: String,
    // Thêm các trường từ Backend Response
    val authorId: String? = null,
    val isOfficial: Boolean = false,

    val collection: String,
    val price: Int,
    val isFree: Boolean = price == 0,
    val thumbnailUrl: String?,
    val backgroundUrl: String?,
    val isOwned: Boolean = false,
    val isActive: Boolean = false,
    val description: String? = null,
    val type: ThemeType = ThemeType.THEME,

    // Đổi 'icons' thành List<ThemeMood> để hứng mảng 'Moods' từ API
    val moods: List<ThemeMood> = emptyList(),

    val primaryColor: String? = null,
    val primaryLightColor: String? = null,
    val primaryDarkColor: String? = null,
    val decoration: String = "NONE",
    val category: String = "LIGHT",
    val activatedAt: Long? = null,

    // Ánh xạ đúng tên trường Backend trả về
    val backgroundLightColor: String? = null,
    val backgroundDarkColor: String? = null
) {
    val parsedConfig: ThemeDescriptionConfig?
        get() = ThemeDescriptionParser.parse(description)

    // 1. Logic thông minh của bạn (Đã đổi tên biến để khớp JSON)
    val resolvedBgLightColor: String?
        get() = backgroundLightColor ?: description.extractColorString("light")

    val resolvedBgDarkColor: String?
        get() = backgroundDarkColor ?: description.extractColorString("dark")

    val icons: List<String>
        get() = if (moods.isNotEmpty()) {
            moods.sortedByDescending { it.baseMoodId }.map { it.customName.toEmotionString() }
        } else {
            listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "VERY_SAD")
        }
}

// Data class để hứng mảng "Moods" từ API
@Immutable
data class ThemeMood(
    val baseMoodId: Long,
    val customName: String,
    val iconColor: String // Hứng key "IconColor" từ JSON
)

// Hàm helper để convert CustomName của Backend thành enum string của bạn
private fun String.toEmotionString(): String {
    return when (this.lowercase().replace("_", " ")) {
        "very happy" -> "VERY_HAPPY"
        "happy" -> "HAPPY"
        "neutral" -> "NEUTRAL"
        "sad" -> "SAD"
        "very sad", "angry" -> "VERY_SAD"
        else -> "NEUTRAL"
    }
}

// Hàm extension parse JSON (Giữ nguyên logic cực hay của bạn)
private fun String?.extractColorString(mode: String): String? {
    if (this.isNullOrBlank()) return null
    return try {
        val root = org.json.JSONObject(this)
        val modeObj = root.optJSONObject(mode) ?: return null
        val fillMode = modeObj.optString("backgroundFillMode", "Solid")
        if (fillMode.equals("Gradient", ignoreCase = true)) {
            val start = modeObj.optString("gradientStartColor")
            val end = modeObj.optString("gradientEndColor")
            if (start.isNotBlank() && end.isNotBlank()) {
                "$start,$end"
            } else if (start.isNotBlank()) {
                start
            } else if (end.isNotBlank()) {
                end
            } else {
                null
            }
        } else if (fillMode.equals("Image", ignoreCase = true)) {
            null
        } else {
            val solid = modeObj.optString("solidBackgroundColor")
            if (solid.isNotBlank()) solid else null
        }
    } catch (e: Exception) {
        null
    }
}