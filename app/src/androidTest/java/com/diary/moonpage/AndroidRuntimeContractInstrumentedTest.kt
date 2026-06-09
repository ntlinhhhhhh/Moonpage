package com.diary.moonpage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diary.moonpage.core.util.PkceUtil
import com.diary.moonpage.core.util.ThemeConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class AndroidRuntimeContractInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun tc09WidgetXmlResourcesExistForAllWidgetTypes() {
        val widgetXmlIds = listOf(
            R.xml.quick_mood_widget_info,
            R.xml.weekly_mood_widget_info,
            R.xml.monthly_mood_widget_info,
            R.xml.daily_summary_widget_info,
            R.xml.photo_moment_widget_info
        )

        widgetXmlIds.forEach { xmlId ->
            assertEquals("appwidget-provider", rootTagName(xmlId))
        }
    }

    @Test
    fun tc11SpotifyPkceVerifierAndChallengeUseUrlSafeBase64() {
        val verifier = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.generateCodeChallenge(verifier)
        val challengeAgain = PkceUtil.generateCodeChallenge(verifier)

        assertTrue(verifier.matches(Regex("[A-Za-z0-9_-]+")))
        assertTrue(challenge.matches(Regex("[A-Za-z0-9_-]+")))
        assertEquals(challenge, challengeAgain)
        assertFalse(challenge.contains("="))
        assertFalse(challenge.contains("+"))
        assertFalse(challenge.contains("/"))
        assertFalse(verifier == challenge)
    }

    @Test
    fun tc12AndTc13BaseResourcesAreAvailableAtRuntime() {
        assertEquals("com.diary.moonpage", context.packageName)
        assertTrue(context.getString(R.string.app_name).isNotBlank())
        assertEquals("locale-config", rootTagName(R.xml.locales_config))
    }

    @Test
    fun tc03AndTc06ThemeCatalogNormalizesDefaultThemeAndProvidesFiveMoods() {
        val defaultTheme = ThemeConstants.findTheme("theme_default")

        assertNotNull(defaultTheme)
        assertEquals(ThemeConstants.DEFAULT_THEME_ID, ThemeConstants.normalizeThemeId("theme_default"))
        assertTrue(ThemeConstants.isDefaultThemeId("theme_default"))
        assertEquals(0, defaultTheme!!.price)
        assertEquals(5, defaultTheme.moods.size)
        assertEquals(listOf("5", "4", "3", "2", "1"), defaultTheme.moods.map { it.baseMoodId })
        assertTrue(ThemeConstants.THEMES.all { it.moods.size == 5 })
    }

    private fun rootTagName(xmlId: Int): String {
        val parser = context.resources.getXml(xmlId)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT) {
                eventType = parser.next()
            }
            return parser.name
        } finally {
            parser.close()
        }
    }
}
