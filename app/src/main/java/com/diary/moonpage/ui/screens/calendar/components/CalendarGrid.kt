package com.diary.moonpage.ui.screens.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.ui.screens.calendar.FilterItem
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.core.util.MoonIcon
import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarGrid(
    pageYearMonth: YearMonth,
    selectedDate: LocalDate?,
    dailyLogs: Map<LocalDate, DailyLog>,
    selectedFilters: List<FilterItem>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: com.diary.moonpage.core.theme.MoonThemeType,
    customMoods: Map<Int, MoonIcon>? = null,
    onDateSelected: (LocalDate) -> Unit,
    isReadOnly: Boolean = false
) {
    val daysInMonth = pageYearMonth.lengthOfMonth()
    val firstDayOfMonth = pageYearMonth.atDay(1)
    val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = firstDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7
    val today = LocalDate.now()
    val isActuallyDark = com.diary.moonpage.core.theme.MoonTheme.customColors.isDark

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (colIndex in 0 until 7) {
                    val cellIndex = rowIndex * 7 + colIndex
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cellIndex in firstDayOffset until totalCells) {
                            val day = cellIndex - firstDayOffset + 1
                            val date = pageYearMonth.atDay(day)
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val logForDay = dailyLogs[date]

                            val isMatch = if (selectedFilters.isEmpty()) true else {
                                selectedFilters.any { filter ->
                                    when (filter) {
                                        is FilterItem.Mood -> logForDay?.baseMoodId == filter.id
                                        is FilterItem.Activity -> logForDay?.activityIds?.contains(filter.id) == true
                                        is FilterItem.Special -> {
                                            when (filter.id) {
                                                "music" -> logForDay?.activityIds?.any { it.contains("music", ignoreCase = true) } == true
                                                "sleep" -> (logForDay?.sleepHours ?: 0.0) > 0.0
                                                "sleep_long" -> (logForDay?.sleepHours ?: 0.0) in 6.0..8.0
                                                "menstruation" -> logForDay?.isMenstruation == true
                                                else -> false
                                            }
                                        }
                                    }
                                }
                            }
                            
                            val isFiltered = selectedFilters.isNotEmpty()
                            val isDimmed = isFiltered && !isMatch

                            var moodColor: Color? = null
                            var moodIcon: ImageVector? = null
                            var moodDrawable: Int? = null

                            if (logForDay != null) {
                                val mv = MoonIcons.Moods.getMoodVisual(logForDay.baseMoodId, themeType, customMoods)
                                moodColor = mv.color
                                moodDrawable = mv.drawableRes
                                moodIcon = mv.vector
                            }

                            DayItem(
                                day = day,
                                isSelected = isSelected,
                                moodColor = moodColor,
                                moodIcon = moodIcon,
                                moodDrawable = moodDrawable,
                                isToday = isToday,
                                isDimmed = isDimmed,
                                isFiltered = isFiltered,
                                themeType = themeType,
                                isActuallyDark = isActuallyDark,
                                modifier = Modifier.tutorialTarget(TutorialStep.HighlightCurrentDay, enabled = isToday),
                                onClick = { if (!isReadOnly) onDateSelected(date) }
                            )
                        } else {
                            DayItem(day = null, isSelected = false, moodColor = null, isActuallyDark = isActuallyDark, onClick = {})
                        }
                    }
                }
            }
        }
    }
}
