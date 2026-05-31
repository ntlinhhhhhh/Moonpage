package com.diary.moonpage

import androidx.compose.ui.graphics.SolidColor
import com.diary.moonpage.core.theme.previewBackgroundBrush
import com.diary.moonpage.core.theme.previewBackgroundImagePath
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeDescriptionParser
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
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
}