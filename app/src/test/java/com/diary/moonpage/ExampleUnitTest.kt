package com.diary.moonpage

import androidx.compose.ui.graphics.SolidColor
import com.diary.moonpage.core.theme.previewBackgroundBrush
import com.diary.moonpage.core.theme.previewBackgroundImagePath
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeDescriptionParser
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    init {
        com.diary.moonpage.core.theme.isThemeTestingMode = true
    }
    @Test
    fun testJsonParsing() {
        val json = """{"light":{"backgroundScale":1,"backgroundRotation":0,"backgroundOffsetX":0,"backgroundOffsetY":0,"backgroundFillMode":"Gradient","solidBackgroundColor":"#FFFFF7EC","gradientStartColor":"#FFA8C7E0","gradientEndColor":"#FFE8F5E9","primaryColor":"#FFE57373","iconColor":"#FFE05454","iconColors":["#FFF6C0C0","#FFF2A8A8","#FFF68585","#FFEB7070","#FFE05454"]},"dark":{"backgroundScale":1,"backgroundRotation":0,"backgroundOffsetX":0,"backgroundOffsetY":0,"backgroundFillMode":"Solid","solidBackgroundColor":"#FF1C1C1C","gradientStartColor":"#FF232323","gradientEndColor":"#FF393939","primaryColor":"#FFFFF9EF","iconColor":"#FFDB5353","iconColors":["#FFEEB6B6","#FFECA1A1","#FFEE8686","#FFEF6D6D","#FFDB5353"]}}"""
        val config = ThemeDescriptionParser.parse(json)
        assertNotNull(config)
        assertEquals("Gradient", config?.light?.backgroundFillMode)
        assertEquals("#FFA8C7E0", config?.light?.gradientStartColor)
    }

    @Test
    fun testFillModeRendering_GradientLight() {
        val json = """{"light":{"backgroundFillMode":"Gradient","gradientStartColor":"#FFA8C7E0","gradientEndColor":"#FFE8F5E9"},"dark":{"backgroundFillMode":"Solid","solidBackgroundColor":"#FF1C1C1C"}}"""
        val theme = Theme(
            id = "custom_123",
            name = "Test Gradient",
            collection = "Custom Theme",
            price = 0,
            thumbnailUrl = null,
            backgroundUrl = "pending",
            description = json
        )

        // Gradient Mode should return null for image path and a gradient brush for brush
        val path = theme.previewBackgroundImagePath(isDark = false)
        assertNull(path)

        val brush = theme.previewBackgroundBrush(isDark = false)
        assertNotNull(brush)
        // Since it's a gradient brush, it's not a SolidColor
        assertFalse(brush is SolidColor)
    }

    @Test
    fun testFillModeRendering_SolidDark() {
        val json = """{"light":{"backgroundFillMode":"Gradient","gradientStartColor":"#FFA8C7E0","gradientEndColor":"#FFE8F5E9"},"dark":{"backgroundFillMode":"Solid","solidBackgroundColor":"#FF1C1C1C"}}"""
        val theme = Theme(
            id = "custom_123",
            name = "Test Solid",
            collection = "Custom Theme",
            price = 0,
            thumbnailUrl = null,
            backgroundUrl = "pending",
            description = json
        )

        // Solid Mode should return null for image path and a SolidColor brush for brush
        val path = theme.previewBackgroundImagePath(isDark = true)
        assertNull(path)

        val brush = theme.previewBackgroundBrush(isDark = true)
        assertNotNull(brush)
        assertTrue(brush is SolidColor)
    }

    @Test
    fun testFillModeRendering_Image() {
        val json = """{"light":{"backgroundFillMode":"Image","backgroundUri":"themes/my_bg.png"},"dark":{"backgroundFillMode":"Solid","solidBackgroundColor":"#FF1C1C1C"}}"""
        val theme = Theme(
            id = "custom_123",
            name = "Test Image",
            collection = "Custom Theme",
            price = 0,
            thumbnailUrl = null,
            backgroundUrl = "themes/my_bg.png",
            description = json
        )

        // Image Mode should return the image path and null for brush
        val path = theme.previewBackgroundImagePath(isDark = false)
        assertEquals("themes/my_bg.png", path)

        val brush = theme.previewBackgroundBrush(isDark = false)
        assertNull(brush)
    }

    @Test
    fun testFillModeRendering_ImageLocalFilePath() {
        val json = """{"light":{"backgroundFillMode":"background","backgroundUri":"/data/user/0/com.diary.moonpage/files/custom_themes/custom_theme_bg_1780194365568.webp"},"dark":{"backgroundFillMode":"Solid","solidBackgroundColor":"#FF1C1C1C"}}"""
        val theme = Theme(
            id = "custom_123",
            name = "Test Local Webp",
            collection = "Custom Theme",
            price = 0,
            thumbnailUrl = null,
            backgroundUrl = "/data/user/0/com.diary.moonpage/files/custom_themes/custom_theme_bg_1780194365568.webp",
            description = json
        )

        // It should identify it as Image/background mode, return the backgroundUrl and null for brush
        val path = theme.previewBackgroundImagePath(isDark = false)
        assertEquals("/data/user/0/com.diary.moonpage/files/custom_themes/custom_theme_bg_1780194365568.webp", path)

        val brush = theme.previewBackgroundBrush(isDark = false)
        assertNull(brush)
    }

    @Test
    fun testFallbackWhenLocalFileDoesNotExist() {
        // Temporarily disable testing mode so file existence is actually checked
        com.diary.moonpage.core.theme.isThemeTestingMode = false
        try {
            val json = """{"light":{"backgroundFillMode":"Image","backgroundUri":"/data/user/0/non_existent_path/bg.webp"},"dark":{"backgroundFillMode":"Solid","solidBackgroundColor":"#FF1C1C1C"}}"""
            val theme = Theme(
                id = "custom_123",
                name = "Test Fallback",
                collection = "Custom Theme",
                price = 0,
                thumbnailUrl = "https://example.com/thumbnail.png",
                backgroundUrl = "https://example.com/background.png",
                description = json
            )

            // When backgroundUrl is remote, it should return that remote backgroundUrl
            val path = theme.previewBackgroundImagePath(isDark = false)
            assertEquals("https://example.com/background.png", path)

            // When backgroundUrl is also local (and thus doesn't exist on disk), it should fall back to remote thumbnailUrl
            val themeWithBothLocal = Theme(
                id = "custom_124",
                name = "Test Local Both",
                collection = "Custom Theme",
                price = 0,
                thumbnailUrl = "https://example.com/thumbnail.png",
                backgroundUrl = "/data/user/0/non_existent_path/bg_url.webp",
                description = json
            )

            val fallbackPath = themeWithBothLocal.previewBackgroundImagePath(isDark = false)
            assertEquals("https://example.com/thumbnail.png", fallbackPath)
        } finally {
            com.diary.moonpage.core.theme.isThemeTestingMode = true
        }
    }

    @Test
    fun testBackgroundModeUsesRemoteBackgroundWhenLocalCacheIsGone() {
        com.diary.moonpage.core.theme.isThemeTestingMode = false
        try {
            val remoteBackground = "https://example.com/custom_background.webp"
            val json = """{"light":{"backgroundFillMode":"background","backgroundUri":"/data/user/0/com.diary.moonpage/files/custom_themes/missing.webp","solidBackgroundColor":"#FFFFF7EC"},"dark":{"backgroundFillMode":"background","backgroundUri":"/data/user/0/com.diary.moonpage/files/custom_themes/missing.webp","solidBackgroundColor":"#FF1C1C1C"}}"""
            val theme = Theme(
                id = "custom_background_cache_clear",
                name = "Background Cache Clear",
                collection = "Custom Theme",
                price = 0,
                thumbnailUrl = "https://example.com/thumbnail.webp",
                backgroundUrl = remoteBackground,
                description = json,
                isOfficial = false
            )

            assertEquals(remoteBackground, theme.previewBackgroundImagePath(isDark = false))
            assertNull(theme.previewBackgroundBrush(isDark = false))
        } finally {
            com.diary.moonpage.core.theme.isThemeTestingMode = true
        }
    }

    @Test
    fun testCustomThemeApiPreviewUsesRemoteBackgroundAfterCacheClear() {
        val remoteBackground = "https://firebasestorage.googleapis.com/v0/b/moodyfy-3f2dd.firebasestorage.app/o/diary_app%2Fthemes%2Fcustom_theme_bg.webp?alt=media"
        val description = """
            {
              "light": {
                "backgroundUri": "/data/user/0/com.diary.moonpage/files/custom_themes/custom_theme_bg_1780213468797.webp",
                "backgroundFillMode": "background",
                "solidBackgroundColor": "#FFFFF7EC",
                "primaryColor": "#FF8D6E63",
                "iconColors": ["#FFFFCA28", "#FF81C784", "#FF64B5F6", "#FFBA68C8", "#FF8D6E63"]
              },
              "dark": {
                "backgroundUri": "/data/user/0/com.diary.moonpage/files/custom_themes/custom_theme_bg_1780213468797.webp",
                "backgroundFillMode": "background",
                "solidBackgroundColor": "#FF1C1C1C",
                "primaryColor": "#FFFFF9EF",
                "iconColors": ["#FFFFCA28", "#FF81C784", "#FF64B5F6", "#FFBA68C8", "#FF8D6E63"]
              }
            }
        """.trimIndent()
        val theme = Theme(
            id = "custom_4425d578-173c-4f6f-b515-534811867976_1780213468797",
            name = "My Custom Theme",
            collection = "Custom Theme",
            price = 250,
            thumbnailUrl = "https://example.com/theme_thumb.webp",
            backgroundUrl = remoteBackground,
            primaryLightColor = "#FF8D6E63",
            primaryDarkColor = "#FFFFF9EF",
            backgroundDarkColor = "0xFFF4F6F1",
            backgroundLightColor = "0xFF1C1C1C",
            description = description,
            isOfficial = false,
            category = "LIGHT",
            isActive = false
        )

        assertEquals(remoteBackground, theme.previewBackgroundImagePath(isDark = false))
        assertNull(theme.previewBackgroundBrush(isDark = false))
    }

    @Test
    fun testRemoteBackgroundUrlDoesNotOverrideSolidAppearanceForCustomPreview() {
        val remoteBackground = "https://example.com/custom_background.webp"
        val theme = Theme(
            id = "custom_123",
            name = "Remote Image",
            collection = "Custom Theme",
            price = 0,
            thumbnailUrl = null,
            backgroundUrl = remoteBackground,
            backgroundLightColor = "0xFF1C1C1C",
            description = """{"light":{"backgroundFillMode":"Solid","solidBackgroundColor":"#FF1C1C1C"}}""",
            isOfficial = false
        )

        assertNull(theme.previewBackgroundImagePath(isDark = false))
        assertTrue(theme.previewBackgroundBrush(isDark = false) is SolidColor)
    }

    @Test
    fun testRemoteBackgroundUrlDoesNotOverrideGradientAppearanceForCustomPreview() {
        val remoteBackground = "https://example.com/custom_background.webp"
        val theme = Theme(
            id = "custom_124",
            name = "Remote Image With Gradient",
            collection = "Custom Theme",
            price = 0,
            thumbnailUrl = null,
            backgroundUrl = remoteBackground,
            backgroundLightColor = "#FFA8C7E0,#FFE8F5E9",
            description = """{"light":{"backgroundFillMode":"Gradient","gradientStartColor":"#FFA8C7E0","gradientEndColor":"#FFE8F5E9"}}""",
            isOfficial = false
        )

        assertNull(theme.previewBackgroundImagePath(isDark = false))
        val brush = theme.previewBackgroundBrush(isDark = false)
        assertNotNull(brush)
        assertFalse(brush is SolidColor)
    }
}
