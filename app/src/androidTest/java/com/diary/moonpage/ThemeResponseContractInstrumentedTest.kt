package com.diary.moonpage

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diary.moonpage.data.remote.dto.theme.ThemeMoodResponseDTO
import com.diary.moonpage.data.remote.dto.theme.ThemeResponseDTO
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class ThemeResponseContractInstrumentedTest {
    @Test
    fun tc06ThemeResponseBuildsGradientAppearanceAndMoodIcons() {
        val dto = ThemeResponseDTO(
            id = "theme_sunset",
            name = "Sunset",
            price = 100,
            thumbnailUrl = "https://example.com/thumb.webp",
            backgroundUrl = "https://example.com/bg.webp",
            primaryLightColor = "#FFAA0000",
            primaryDarkColor = "#FF330000",
            backgroundLightColor = "#FFA8C7E0,#FFE8F5E9",
            backgroundDarkColor = "#FF101010",
            description = JsonParser.parseString("""{"light":{"backgroundFillMode":"Solid"}}"""),
            isOfficial = true,
            isActive = true,
            moods = listOf(
                ThemeMoodResponseDTO(baseMoodId = 5, iconColor = "#555555", customName = "Very Happy"),
                ThemeMoodResponseDTO(baseMoodId = 1, iconColor = "#111111", customName = "Very Sad")
            )
        )

        val theme = dto.toDomain()

        assertEquals("Collection", theme.collection)
        assertFalse(theme.isFree)
        assertTrue(theme.isOfficial)
        assertTrue(theme.isActive)
        assertEquals("#FFAA0000", theme.primaryColor)
        assertEquals(listOf("VERY_HAPPY", "VERY_SAD"), theme.icons)
        assertNotNull(theme.description)
        assertTrue(theme.description!!.contains("Gradient"))
        assertTrue(theme.description!!.contains("#FFA8C7E0"))
        assertTrue(theme.description!!.contains("#FFE8F5E9"))
    }

    @Test
    fun tc06CustomThemeResponseKeepsRemoteImageWhenLocalDescriptionPointsToCache() {
        val dto = ThemeResponseDTO(
            id = "custom_1",
            name = "My Theme",
            price = 250,
            thumbnailUrl = null,
            backgroundUrl = "https://example.com/custom-bg.webp",
            backgroundLightColor = "#FFFFFFFF",
            description = JsonParser.parseString("""{"light":{"backgroundFillMode":"background","backgroundUri":"/data/user/0/com.diary.moonpage/files/missing.webp"}}"""),
            isOfficial = false,
            moods = emptyList()
        )

        val theme = dto.toDomain()

        assertEquals("Custom Theme", theme.collection)
        assertFalse(theme.isOfficial)
        assertEquals("CUSTOM", theme.decoration)
        assertEquals(
            "https://example.com/custom-bg.webp",
            JSONObject(theme.description!!).getJSONObject("light").getString("backgroundUri")
        )
    }
}
