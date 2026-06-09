package com.diary.moonpage.domain

import com.diary.moonpage.core.util.EmailValidator
import com.diary.moonpage.core.util.resolveLogDate
import com.diary.moonpage.data.local.entity.DailyLogEntity
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeMood
import com.diary.moonpage.domain.usecase.validation.ValidateEmail
import com.diary.moonpage.domain.usecase.validation.ValidatePassword
import com.diary.moonpage.domain.usecase.validation.ValidateUsername
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainContractUnitTest {
    @Test
    fun tc01EmailValidatorRejectsInvalidFormatsAndTrimsBoundarySpaces() {
        val invalidEmails = listOf(
            "",
            "abc",
            "user@",
            "@domain.com",
            "user@.com",
            "user name@email.com",
            ".user@test.com",
            "user..name@test.com",
            "user@test",
            "user@test-.com"
        )

        invalidEmails.forEach { email ->
            assertFalse("Expected invalid email: $email", EmailValidator.isValid(email))
        }
        assertTrue(EmailValidator.isValid(" user@test.com "))
        assertTrue(EmailValidator.isValid("newuser@test.com"))
    }

    @Test
    fun tc01AuthValidationUseCasesCoverEmptyAndBoundaryValues() {
        val validateEmail = ValidateEmail()
        val validatePassword = ValidatePassword()
        val validateUsername = ValidateUsername()

        assertFalse(validateEmail.execute("").successful)
        assertFalse(validateEmail.execute("abc").successful)
        assertTrue(validateEmail.execute("user@test.com").successful)

        assertFalse(validatePassword.execute("").successful)
        assertFalse(validatePassword.execute("12345").successful)
        assertTrue(validatePassword.execute("123456").successful)

        assertFalse(validateUsername.execute("   ").successful)
        assertTrue(validateUsername.execute("Test User").successful)
    }

    @Test
    fun tc02DailyLogEntityRoundTripKeepsOptionalFields() {
        val dailyLog = DailyLog(
            id = "log-2026-06-01",
            baseMoodId = 4,
            date = "2026-06-01",
            note = "Long note",
            sleepHours = 7.5,
            sleepStartTime = "22:30",
            isMenstruation = true,
            menstruationPhase = "period",
            steps = 10000,
            musicRecord = "Song - Artist",
            musicTitle = "Song",
            artistName = "Artist",
            albumArtUrl = "https://example.com/album.png",
            dailyPhotos = listOf("photo1.jpg", "photo2.jpg"),
            activityIds = listOf("reading", "exercise"),
            createdAt = "2026-06-01T10:00:00.000Z",
            calories = 500,
            distance = 6.4,
            wakeupTime = "06:00",
            weather = "Sunny",
            temperature = 30.5
        )

        val roundTrip = DailyLogEntity.fromDomain(dailyLog).toDomain()

        assertEquals(dailyLog, roundTrip)
    }

    @Test
    fun tc02AndTc11DailyLogEntityResolvesLegacyMusicRecord() {
        val entity = DailyLogEntity(
            id = "log-legacy",
            baseMoodId = 5,
            date = "2026-06-01",
            note = null,
            sleepHours = null,
            isMenstruation = false,
            menstruationPhase = null,
            dailyPhotosJson = "",
            activityIdsJson = "",
            musicRecord = "Legacy Song - Legacy Artist"
        )

        val domain = entity.toDomain()

        assertEquals("Legacy Song", domain.musicTitle)
        assertEquals("Legacy Artist", domain.artistName)
        assertEquals("Legacy Song - Legacy Artist", domain.musicRecord)
        assertTrue(domain.dailyPhotos.orEmpty().isEmpty())
        assertTrue(domain.activityIds.orEmpty().isEmpty())
    }

    @Test
    fun tc04MomentResolveLogDatePrefersDailyLogIdAndParsesCapturedAtFallback() {
        val linkedMoment = moment(
            id = "moment-linked",
            capturedAt = "2026-06-02T23:30:00Z",
            dailyLogId = "2026-06-01_daily"
        )
        val unlinkedMoment = moment(
            id = "moment-unlinked",
            capturedAt = "2026-06-02T23:30:00Z"
        )

        assertEquals(LocalDate.parse("2026-06-01"), linkedMoment.resolveLogDate(ZoneId.of("UTC")))
        assertEquals(LocalDate.parse("2026-06-02"), unlinkedMoment.resolveLogDate(ZoneId.of("UTC")))
        assertNull(moment(id = "bad", capturedAt = "not-a-date").resolveLogDate(ZoneId.of("UTC")))
    }

    @Test
    fun tc06ThemeMoodIconsSortByMoodLevelAndFallbackToDefaultIcons() {
        val customTheme = Theme(
            id = "custom-theme",
            name = "Custom",
            collection = "Custom Theme",
            price = 0,
            thumbnailUrl = null,
            backgroundUrl = null,
            moods = listOf(
                ThemeMood(baseMoodId = 1, customName = "Very Sad", iconColor = "#111111"),
                ThemeMood(baseMoodId = 5, customName = "Very Happy", iconColor = "#555555"),
                ThemeMood(baseMoodId = 3, customName = "Neutral", iconColor = "#333333")
            )
        )
        val defaultTheme = customTheme.copy(moods = emptyList())

        assertEquals(listOf("VERY_HAPPY", "NEUTRAL", "VERY_SAD"), customTheme.icons)
        assertEquals(listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "VERY_SAD"), defaultTheme.icons)
    }

    private fun moment(
        id: String,
        capturedAt: String,
        dailyLogId: String? = null
    ): Moment {
        return Moment(
            id = id,
            imageUrl = "https://example.com/$id.jpg",
            caption = null,
            capturedAt = capturedAt,
            isPublic = true,
            dailyLogId = dailyLogId
        )
    }
}
