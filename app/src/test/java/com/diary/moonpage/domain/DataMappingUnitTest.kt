package com.diary.moonpage.domain

import com.diary.moonpage.data.local.Converters
import com.diary.moonpage.data.local.entity.ThemeEntity
import com.diary.moonpage.data.remote.dto.auth.UserResponseDto
import com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeMood
import com.diary.moonpage.domain.model.ThemeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataMappingUnitTest {
    @Test
    fun userResponseDtoMapsDefaultsAndNormalizesAvatarUrl() {
        val dto = UserResponseDto(
            id = "user-1",
            name = "Linh",
            email = "linh@example.com",
            avatarUrl = "avatars/linh.png",
            gender = null,
            birthday = null,
            coinBalance = null,
            authProvider = null,
            streakFreezeCount = null,
            currentStreak = null,
            longestStreak = null
        )

        val domain = dto.toDomain(token = "jwt-token")

        assertEquals("jwt-token", domain.token)
        assertEquals("user-1", domain.userId)
        assertEquals("https://hieu-wikipedia.io.vn/avatars/linh.png", domain.avatarUrl)
        assertEquals(0, domain.coinBalance)
        assertEquals("Password", domain.authProvider)
        assertEquals(0, domain.streakFreezeCount)
        assertEquals(0, domain.currentStreak)
        assertEquals(0, domain.longestStreak)
    }

    @Test
    fun dailyLogResponseDtoResolvesLegacyMusicRecordWhenSplitFieldsAreBlank() {
        val domain = dailyLogDto(
            musicRecord = " Legacy Song - Legacy Artist ",
            musicTitle = " ",
            artistName = null
        ).toDomain()

        assertEquals("Legacy Song", domain.musicTitle)
        assertEquals("Legacy Artist", domain.artistName)
        assertEquals("Legacy Song - Legacy Artist", domain.musicRecord)
    }

    @Test
    fun dailyLogResponseDtoKeepsExplicitMusicFieldsOverLegacyRecord() {
        val domain = dailyLogDto(
            musicRecord = "Legacy Song - Legacy Artist",
            musicTitle = "New Song",
            artistName = "New Artist",
            albumArtUrl = " "
        ).toDomain()

        assertEquals("New Song", domain.musicTitle)
        assertEquals("New Artist", domain.artistName)
        assertEquals("Legacy Song - Legacy Artist", domain.musicRecord)
        assertNull(domain.albumArtUrl)
    }

    @Test
    fun themeEntityRoundTripPreservesThemeContractFields() {
        val theme = Theme(
            id = "custom-theme",
            name = "Evening",
            collection = "Custom Theme",
            price = 250,
            isFree = false,
            thumbnailUrl = "https://example.com/thumb.webp",
            backgroundUrl = "https://example.com/bg.webp",
            isOwned = true,
            isActive = true,
            description = "description",
            type = ThemeType.THEME,
            moods = listOf(
                ThemeMood(baseMoodId = 1, customName = "Very Sad", iconColor = "#111111"),
                ThemeMood(baseMoodId = 5, customName = "Very Happy", iconColor = "#555555"),
                ThemeMood(baseMoodId = 3, customName = "Neutral", iconColor = "#333333")
            ),
            primaryColor = "#123456",
            decoration = "STARS",
            activatedAt = 1234L
        )

        val entity = ThemeEntity.fromDomain(theme)
        val roundTrip = entity.toDomain()

        assertEquals(theme.id, roundTrip.id)
        assertEquals(theme.name, roundTrip.name)
        assertEquals(theme.collection, roundTrip.collection)
        assertEquals(theme.price, roundTrip.price)
        assertFalse(roundTrip.isFree)
        assertTrue(roundTrip.isOwned)
        assertTrue(roundTrip.isActive)
        assertEquals(theme.thumbnailUrl, roundTrip.thumbnailUrl)
        assertEquals(theme.backgroundUrl, roundTrip.backgroundUrl)
        assertEquals(theme.primaryColor, roundTrip.primaryColor)
        assertEquals(theme.decoration, roundTrip.decoration)
        assertEquals(theme.activatedAt, roundTrip.activatedAt)
        assertEquals(listOf("VERY_HAPPY", "NEUTRAL", "VERY_SAD"), roundTrip.icons)
        assertFalse(roundTrip.isOfficial)
    }

    @Test
    fun themeEntityUsesDefaultLegacyIconsWhenThemeHasNoMoods() {
        val entity = ThemeEntity.fromDomain(
            Theme(
                id = "theme-default",
                name = "Default",
                collection = "Moon Page",
                price = 0,
                thumbnailUrl = null,
                backgroundUrl = null,
                moods = emptyList()
            )
        )

        assertEquals("VERY_HAPPY,HAPPY,NEUTRAL,SAD,ANGRY", entity.icons)
        assertEquals(listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "VERY_SAD"), entity.toDomain().icons)
    }

    @Test
    fun statisticsConverterRoundTripsNestedStatisticsResponse() {
        val expected = StatisticsResponse(
            totalLogs = 12,
            totalPhotos = 4,
            currentStreak = 3,
            longestStreak = 7,
            streakFreezeCount = 1,
            moodDistribution = listOf(MoodDistributionDto(label = "Happy", baseMoodId = 4, count = 5, percentage = 41.7)),
            moodFlow = listOf(MoodFlowDto(date = "2026-06-01", moodId = 4.0)),
            bestActivities = listOf(
                BestActivityDto(
                    activityId = "reading",
                    activityName = "Reading",
                    iconUrl = "https://example.com/reading.png",
                    averageMoodScore = 4.2,
                    occurrence = 6,
                    moodDistribution = listOf(MoodDistributionDto(label = "Happy", baseMoodId = 4, count = 6, percentage = 100.0))
                )
            ),
            totalSteps = 10000,
            averageSteps = 5000.0,
            totalCalories = 300,
            averageCalories = 150.0,
            totalDistance = 8.5,
            averageDistance = 4.25,
            averageSleepHours = 7.5,
            averageSleepStartTime = "23:00",
            averageWakeupTime = "06:30"
        )

        val converter = Converters()
        val actual = converter.toStatisticsResponse(converter.fromStatisticsResponse(expected))

        assertEquals(expected.totalLogs, actual.totalLogs)
        assertEquals(expected.totalPhotos, actual.totalPhotos)
        assertEquals(expected.currentStreak, actual.currentStreak)
        assertEquals(expected.longestStreak, actual.longestStreak)
        assertEquals(expected.moodDistribution, actual.moodDistribution)
        assertEquals(expected.moodFlow, actual.moodFlow)
        assertEquals(expected.bestActivities, actual.bestActivities)
        assertEquals(expected.averageSleepStartTime, actual.averageSleepStartTime)
    }

    private fun dailyLogDto(
        musicRecord: String?,
        musicTitle: String?,
        artistName: String?,
        albumArtUrl: String? = "https://example.com/album.png"
    ): DailyLogResponseDto {
        return DailyLogResponseDto(
            id = "log-1",
            baseMoodId = 4,
            date = "2026-06-01",
            note = "note",
            sleepHours = 7.0,
            sleepStartTime = "23:00",
            isMenstruation = false,
            menstruationPhase = null,
            steps = 1200,
            musicRecord = musicRecord,
            musicTitle = musicTitle,
            artistName = artistName,
            albumArtUrl = albumArtUrl,
            dailyPhotos = listOf("photo-1.jpg"),
            activityIds = listOf("reading"),
            createdAt = "2026-06-01T10:00:00Z",
            calories = 100,
            distance = 2.5,
            wakeupTime = "06:30",
            weather = "Sunny",
            temperature = 30.0
        )
    }
}
