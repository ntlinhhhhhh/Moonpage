package com.diary.moonpage.domain.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ThemeDescriptionConfig(
    @SerializedName("light") val light: ThemeModeConfig? = null,
    @SerializedName("dark") val dark: ThemeModeConfig? = null
)

data class ThemeModeConfig(
    @SerializedName("backgroundScale") val backgroundScale: Float? = null,
    @SerializedName("backgroundRotation") val backgroundRotation: Float? = null,
    @SerializedName("backgroundOffsetX") val backgroundOffsetX: Float? = null,
    @SerializedName("backgroundOffsetY") val backgroundOffsetY: Float? = null,
    @SerializedName("backgroundFlipH") val backgroundFlipH: Boolean? = null,
    @SerializedName("backgroundFlipV") val backgroundFlipV: Boolean? = null,
    @SerializedName("backgroundFillMode") val backgroundFillMode: String? = null, // "Gradient" or "Solid"
    @SerializedName("solidBackgroundColor") val solidBackgroundColor: String? = null,
    @SerializedName("gradientStartColor") val gradientStartColor: String? = null,
    @SerializedName("gradientEndColor") val gradientEndColor: String? = null,
    @SerializedName("primaryColor") val primaryColor: String? = null,
    @SerializedName("iconColor") val iconColor: String? = null,
    @SerializedName("iconColors") val iconColors: List<String>? = null,
    @SerializedName("backgroundUri") val backgroundUri: String? = null
)

object ThemeDescriptionParser {
    private val gson = Gson()
    private val cache = java.util.concurrent.ConcurrentHashMap<String, ThemeDescriptionConfig>()

    fun parse(description: String?): ThemeDescriptionConfig? {
        if (description.isNullOrBlank()) return null
        val cached = cache[description]
        if (cached != null) return cached
        
        return try {
            val parsed = gson.fromJson(description, ThemeDescriptionConfig::class.java)
            if (parsed != null) {
                cache[description] = parsed
            }
            parsed
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Safely parse a hex color string to Compose Color with try-catch block.
 */
fun String?.toThemeColorOrNull(): Color? {
    if (this.isNullOrBlank()) return null
    return runCatching {
        var hex = this.trim().replace("[", "").replace("]", "").replace("\"", "").replace("'", "").trim()
        if (hex.startsWith("0x", ignoreCase = true)) hex = hex.drop(2)
        if (hex.startsWith("#")) hex = hex.drop(1)
        
        val longColor = hex.toLongOrNull(16) ?: return@runCatching null
        if (hex.length == 6) {
            Color(longColor or 0xFF000000)
        } else {
            Color(longColor)
        }
    }.getOrNull()
}

/**
 * Converts parsed ThemeDescriptionConfig to Compose Brush based on Device Dark Mode setting.
 */
fun ThemeDescriptionConfig.toBackgroundBrush(isDark: Boolean): Brush? {
    val config = if (isDark) dark ?: light else light ?: dark
    if (config == null) return null

    val fillMode = config.backgroundFillMode
    return if (fillMode?.equals("Gradient", ignoreCase = true) == true) {
        val startColor = config.gradientStartColor.toThemeColorOrNull()
        val endColor = config.gradientEndColor.toThemeColorOrNull()
        if (startColor != null && endColor != null) {
            Brush.verticalGradient(listOf(startColor, endColor))
        } else {
            null
        }
    } else {
        val solidColor = config.solidBackgroundColor.toThemeColorOrNull()
        if (solidColor != null) {
            SolidColor(solidColor)
        } else {
            null
        }
    }
}
