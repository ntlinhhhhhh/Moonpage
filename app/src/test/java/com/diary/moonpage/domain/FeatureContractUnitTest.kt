package com.diary.moonpage.domain

import com.diary.moonpage.data.local.entity.DailyLogEntity
import com.diary.moonpage.data.remote.dto.activity.ActivityDto
import com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto
import com.diary.moonpage.data.remote.dto.moment.MomentResponse
import com.diary.moonpage.data.remote.dto.notification.CreateNotificationRequest
import com.diary.moonpage.data.remote.dto.notification.NotificationDto
import com.diary.moonpage.data.remote.dto.notification.NotificationListResponse
import com.diary.moonpage.data.remote.dto.notification.NotificationType
import com.diary.moonpage.data.remote.dto.notification.SendPushRequest
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.data.remote.dto.weather.CurrentWeatherDto
import com.diary.moonpage.data.remote.dto.weather.DailyWeatherDataDto
import com.diary.moonpage.data.remote.dto.weather.OpenMeteoResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureContractUnitTest {
    @Test
    fun tc02DailyLogResponseEntityRoundTripPreservesFullDailyLogFields() {
        val longNote = "a".repeat(1200)
        val response = DailyLogResponseDto(
            id = "log-full",
            baseMoodId = 5,
            date = "2026-05-20",
            note = longNote,
            sleepHours = 6.5,
            sleepStartTime = "23:45",
            isMenstruation = true,
            menstruationPhase = "period",
            steps = 12345,
            musicRecord = "Song - Artist",
            musicTitle = null,
            artistName = null,
            albumArtUrl = "https://example.com/album.png",
            dailyPhotos = listOf("photo1.jpg", "photo2.jpg", "photo3.jpg"),
            activityIds = listOf("reading", "exercise"),
            createdAt = "2026-06-01T09:00:00Z",
            calories = 640,
            distance = 7.25,
            wakeupTime = "06:20",
            weather = "Rainy",
            temperature = 27.5
        )

        val roundTrip = DailyLogEntity.fromResponse(response).toResponse().toDomain()

        assertEquals("log-full", roundTrip.id)
        assertEquals(5, roundTrip.baseMoodId)
        assertEquals("2026-05-20", roundTrip.date)
        assertEquals(longNote, roundTrip.note)
        assertEquals(6.5, roundTrip.sleepHours!!, 0.0)
        assertEquals("23:45", roundTrip.sleepStartTime)
        assertTrue(roundTrip.isMenstruation)
        assertEquals("period", roundTrip.menstruationPhase)
        assertEquals(listOf("photo1.jpg", "photo2.jpg", "photo3.jpg"), roundTrip.dailyPhotos)
        assertEquals(listOf("reading", "exercise"), roundTrip.activityIds)
        assertEquals(12345, roundTrip.steps)
        assertEquals("Song", roundTrip.musicTitle)
        assertEquals("Artist", roundTrip.artistName)
        assertEquals("https://example.com/album.png", roundTrip.albumArtUrl)
        assertEquals(640, roundTrip.calories)
        assertEquals(7.25, roundTrip.distance!!, 0.0)
        assertEquals("06:20", roundTrip.wakeupTime)
        assertEquals("Rainy", roundTrip.weather)
        assertEquals(27.5, roundTrip.temperature!!, 0.0)
    }

    @Test
    fun tc02AndTc07ActivityDtoMapsAvailableActivityCategoryFields() {
        val dto = ActivityDto(
            id = "reading",
            name = "Reading",
            iconUrl = "https://example.com/reading.png",
            category = "Hobbies"
        )

        val domain = dto.toDomain()

        assertEquals("reading", domain.id)
        assertEquals("Reading", domain.name)
        assertEquals("https://example.com/reading.png", domain.iconUrl)
        assertEquals("Hobbies", domain.category)
    }

    @Test
    fun tc04MomentResponseMapsLocalImageMetadataWeatherAndRating() {
        val response = MomentResponse(
            id = "moment-1",
            imageUrl = "/storage/emulated/0/DCIM/moment.jpg",
            caption = null,
            capturedAt = "2026-06-01T10:00:00Z",
            isPublic = false,
            dailyLogId = "2026-06-01",
            location = "Da Nang",
            weather = "Cloudy",
            rating = 4.5f
        )

        val domain = response.toDomain()

        assertEquals("moment-1", domain.id)
        assertEquals("file:///storage/emulated/0/DCIM/moment.jpg", domain.imageUrl)
        assertNull(domain.caption)
        assertFalse(domain.isPublic)
        assertEquals("2026-06-01", domain.dailyLogId)
        assertEquals("Da Nang", domain.location)
        assertEquals("Cloudy", domain.weather)
        assertEquals(4.5f, domain.rating)
    }

    @Test
    fun tc05StatisticsResponsePreservesEmptyAndLargeYearlyMoodGridData() {
        val yearlyGrid = (1..365).map { day ->
            MoodFlowDto(date = "2026-${((day - 1) / 31 + 1).coerceAtMost(12).toString().padStart(2, '0')}-01", moodId = ((day % 5) + 1).toDouble())
        }
        val response = StatisticsResponse(
            totalLogs = 365,
            totalPhotos = 0,
            currentStreak = 30,
            longestStreak = 100,
            moodDistribution = emptyList(),
            moodFlow = emptyList(),
            bestActivities = emptyList(),
            yearlyMoodGrid = yearlyGrid
        )

        assertEquals(365, response.totalLogs)
        assertTrue(response.moodDistribution.isEmpty())
        assertTrue(response.bestActivities.isEmpty())
        assertEquals(365, response.yearlyMoodGrid?.size)
    }

    @Test
    fun tc05StatisticsActivityDtoCarriesMoodDistributionForDeepDive() {
        val activity = BestActivityDto(
            activityId = "exercise",
            activityName = "Exercise",
            iconUrl = "https://example.com/exercise.png",
            averageMoodScore = 4.2,
            occurrence = 10,
            moodDistribution = listOf(
                MoodDistributionDto(label = "Very Happy", baseMoodId = 5, count = 6, percentage = 60.0),
                MoodDistributionDto(label = "Happy", baseMoodId = 4, count = 4, percentage = 40.0)
            )
        )

        assertEquals("exercise", activity.activityId)
        assertEquals(10, activity.occurrence)
        assertEquals(2, activity.moodDistribution?.size)
        assertEquals(60.0, activity.moodDistribution?.first()?.percentage ?: 0.0, 0.0)
    }

    @Test
    fun tc08NotificationDtosRepresentCenterAndPushPayloads() {
        val notification = NotificationDto(
            id = "noti-1",
            title = "How was your day?",
            message = "Take a minute to tell me.",
            type = NotificationType.REMINDER,
            isRead = false,
            createdAt = "2026-06-01T10:00:00Z"
        )
        val listResponse = NotificationListResponse(success = true, data = listOf(notification))
        val createRequest = CreateNotificationRequest(
            userId = "user-1",
            title = "Great start!",
            message = "3-day streak",
            type = NotificationType.STREAK
        )
        val pushRequest = SendPushRequest(
            token = "fcm-token",
            title = "Photo",
            body = "Large image notification",
            imageUrl = "https://example.com/image.png"
        )

        assertTrue(listResponse.success)
        assertEquals(notification, listResponse.data.single())
        assertEquals(NotificationType.STREAK, createRequest.type)
        assertEquals("https://example.com/image.png", pushRequest.imageUrl)
    }

    @Test
    fun tc11WeatherDtoCarriesCurrentAndDailyForecastData() {
        val dto = OpenMeteoResponseDto(
            currentWeather = CurrentWeatherDto(temperature = 30.5, windspeed = 12.0, weathercode = 61),
            daily = DailyWeatherDataDto(
                time = listOf("2026-06-01"),
                weathercode = listOf(61),
                temperatureMax = listOf(32.0),
                temperatureMin = listOf(25.0),
                windspeedMax = listOf(20.0)
            )
        )

        assertEquals(30.5, dto.currentWeather?.temperature ?: 0.0, 0.0)
        assertEquals(61, dto.currentWeather?.weathercode)
        assertEquals("2026-06-01", dto.daily?.time?.single())
        assertEquals(32.0, dto.daily?.temperatureMax?.single() ?: 0.0, 0.0)
    }
}
