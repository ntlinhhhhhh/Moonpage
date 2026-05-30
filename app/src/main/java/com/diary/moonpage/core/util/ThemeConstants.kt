package com.diary.moonpage.core.util

import org.json.JSONArray
import org.json.JSONObject

data class PredefinedTheme(
    val id: String,
    val name: String,
    val price: Int,
    val thumbnailUrl: String,
    val backgroundUrl: String,
    val decoration: String,
    val backgroundDarkColor: String?,
    val backgroundLightColor: String?,
    val primaryDarkColor: String?,
    val primaryLightColor: String?,
    val moods: List<PredefinedMood>
)

data class PredefinedMood(
    val baseMoodId: String,
    val iconUrl: String,
    val customName: String
)

object ThemeConstants {
    const val DEFAULT_THEME_ID = "default_theme_id"
    private const val API_DEFAULT_THEME_ID = "theme_default"

    val THEMES: List<PredefinedTheme> by lazy { parseThemes(RAW_THEMES_JSON) }

    fun normalizeThemeId(themeId: String): String {
        return if (themeId == API_DEFAULT_THEME_ID) DEFAULT_THEME_ID else themeId
    }

    fun isDefaultThemeId(themeId: String): Boolean {
        return normalizeThemeId(themeId) == DEFAULT_THEME_ID
    }

    fun findTheme(themeId: String): PredefinedTheme? {
        val normalizedId = normalizeThemeId(themeId)
        return THEMES.firstOrNull { it.id == normalizedId }
    }

    private fun parseThemes(raw: String): List<PredefinedTheme> {
        val array = JSONArray(raw)
        return List(array.length()) { index ->
            val theme = array.getJSONObject(index)
            val rawId = theme.getString("id")
            val id = normalizeThemeId(rawId)
            val moods = theme.getJSONArray("moods").toMoodList()
            PredefinedTheme(
                id = id,
                name = theme.getString("name"),
                price = theme.getInt("price"),
                thumbnailUrl = theme.getString("thumbnailUrl"),
                backgroundUrl = theme.getString("backgroundUrl"),
                decoration = id.toDecorationName(),
                backgroundDarkColor = theme.optStringOrNull("backgroundDarkColor"),
                backgroundLightColor = theme.optStringOrNull("backgroundLightColor"),
                primaryDarkColor = theme.optStringOrNull("primaryDarkColor"),
                primaryLightColor = theme.optStringOrNull("primaryLightColor"),
                moods = moods
            )
        }
    }

    private fun JSONArray.toMoodList(): List<PredefinedMood> {
        return List(length()) { index ->
            val mood = getJSONObject(index)
            PredefinedMood(
                baseMoodId = mood.optString("baseMoodId"),
                iconUrl = mood.getString("iconColor"),
                customName = mood.optString("customName")
            )
        }.sortedByDescending { it.baseMoodId.toIntOrNull() ?: Int.MIN_VALUE }
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        return optString(key).takeIf { it.isNotBlank() }
    }

    private fun String.toDecorationName(): String {
        val normalized = when {
            this == DEFAULT_THEME_ID -> "moon"
            startsWith("theme_") -> removePrefix("theme_")
            else -> this
        }
        return when (normalized.uppercase()) {
            "MOON", "DEFAULT" -> "MOON"
            "GRAY_BROWN" -> "BROWN"
            "COOKIE_BATCH" -> "COOKIE"
            "HEART_FELT" -> "HEART"
            "WEATHER_CYCLE" -> "WEATHER"
            else -> normalized.replace("-", "_").uppercase()
        }
    }
}

fun PredefinedTheme.primaryPreviewColor(): String? {
    return primaryLightColor
        ?: primaryDarkColor
        ?: thumbnailUrl.takeIfThemeColor()
        ?: moods.firstOrNull()?.iconUrl
}

fun PredefinedTheme.toAppearanceDescription(): String {
    val iconColors = moods.map { it.iconUrl }
    val root = JSONObject()
    root.put(
        "light",
        appearanceObject(
            primaryColor = primaryLightColor ?: primaryDarkColor ?: primaryPreviewColor(),
            backgroundColor = backgroundLightColor,
            iconColors = iconColors
        )
    )
    root.put(
        "dark",
        appearanceObject(
            primaryColor = primaryDarkColor ?: primaryLightColor ?: primaryPreviewColor(),
            backgroundColor = backgroundDarkColor ?: backgroundLightColor,
            iconColors = iconColors
        )
    )
    return root.toString()
}

private fun appearanceObject(
    primaryColor: String?,
    backgroundColor: String?,
    iconColors: List<String>
): JSONObject {
    return JSONObject().apply {
        put("backgroundFillMode", "Solid")
        backgroundColor?.let { put("solidBackgroundColor", it) }
        primaryColor?.let { put("primaryColor", it) }
        iconColors.firstOrNull()?.let { put("iconColor", it) }
        put("iconColors", JSONArray().apply { iconColors.forEach(::put) })
    }
}

private fun String?.takeIfThemeColor(): String? {
    if (isNullOrBlank()) return null
    val raw = trim()
    val value = when {
        raw.startsWith("#") -> raw.drop(1)
        raw.startsWith("0x", ignoreCase = true) -> raw.drop(2)
        else -> raw
    }
    return takeIf {
        (value.length == 6 || value.length == 8) &&
            value.all { char -> char in '0'..'9' || char in 'a'..'f' || char in 'A'..'F' }
    }
}

private val RAW_THEMES_JSON = """
[
  {
    "id": "theme_default",
    "name": "Moon",
    "price": 0,
    "isOfficial": true,
    "thumbnailUrl": "themes/default_thumb.png",
    "backgroundUrl": "themes/default_bg.png",
    "backgroundDarkColor": "0xFF1E1E1E",
    "backgroundLightColor": "0xFFF4F6F1",
    "primaryDarkColor": "0xFFE8D5C4",
    "primaryLightColor": "0xFF8C7E6A",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#A8730D",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#DB9D1F",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFC547",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFE18A",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFF2C2",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_blushing",
    "name": "Blushing",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/blushing_thumb.png",
    "backgroundUrl": "themes/blushing_bg.png",
    "backgroundDarkColor": "0xFF2A1F1F",
    "backgroundLightColor": "0xFFFFF0F3",
    "primaryDarkColor": "0xFFECA79D",
    "primaryLightColor": "0xFFD2847A",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#A03F38",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#C24B42",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#F07063",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FF9F98",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFC3BB",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_kitty",
    "name": "Kitty",
    "price": 200,
    "isOfficial": true,
    "thumbnailUrl": "themes/kitty_thumb.png",
    "backgroundUrl": "themes/kitty_bg.png",
    "backgroundDarkColor": "0xFF1A1C2B",
    "backgroundLightColor": "0xFFF0F3FF",
    "primaryDarkColor": "0xFFB7C2FF",
    "primaryLightColor": "0xFF8A9AFF",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#3B54BF",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#536FE6",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#7A93FF",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#B3C2FF",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#D6DFFF",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_sprout",
    "name": "Sprout",
    "price": 150,
    "isOfficial": true,
    "thumbnailUrl": "themes/sprout_thumb.png",
    "backgroundUrl": "themes/sprout_bg.png",
    "backgroundDarkColor": "0xFF1A241A",
    "backgroundLightColor": "0xFFF1F8E9",
    "primaryDarkColor": "0xFFB6E388",
    "primaryLightColor": "0xFF66BB6A",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#307A37",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#489E50",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#6EC276",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#AAE0AF",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#D4F0D6",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_midnight",
    "name": "Midnight",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/midnight_thumb.png",
    "backgroundUrl": "themes/midnight_bg.png",
    "backgroundDarkColor": "0xFF10111A",
    "backgroundLightColor": "0xFFE0E2EA",
    "primaryDarkColor": "0xFFF0E68C",
    "primaryLightColor": "0xFF1A1B26",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#806F18",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#A89532",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#D4C059",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#F5E69A",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFF7D1",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_sunny",
    "name": "Sunny",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/sunny_thumb.png",
    "backgroundUrl": "themes/sunny_bg.png",
    "backgroundDarkColor": "0xFF262014",
    "backgroundLightColor": "0xFFFFF8E1",
    "primaryDarkColor": "0xFFFFD54F",
    "primaryLightColor": "0xFFFFB300",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#A86010",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#D68322",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FAAA4B",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFCD8F",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFE6C2",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_sky",
    "name": "Sky",
    "price": 150,
    "isOfficial": true,
    "thumbnailUrl": "themes/sky_thumb.png",
    "backgroundUrl": "themes/sky_bg.png",
    "backgroundDarkColor": "0xFF162129",
    "backgroundLightColor": "0xFFE1F5FE",
    "primaryDarkColor": "0xFF81D4FA",
    "primaryLightColor": "0xFF29B6F6",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#1E82AB",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#34A6D6",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#5CCBFA",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#A3E5FF",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#D1F2FF",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_forest",
    "name": "Forest",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/forest_thumb.png",
    "backgroundUrl": "themes/forest_bg.png",
    "backgroundDarkColor": "0xFF162321",
    "backgroundLightColor": "0xFFE0F2F1",
    "primaryDarkColor": "0xFF80CBC4",
    "primaryLightColor": "0xFF26A69A",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#2B736D",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#44948D",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#6BB5AE",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#A8D9D4",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#D1EBE8",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_coffee",
    "name": "Coffee",
    "price": 120,
    "isOfficial": true,
    "thumbnailUrl": "themes/coffee_thumb.png",
    "backgroundUrl": "themes/coffee_bg.png",
    "backgroundDarkColor": "0xFF241F1D",
    "backgroundLightColor": "0xFFEFEBE9",
    "primaryDarkColor": "0xFFD7CCC8",
    "primaryLightColor": "0xFF8D6E63",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#61483D",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#826659",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#A68D81",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#D6C6BC",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#EBE2DD",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_lemon",
    "name": "Lemon",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/lemon_thumb.png",
    "backgroundUrl": "themes/lemon_bg.png",
    "backgroundDarkColor": "0xFF242618",
    "backgroundLightColor": "0xFFF9FBE7",
    "primaryDarkColor": "0xFFD4E157",
    "primaryLightColor": "0xFFCDDC39",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#7B8721",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#A1AF35",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#C8D65A",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#E8F2A0",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F4FAD2",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_cherry",
    "name": "Cherry",
    "price": 160,
    "isOfficial": true,
    "thumbnailUrl": "themes/cherry_thumb.png",
    "backgroundUrl": "themes/cherry_bg.png",
    "backgroundDarkColor": "0xFF291B1C",
    "backgroundLightColor": "0xFFFFEBEE",
    "primaryDarkColor": "0xFFEF9A9A",
    "primaryLightColor": "0xFFEF5350",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#991D29",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#C43543",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#EB606E",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFA3AC",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFD4D9",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_lavender",
    "name": "Lavender",
    "price": 80,
    "isOfficial": true,
    "thumbnailUrl": "themes/lavender_thumb.png",
    "backgroundUrl": "themes/lavender_bg.png",
    "backgroundDarkColor": "0xFF221A29",
    "backgroundLightColor": "0xFFF3E5F5",
    "primaryDarkColor": "0xFFCE93D8",
    "primaryLightColor": "0xFFAB47BC",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#702C9E",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#9147C9",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#B570EB",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#E0B8FF",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F2DFFF",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_ocean",
    "name": "Ocean",
    "price": 90,
    "isOfficial": true,
    "thumbnailUrl": "themes/ocean_thumb.png",
    "backgroundUrl": "themes/ocean_bg.png",
    "backgroundDarkColor": "0xFF161F2B",
    "backgroundLightColor": "0xFFE3F2FD",
    "primaryDarkColor": "0xFF90CAF9",
    "primaryLightColor": "0xFF42A5F5",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#26629E",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#4083C4",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#66AAEB",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#A8D3FF",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#D6EBFF",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_nebula",
    "name": "Nebula",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/nebula_thumb.png",
    "backgroundUrl": "themes/nebula_bg.png",
    "backgroundDarkColor": "0xFF201626",
    "backgroundLightColor": "0xFFF3E5F5",
    "primaryDarkColor": "0xFFBA68C8",
    "primaryLightColor": "0xFF9C27B0",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#7B1FA2",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#9C27B0",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#BA68C8",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#E1BEE7",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F3E5F5",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_matcha",
    "name": "Matcha",
    "price": 160,
    "isOfficial": true,
    "thumbnailUrl": "themes/matcha_thumb.png",
    "backgroundUrl": "themes/matcha_bg.png",
    "backgroundDarkColor": "0xFF19241A",
    "backgroundLightColor": "0xFFE8F5E9",
    "primaryDarkColor": "0xFF81C784",
    "primaryLightColor": "0xFF4CAF50",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#66BB6A",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#81C784",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#A5D6A7",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#C8E6C9",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E8F5E9",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_sunset",
    "name": "Sunset",
    "price": 110,
    "isOfficial": true,
    "thumbnailUrl": "themes/sunset_thumb.png",
    "backgroundUrl": "themes/sunset_bg.png",
    "backgroundDarkColor": "0xFF2A1C14",
    "backgroundLightColor": "0xFFFFF3E0",
    "primaryDarkColor": "0xFFFFB74D",
    "primaryLightColor": "0xFFFF9800",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#FF9800",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FFA726",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFB74D",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFE0B2",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFF3E0",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_galaxy",
    "name": "Galaxy",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/galaxy_thumb.png",
    "backgroundUrl": "themes/galaxy_bg.png",
    "backgroundDarkColor": "0xFF171826",
    "backgroundLightColor": "0xFFE8EAF6",
    "primaryDarkColor": "0xFF7986CB",
    "primaryLightColor": "0xFF3F51B5",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#5C6BC0",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#7986CB",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#9FA8DA",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#C5CAE9",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E8EAF6",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_autumn",
    "name": "Autumn",
    "price": 250,
    "isOfficial": true,
    "thumbnailUrl": "themes/autumn_thumb.png",
    "backgroundUrl": "themes/autumn_bg.png",
    "backgroundDarkColor": "0xFF2B1616",
    "backgroundLightColor": "0xFFFDF5E6",
    "primaryDarkColor": "0xFFFF8A65",
    "primaryLightColor": "0xFFD84315",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#D84315",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FF7043",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFAB91",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFCCBC",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FBE9E7",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_gray_brown",
    "name": "Gray Brown",
    "price": 160,
    "isOfficial": true,
    "thumbnailUrl": "themes/gray_brown_thumb.png",
    "backgroundUrl": "themes/gray_brown_bg.png",
    "backgroundDarkColor": "0xFF221E1C",
    "backgroundLightColor": "0xFFEFEBE9",
    "primaryDarkColor": "0xFF8D6E63",
    "primaryLightColor": "0xFF6D4C41",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#5D4037",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#8D6E63",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#BCAAA4",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#D7CCC8",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#EFEBE9",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_cookie_batch",
    "name": "Cookie Batch",
    "price": 220,
    "isOfficial": true,
    "thumbnailUrl": "themes/cookie_batch_thumb.png",
    "backgroundUrl": "themes/cookie_batch_bg.png",
    "backgroundDarkColor": "0xFF2B2113",
    "backgroundLightColor": "0xFFFFF8E1",
    "primaryDarkColor": "0xFFFFA000",
    "primaryLightColor": "0xFF8D6E63",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#8D6E63",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FFA000",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFD54F",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFECB3",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFF8E1",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_heart_felt",
    "name": "Heart Felt",
    "price": 180,
    "isOfficial": true,
    "thumbnailUrl": "themes/heart_felt_thumb.png",
    "backgroundUrl": "themes/heart_felt_bg.png",
    "backgroundDarkColor": "0xFF29151C",
    "backgroundLightColor": "0xFFFCE4EC",
    "primaryDarkColor": "0xFFE91E63",
    "primaryLightColor": "0xFFC2185B",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#AD1457",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#E91E63",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#F06292",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#F8BBD0",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FCE4EC",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_weather_cycle",
    "name": "Weather Cycle",
    "price": 140,
    "isOfficial": true,
    "thumbnailUrl": "themes/weather_cycle_thumb.png",
    "backgroundUrl": "themes/weather_cycle_bg.png",
    "backgroundDarkColor": "0xFF1C2226",
    "backgroundLightColor": "0xFFECEFF1",
    "primaryDarkColor": "0xFF607D8B",
    "primaryLightColor": "0xFF455A64",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#455A64",
        "customName": "Very Sad"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#607D8B",
        "customName": "Sad"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#90A4AE",
        "customName": "Neutral"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#CFD8DC",
        "customName": "Happy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#ECEFF1",
        "customName": "Very Happy"
      }
    ]
  },
  {
    "id": "theme_neon_cyber",
    "name": "Neon Cyber",
    "price": 250,
    "isOfficial": true,
    "thumbnailUrl": "themes/neon_cyber_thumb.png",
    "backgroundUrl": "themes/neon_cyber_bg.png",
    "backgroundDarkColor": "0xFF0A0F1A",
    "backgroundLightColor": "0xFFE0F7FA",
    "primaryDarkColor": "0xFF18FFFF",
    "primaryLightColor": "0xFF006064",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#00B8D4",
        "customName": "Glitched"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#00E5FF",
        "customName": "Laggy"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#18FFFF",
        "customName": "Online"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#84FFFF",
        "customName": "Hacked"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E0F7FA",
        "customName": "Overdrive"
      }
    ]
  },
  {
    "id": "theme_rose_gold",
    "name": "Rose Gold",
    "price": 300,
    "isOfficial": true,
    "thumbnailUrl": "themes/rose_gold_thumb.png",
    "backgroundUrl": "themes/rose_gold_bg.png",
    "backgroundDarkColor": "0xFF26191A",
    "backgroundLightColor": "0xFFFDF5F6",
    "primaryDarkColor": "0xFFF48FB1",
    "primaryLightColor": "0xFF880E4F",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#C2185B",
        "customName": "Dull"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#F06292",
        "customName": "Tarnished"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#F48FB1",
        "customName": "Polished"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#F8BBD0",
        "customName": "Shining"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FCE4EC",
        "customName": "Radiant"
      }
    ]
  },
  {
    "id": "theme_arctic_frost",
    "name": "Arctic Frost",
    "price": 120,
    "isOfficial": true,
    "thumbnailUrl": "themes/arctic_frost_thumb.png",
    "backgroundUrl": "themes/arctic_frost_bg.png",
    "backgroundDarkColor": "0xFF132224",
    "backgroundLightColor": "0xFFF0FBFC",
    "primaryDarkColor": "0xFF84FFFF",
    "primaryLightColor": "0xFF0097A7",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#0097A7",
        "customName": "Freezing"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#26C6DA",
        "customName": "Cold"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#80DEEA",
        "customName": "Crisp"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#B2EBF2",
        "customName": "Snowy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E0F7FA",
        "customName": "Aurora"
      }
    ]
  },
  {
    "id": "theme_desert_dune",
    "name": "Desert Dune",
    "price": 110,
    "isOfficial": true,
    "thumbnailUrl": "themes/desert_dune_thumb.png",
    "backgroundUrl": "themes/desert_dune_bg.png",
    "backgroundDarkColor": "0xFF261E16",
    "backgroundLightColor": "0xFFFFF9F2",
    "primaryDarkColor": "0xFFFFCC80",
    "primaryLightColor": "0xFFE65100",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#E65100",
        "customName": "Parched"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FFA726",
        "customName": "Dusty"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFCC80",
        "customName": "Warm"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFE0B2",
        "customName": "Golden"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFF3E0",
        "customName": "Oasis"
      }
    ]
  },
  {
    "id": "theme_mint_breeze",
    "name": "Mint Breeze",
    "price": 90,
    "isOfficial": true,
    "thumbnailUrl": "themes/mint_breeze_thumb.png",
    "backgroundUrl": "themes/mint_breeze_bg.png",
    "backgroundDarkColor": "0xFF142420",
    "backgroundLightColor": "0xFFF2FBF7",
    "primaryDarkColor": "0xFF80CBC4",
    "primaryLightColor": "0xFF00695C",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#00897B",
        "customName": "Stale"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#26A69A",
        "customName": "Mild"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#80CBC4",
        "customName": "Cool"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#B2DFDB",
        "customName": "Refreshing"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E0F2F1",
        "customName": "Breezy"
      }
    ]
  },
  {
    "id": "theme_royal_velvet",
    "name": "Royal Velvet",
    "price": 280,
    "isOfficial": true,
    "thumbnailUrl": "themes/royal_velvet_thumb.png",
    "backgroundUrl": "themes/royal_velvet_bg.png",
    "backgroundDarkColor": "0xFF1B1424",
    "backgroundLightColor": "0xFFF8F5FA",
    "primaryDarkColor": "0xFFD1C4E9",
    "primaryLightColor": "0xFF4527A0",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#4527A0",
        "customName": "Faded"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#7E57C2",
        "customName": "Subtle"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#B39DDB",
        "customName": "Elegant"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#D1C4E9",
        "customName": "Majestic"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#EDE7F6",
        "customName": "Royal"
      }
    ]
  },
  {
    "id": "theme_matcha_latte",
    "name": "Matcha Latte",
    "price": 140,
    "isOfficial": true,
    "thumbnailUrl": "themes/matcha_latte_thumb.png",
    "backgroundUrl": "themes/matcha_latte_bg.png",
    "backgroundDarkColor": "0xFF23261D",
    "backgroundLightColor": "0xFFF6F8F3",
    "primaryDarkColor": "0xFFC5E1A5",
    "primaryLightColor": "0xFF689F38",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#689F38",
        "customName": "Spilled"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#9CCC65",
        "customName": "Bitter"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#C5E1A5",
        "customName": "Steamed"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#DCEDC8",
        "customName": "Sweet"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F1F8E9",
        "customName": "Delicious"
      }
    ]
  },
  {
    "id": "theme_copper_rust",
    "name": "Copper Rust",
    "price": 160,
    "isOfficial": true,
    "thumbnailUrl": "themes/copper_rust_thumb.png",
    "backgroundUrl": "themes/copper_rust_bg.png",
    "backgroundDarkColor": "0xFF261916",
    "backgroundLightColor": "0xFFFBF4F2",
    "primaryDarkColor": "0xFFFFAB91",
    "primaryLightColor": "0xFFD84315",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#D84315",
        "customName": "Rusted"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FF7043",
        "customName": "Weathered"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFAB91",
        "customName": "Solid"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFCCBC",
        "customName": "Polished"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FBE9E7",
        "customName": "Shining"
      }
    ]
  },
  {
    "id": "theme_starlight",
    "name": "Starlight",
    "price": 200,
    "isOfficial": true,
    "thumbnailUrl": "themes/starlight_thumb.png",
    "backgroundUrl": "themes/starlight_bg.png",
    "backgroundDarkColor": "0xFF10121A",
    "backgroundLightColor": "0xFFF2F4F8",
    "primaryDarkColor": "0xFF9FA8DA",
    "primaryLightColor": "0xFF283593",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#283593",
        "customName": "Dim"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#5C6BC0",
        "customName": "Faint"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#9FA8DA",
        "customName": "Twinkling"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#C5CAE9",
        "customName": "Glowing"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E8EAF6",
        "customName": "Supernova"
      }
    ]
  },
  {
    "id": "theme_peach_sorbet",
    "name": "Peach Sorbet",
    "price": 130,
    "isOfficial": true,
    "thumbnailUrl": "themes/peach_sorbet_thumb.png",
    "backgroundUrl": "themes/peach_sorbet_bg.png",
    "backgroundDarkColor": "0xFF2A1C1A",
    "backgroundLightColor": "0xFFFFF5F2",
    "primaryDarkColor": "0xFFFFAB91",
    "primaryLightColor": "0xFFE64A19",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#E64A19",
        "customName": "Sour"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FF7043",
        "customName": "Bland"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFAB91",
        "customName": "Sweet"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFCCBC",
        "customName": "Juicy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FBE9E7",
        "customName": "Perfect"
      }
    ]
  },
  {
    "id": "theme_sapphire_depths",
    "name": "Sapphire Depths",
    "price": 220,
    "isOfficial": true,
    "thumbnailUrl": "themes/sapphire_depths_thumb.png",
    "backgroundUrl": "themes/sapphire_depths_bg.png",
    "backgroundDarkColor": "0xFF111724",
    "backgroundLightColor": "0xFFF3F6FA",
    "primaryDarkColor": "0xFF90CAF9",
    "primaryLightColor": "0xFF1565C0",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#1565C0",
        "customName": "Murky"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#42A5F5",
        "customName": "Shallow"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#90CAF9",
        "customName": "Deep"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#BBDEFB",
        "customName": "Crystal"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E3F2FD",
        "customName": "Abyssal"
      }
    ]
  },
  {
    "id": "theme_lemonade",
    "name": "Lemonade",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/lemonade_thumb.png",
    "backgroundUrl": "themes/lemonade_bg.png",
    "backgroundDarkColor": "0xFF242416",
    "backgroundLightColor": "0xFFFDFDF0",
    "primaryDarkColor": "0xFFFFF59D",
    "primaryLightColor": "0xFFF57F17",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#FBC02D",
        "customName": "Watery"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FFEB3B",
        "customName": "Tart"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFF176",
        "customName": "Sweet"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFF59D",
        "customName": "Zesty"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFFDE7",
        "customName": "Refreshing"
      }
    ]
  },
  {
    "id": "theme_crimson_tide",
    "name": "Crimson Tide",
    "price": 190,
    "isOfficial": true,
    "thumbnailUrl": "themes/crimson_tide_thumb.png",
    "backgroundUrl": "themes/crimson_tide_bg.png",
    "backgroundDarkColor": "0xFF2E1515",
    "backgroundLightColor": "0xFFFFF3F3",
    "primaryDarkColor": "0xFFFF8A80",
    "primaryLightColor": "0xFFD50000",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#D50000",
        "customName": "Low Tide"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FF5252",
        "customName": "Ebbing"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FF8A80",
        "customName": "Flowing"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFCDD2",
        "customName": "Surging"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFEBEE",
        "customName": "High Tide"
      }
    ]
  },
  {
    "id": "theme_lofi_study",
    "name": "Lofi Study",
    "price": 170,
    "isOfficial": true,
    "thumbnailUrl": "themes/lofi_study_thumb.png",
    "backgroundUrl": "themes/lofi_study_bg.png",
    "backgroundDarkColor": "0xFF1F1C1B",
    "backgroundLightColor": "0xFFFAF8F5",
    "primaryDarkColor": "0xFFD7CCC8",
    "primaryLightColor": "0xFF4E342E",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#4E342E",
        "customName": "Burnout"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#8D6E63",
        "customName": "Distracted"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#BCAAA4",
        "customName": "Chilling"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#D7CCC8",
        "customName": "Focused"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#EFEBE9",
        "customName": "Flow State"
      }
    ]
  },
  {
    "id": "theme_emerald_city",
    "name": "Emerald City",
    "price": 240,
    "isOfficial": true,
    "thumbnailUrl": "themes/emerald_city_thumb.png",
    "backgroundUrl": "themes/emerald_city_bg.png",
    "backgroundDarkColor": "0xFF13261C",
    "backgroundLightColor": "0xFFF2FCF6",
    "primaryDarkColor": "0xFF69F0AE",
    "primaryLightColor": "0xFF00C853",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#00C853",
        "customName": "Foggy"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#00E676",
        "customName": "Overcast"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#69F0AE",
        "customName": "Clear"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#B9F6CA",
        "customName": "Bright"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E8F5E9",
        "customName": "Dazzling"
      }
    ]
  },
  {
    "id": "theme_amber_glow",
    "name": "Amber Glow",
    "price": 150,
    "isOfficial": true,
    "thumbnailUrl": "themes/amber_glow_thumb.png",
    "backgroundUrl": "themes/amber_glow_bg.png",
    "backgroundDarkColor": "0xFF261D12",
    "backgroundLightColor": "0xFFFDF8F0",
    "primaryDarkColor": "0xFFFFCC80",
    "primaryLightColor": "0xFFE65100",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#FF8F00",
        "customName": "Dim"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FFB300",
        "customName": "Flickering"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFE082",
        "customName": "Warm"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFECB3",
        "customName": "Radiant"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFF8E1",
        "customName": "Blazing"
      }
    ]
  },
  {
    "id": "theme_orchid_garden",
    "name": "Orchid Garden",
    "price": 210,
    "isOfficial": true,
    "thumbnailUrl": "themes/orchid_garden_thumb.png",
    "backgroundUrl": "themes/orchid_garden_bg.png",
    "backgroundDarkColor": "0xFF2A1B2E",
    "backgroundLightColor": "0xFFFCF4FF",
    "primaryDarkColor": "0xFFEA80FC",
    "primaryLightColor": "0xFFAA00FF",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#6A1B9A",
        "customName": "Drooping"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#AA00FF",
        "customName": "Budding"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#D500F9",
        "customName": "Blooming"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#E1BEE7",
        "customName": "Vibrant"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F3E5F5",
        "customName": "Flawless"
      }
    ]
  },
  {
    "id": "theme_winter_woods",
    "name": "Winter Woods",
    "price": 120,
    "isOfficial": true,
    "thumbnailUrl": "themes/winter_woods_thumb.png",
    "backgroundUrl": "themes/winter_woods_bg.png",
    "backgroundDarkColor": "0xFF1A1F21",
    "backgroundLightColor": "0xFFF4F6F7",
    "primaryDarkColor": "0xFFB0BEC5",
    "primaryLightColor": "0xFF455A64",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#455A64",
        "customName": "Bare"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#78909C",
        "customName": "Frosty"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#B0BEC5",
        "customName": "Quiet"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#CFD8DC",
        "customName": "Snowy"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#ECEFF1",
        "customName": "Magical"
      }
    ]
  },
  {
    "id": "theme_tropical_paradise",
    "name": "Tropical Paradise",
    "price": 230,
    "isOfficial": true,
    "thumbnailUrl": "themes/tropical_paradise_thumb.png",
    "backgroundUrl": "themes/tropical_paradise_bg.png",
    "backgroundDarkColor": "0xFF122421",
    "backgroundLightColor": "0xFFF0F9F6",
    "primaryDarkColor": "0xFF80CBC4",
    "primaryLightColor": "0xFF00695C",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#00796B",
        "customName": "Overcast"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#26A69A",
        "customName": "Humid"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#80CBC4",
        "customName": "Breezy"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#B2DFDB",
        "customName": "Sunny"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E0F2F1",
        "customName": "Paradise"
      }
    ]
  },
  {
    "id": "theme_vanilla_bean",
    "name": "Vanilla Bean",
    "price": 100,
    "isOfficial": true,
    "thumbnailUrl": "themes/vanilla_bean_thumb.png",
    "backgroundUrl": "themes/vanilla_bean_bg.png",
    "backgroundDarkColor": "0xFF24221E",
    "backgroundLightColor": "0xFFFCFAEE",
    "primaryDarkColor": "0xFFFFF59D",
    "primaryLightColor": "0xFFF57F17",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#F57F17",
        "customName": "Bland"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FBC02D",
        "customName": "Subtle"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFF59D",
        "customName": "Sweet"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFF9C4",
        "customName": "Rich"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFFDE7",
        "customName": "Decadent"
      }
    ]
  },
  {
    "id": "theme_steel_city",
    "name": "Steel City",
    "price": 150,
    "isOfficial": true,
    "thumbnailUrl": "themes/steel_city_thumb.png",
    "backgroundUrl": "themes/steel_city_bg.png",
    "backgroundDarkColor": "0xFF1A1C1E",
    "backgroundLightColor": "0xFFF0F2F4",
    "primaryDarkColor": "0xFF90A4AE",
    "primaryLightColor": "0xFF37474F",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#37474F",
        "customName": "Smoggy"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#607D8B",
        "customName": "Dull"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#90A4AE",
        "customName": "Bustling"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#CFD8DC",
        "customName": "Vibrant"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#ECEFF1",
        "customName": "Electric"
      }
    ]
  },
  {
    "id": "theme_berry_smoothie",
    "name": "Berry Smoothie",
    "price": 180,
    "isOfficial": true,
    "thumbnailUrl": "themes/berry_smoothie_thumb.png",
    "backgroundUrl": "themes/berry_smoothie_bg.png",
    "backgroundDarkColor": "0xFF221626",
    "backgroundLightColor": "0xFFF7F0FA",
    "primaryDarkColor": "0xFFCE93D8",
    "primaryLightColor": "0xFF6A1B9A",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#7B1FA2",
        "customName": "Watery"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#AB47BC",
        "customName": "Tart"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#CE93D8",
        "customName": "Sweet"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#E1BEE7",
        "customName": "Fruity"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F3E5F5",
        "customName": "Delicious"
      }
    ]
  },
  {
    "id": "theme_olive_grove",
    "name": "Olive Grove",
    "price": 140,
    "isOfficial": true,
    "thumbnailUrl": "themes/olive_grove_thumb.png",
    "backgroundUrl": "themes/olive_grove_bg.png",
    "backgroundDarkColor": "0xFF1F2418",
    "backgroundLightColor": "0xFFF7FBEB",
    "primaryDarkColor": "0xFFC5E1A5",
    "primaryLightColor": "0xFF558B2F",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#558B2F",
        "customName": "Dry"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#9CCC65",
        "customName": "Earthy"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#C5E1A5",
        "customName": "Fresh"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#DCEDC8",
        "customName": "Lush"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F1F8E9",
        "customName": "Bountiful"
      }
    ]
  },
  {
    "id": "theme_ruby_red",
    "name": "Ruby Red",
    "price": 270,
    "isOfficial": true,
    "thumbnailUrl": "themes/ruby_red_thumb.png",
    "backgroundUrl": "themes/ruby_red_bg.png",
    "backgroundDarkColor": "0xFF261214",
    "backgroundLightColor": "0xFFFFF0F2",
    "primaryDarkColor": "0xFFEF9A9A",
    "primaryLightColor": "0xFFB71C1C",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#C62828",
        "customName": "Faded"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#E53935",
        "customName": "Uncut"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#EF9A9A",
        "customName": "Polished"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFCDD2",
        "customName": "Gleaming"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFEBEE",
        "customName": "Flawless"
      }
    ]
  },
  {
    "id": "theme_pearl_white",
    "name": "Pearl White",
    "price": 200,
    "isOfficial": true,
    "thumbnailUrl": "themes/pearl_white_thumb.png",
    "backgroundUrl": "themes/pearl_white_bg.png",
    "backgroundDarkColor": "0xFF222426",
    "backgroundLightColor": "0xFFFFFFFF",
    "primaryDarkColor": "0xFFCFD8DC",
    "primaryLightColor": "0xFF78909C",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#607D8B",
        "customName": "Clouded"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#90A4AE",
        "customName": "Opaque"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#B0BEC5",
        "customName": "Smooth"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#CFD8DC",
        "customName": "Lustrous"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#ECEFF1",
        "customName": "Iridescent"
      }
    ]
  },
  {
    "id": "theme_honey_comb",
    "name": "Honey Comb",
    "price": 160,
    "isOfficial": true,
    "thumbnailUrl": "themes/honey_comb_thumb.png",
    "backgroundUrl": "themes/honey_comb_bg.png",
    "backgroundDarkColor": "0xFF261D0F",
    "backgroundLightColor": "0xFFFEF8ED",
    "primaryDarkColor": "0xFFFFCC80",
    "primaryLightColor": "0xFFEF6C00",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#E65100",
        "customName": "Empty"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FFA726",
        "customName": "Sticky"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFCC80",
        "customName": "Sweet"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFE0B2",
        "customName": "Golden"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFF3E0",
        "customName": "Nectar"
      }
    ]
  },
  {
    "id": "theme_dusty_blue",
    "name": "Dusty Blue",
    "price": 110,
    "isOfficial": true,
    "thumbnailUrl": "themes/dusty_blue_thumb.png",
    "backgroundUrl": "themes/dusty_blue_bg.png",
    "backgroundDarkColor": "0xFF161B21",
    "backgroundLightColor": "0xFFF0F4F8",
    "primaryDarkColor": "0xFF9FA8DA",
    "primaryLightColor": "0xFF3949AB",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#303F9F",
        "customName": "Muted"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#5C6BC0",
        "customName": "Faded"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#9FA8DA",
        "customName": "Calm"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#C5CAE9",
        "customName": "Clear"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#E8EAF6",
        "customName": "Vivid"
      }
    ]
  },
  {
    "id": "theme_sunflower_field",
    "name": "Sunflower Field",
    "price": 190,
    "isOfficial": true,
    "thumbnailUrl": "themes/sunflower_field_thumb.png",
    "backgroundUrl": "themes/sunflower_field_bg.png",
    "backgroundDarkColor": "0xFF262111",
    "backgroundLightColor": "0xFFFDFBF2",
    "primaryDarkColor": "0xFFFFF59D",
    "primaryLightColor": "0xFFF57F17",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#F57F17",
        "customName": "Drooping"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#FBC02D",
        "customName": "Shaded"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#FFEB3B",
        "customName": "Upright"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#FFF59D",
        "customName": "Sun-kissed"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#FFFDE7",
        "customName": "Blazing"
      }
    ]
  },
  {
    "id": "theme_obsidian",
    "name": "Obsidian",
    "price": 300,
    "isOfficial": true,
    "thumbnailUrl": "themes/obsidian_thumb.png",
    "backgroundUrl": "themes/obsidian_bg.png",
    "backgroundDarkColor": "0xFF0A0A0A",
    "backgroundLightColor": "0xFFE8E8E8",
    "primaryDarkColor": "0xFF9E9E9E",
    "primaryLightColor": "0xFF212121",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#212121",
        "customName": "Dusty"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#616161",
        "customName": "Dull"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#9E9E9E",
        "customName": "Solid"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#E0E0E0",
        "customName": "Sharp"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F5F5F5",
        "customName": "Glassy"
      }
    ]
  },
  {
    "id": "theme_spring_morning",
    "name": "Spring Morning",
    "price": 130,
    "isOfficial": true,
    "thumbnailUrl": "themes/spring_morning_thumb.png",
    "backgroundUrl": "themes/spring_morning_bg.png",
    "backgroundDarkColor": "0xFF232617",
    "backgroundLightColor": "0xFFFAFCF2",
    "primaryDarkColor": "0xFFEEFF41",
    "primaryLightColor": "0xFFAEEA00",
    "moods": [
      {
        "baseMoodId": 1,
        "iconColor": "#AEEA00",
        "customName": "Chilly"
      },
      {
        "baseMoodId": 2,
        "iconColor": "#C6FF00",
        "customName": "Dewy"
      },
      {
        "baseMoodId": 3,
        "iconColor": "#EEFF41",
        "customName": "Fresh"
      },
      {
        "baseMoodId": 4,
        "iconColor": "#F4FF81",
        "customName": "Sunny"
      },
      {
        "baseMoodId": 5,
        "iconColor": "#F9FBE7",
        "customName": "Vibrant"
      }
    ]
  }
]
""".trimIndent()
