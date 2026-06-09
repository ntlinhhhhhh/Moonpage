package com.diary.moonpage.data.local

import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun testStatisticsResponseToStringAndBack() {
        // Arrange
        val originalResponse = StatisticsResponse(
            totalLogs = 10,
            totalPhotos = 5,
            currentStreak = 3,
            longestStreak = 5,
            streakFreezeCount = 0,
            moodDistribution = listOf(
                MoodDistributionDto(label = "Happy", count = 5, percentage = 50.0),
                MoodDistributionDto(label = "Sad", count = 5, percentage = 50.0)
            ),
            moodFlow = listOf(
                MoodFlowDto(date = "2023-10-01", moodId = 1.0)
            ),
            bestActivities = emptyList(),
            worstActivities = emptyList(),
            performedActivities = emptyList()
        )

        // Act
        val jsonString = converters.fromStatisticsResponse(originalResponse)
        val restoredResponse = converters.toStatisticsResponse(jsonString)

        // Assert
        assertEquals(originalResponse.totalLogs, restoredResponse.totalLogs)
        assertEquals(originalResponse.moodDistribution.size, restoredResponse.moodDistribution.size)
        assertEquals(originalResponse.moodDistribution[0].label, restoredResponse.moodDistribution[0].label)
    }
}
