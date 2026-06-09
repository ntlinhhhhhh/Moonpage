package com.diary.moonpage.ui.screens.calendar

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.domain.model.DailyLog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class CalendarScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCalendarScreen_rendersEmptyState() {
        val testUiState = CalendarUiState(
            currentYearMonth = YearMonth.of(2023, 10),
            selectedDate = LocalDate.of(2023, 10, 15),
            dailyLogs = emptyMap(),
            viewMode = CalendarViewMode.TIMELINE
        )

        composeTestRule.setContent {
            MoonPageTheme {
                CalendarScreen(
                    uiState = testUiState,
                    onEvent = {},
                    onNavigateToSettings = {},
                    onNavigateToDailyLog = {},
                    onNavigateToShareLog = {},
                    onNavigateToShareCalendar = {},
                    onNavigateToThemeCalendar = {},
                    onStreakClick = {},
                    showSnackbar = { _, _ -> },
                    onRefresh = {}
                )
            }
        }

        // Test timeline mode empty state
        composeTestRule.onNode(
            hasText("Chưa có nhật ký nào")
                .or(hasText("No entries yet"))
        ).assertExists()
    }

    @Test
    fun testCalendarScreen_rendersMoodSelectorsAndLogs() {
        val date = LocalDate.of(2023, 10, 15)
        val testUiState = CalendarUiState(
            currentYearMonth = YearMonth.of(2023, 10),
            selectedDate = date,
            dailyLogs = mapOf(
                date to DailyLog(
                    id = "1",
                    date = date.toString(),
                    baseMoodId = 5, // Rad / Very Happy
                    note = "Great day!",
                    sleepHours = null,
                    isMenstruation = false,
                    menstruationPhase = null,
                    dailyPhotos = null,
                    activityIds = null
                )
            ),
            viewMode = CalendarViewMode.TIMELINE
        )

        composeTestRule.setContent {
            MoonPageTheme {
                CalendarScreen(
                    uiState = testUiState,
                    onEvent = {},
                    onNavigateToSettings = {},
                    onNavigateToDailyLog = {},
                    onNavigateToShareLog = {},
                    onNavigateToShareCalendar = {},
                    onNavigateToThemeCalendar = {},
                    onStreakClick = {},
                    showSnackbar = { _, _ -> },
                    onRefresh = {}
                )
            }
        }

        // Wait for it to render
        composeTestRule.onNodeWithText("Great day!").assertExists()
    }
}
