package com.diary.moonpage

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.ui.screens.stats.ActivityInsightsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityInsightsEngineInstrumentedTest {
    @Test
    fun tc05ComputesBestAndWorstActivitiesFromDailyLogs() {
        val activities = listOf(
            activity("exercise", "Exercise"),
            activity("work", "Work"),
            activity("reading", "Reading")
        )
        val logs = listOf(
            dailyLog("2026-06-01", 5, "exercise"),
            dailyLog("2026-06-02", 4, "exercise"),
            dailyLog("2026-06-03", 5, "exercise"),
            dailyLog("2026-06-04", 1, "work"),
            dailyLog("2026-06-05", 2, "work"),
            dailyLog("2026-06-06", 2, "work"),
            dailyLog("2026-06-07", 3, "reading")
        )

        val (best, worst) = ActivityInsightsEngine.computeBestWorst(logs, activities)

        assertEquals("Exercise", best.first().activityName)
        assertEquals(100, best.first().bestRatePercent)
        assertEquals("Work", worst.first().activityName)
        assertEquals(100, worst.first().worstRatePercent)
    }

    @Test
    fun tc05ComputesActivityDeepDiveDistributionStreakAndRelatedActivities() {
        val activities = listOf(
            activity("exercise", "Exercise", occurrence = 4, averageMoodScore = 4.25),
            activity("reading", "Reading", occurrence = 2),
            activity("work", "Work", occurrence = 1)
        )
        val logs = listOf(
            dailyLog("2026-06-01", 5, "exercise", "reading"),
            dailyLog("2026-06-02", 4, "exercise", "reading"),
            dailyLog("2026-06-03", 5, "exercise", "work"),
            dailyLog("2026-06-10", 3, "exercise")
        )

        val result = ActivityInsightsEngine.computeIconDeepDive("exercise", logs, activities)

        assertEquals("exercise", result?.activityId)
        assertEquals(4, result?.totalOccurrence)
        assertEquals(3, result?.longestStreak)
        assertEquals(5, result?.moodDistribution?.size)
        assertEquals("Reading", result?.relatedActivities?.first()?.first)
        assertEquals(2, result?.relatedActivities?.first()?.second)
        assertTrue((result?.weeklyFrequency ?: 0.0) > 0.0)
    }

    @Test
    fun tc05FiltersLogsByMonthAndYear() {
        val logs = listOf(
            dailyLog("2026-06-01", 5, "exercise"),
            dailyLog("2026-06-30", 4, "reading"),
            dailyLog("2026-07-01", 3, "work"),
            dailyLog("bad-date", 2, "work")
        )

        val june = ActivityInsightsEngine.filterLogsByPeriod(
            logs = logs,
            year = 2026,
            month = 6,
            isMonthly = true
        )
        val wholeYear = ActivityInsightsEngine.filterLogsByPeriod(
            logs = logs,
            year = 2026,
            month = null,
            isMonthly = false
        )

        assertEquals(listOf("2026-06-01", "2026-06-30"), june.map { it.date })
        assertEquals(listOf("2026-06-01", "2026-06-30", "2026-07-01"), wholeYear.map { it.date })
    }

    private fun activity(
        id: String,
        name: String,
        occurrence: Int = 0,
        averageMoodScore: Double = 0.0
    ): BestActivityDto {
        return BestActivityDto(
            activityId = id,
            activityName = name,
            iconUrl = "https://example.com/$id.png",
            averageMoodScore = averageMoodScore,
            occurrence = occurrence
        )
    }

    private fun dailyLog(
        date: String,
        mood: Int,
        vararg activityIds: String
    ): DailyLog {
        return DailyLog(
            id = "log-$date",
            baseMoodId = mood,
            date = date,
            note = null,
            sleepHours = null,
            isMenstruation = false,
            menstruationPhase = null,
            dailyPhotos = emptyList(),
            activityIds = activityIds.toList()
        )
    }
}
