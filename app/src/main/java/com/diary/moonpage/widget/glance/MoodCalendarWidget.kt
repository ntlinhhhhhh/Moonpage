package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.diary.moonpage.R
import com.diary.moonpage.ui.MainActivity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

class MoodCalendarWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()

        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)
        val yearMonthStr = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val monthTitle = yearMonth.month.getDisplayName(JTextStyle.FULL, Locale.ENGLISH) +
                " " + yearMonth.year

        // Load real data: map of "yyyy-MM-dd" → moodResId
        val moodMap = dataSource.loadMonthLogsMap(yearMonthStr)

        // Colors
        val surfaceColor = if (isNight) snapshot.palette.nightSurface else snapshot.palette.daySurface
        val onSurfaceColor = if (isNight) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
        val subtleColor = if (isNight) Color(0x44FFFFFF) else Color(0x22000000)
        val dashedCircleColor = if (isNight) Color(0xFF6EBB82) else Color(0xFF4A9D5E)
        val badgeBg = if (isNight) snapshot.palette.nightBadge else snapshot.palette.dayBadge
        val badgeText = if (isNight) snapshot.palette.nightBadgeText else snapshot.palette.dayBadgeText

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "daily_log")
        }

        // Build calendar: first day of month's weekday offset (0=Sun..6=Sat)
        val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value % 7 // Sun=0
        val daysInMonth = yearMonth.lengthOfMonth()

        // Day header labels
        val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(20.dp)
                    .background(ColorProvider(surfaceColor))
                    .clickable(actionStartActivity(openIntent))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    // ── Header Row ──
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        // Refresh icon (decorative - opens app on click)
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_refresh),
                            contentDescription = "refresh",
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        // Month title — centered
                        Box(
                            modifier = GlanceModifier.defaultWeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monthTitle,
                                style = TextStyle(
                                    color = ColorProvider(onSurfaceColor),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        // Settings icon + Streak badge
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_settings),
                            contentDescription = "settings",
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = "\uD83D\uDD25 ${snapshot.streakCount}",
                            modifier = GlanceModifier
                                .cornerRadius(50.dp)
                                .background(ColorProvider(badgeBg))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            style = TextStyle(
                                color = ColorProvider(badgeText),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // ── Day-of-week header ──
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().wrapContentHeight()
                    ) {
                        dayHeaders.forEach { day ->
                            Box(
                                modifier = GlanceModifier.defaultWeight().wrapContentHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    style = TextStyle(
                                        color = ColorProvider(onSurfaceColor.copy(alpha = 0.5f)),
                                        fontSize = 8.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    // ── Calendar grid ──
                    // Calculate total cells needed
                    val totalCells = firstDayOffset + daysInMonth
                    val totalRows = (totalCells + 6) / 7

                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        for (row in 0 until minOf(totalRows, 3)) { // max 3 rows for 4×2
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                            ) {
                                for (col in 0..6) {
                                    val cellIndex = row * 7 + col
                                    val day = cellIndex - firstDayOffset + 1
                                    Box(
                                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            day < 1 || day > daysInMonth -> {
                                                // Empty cell
                                            }
                                            else -> {
                                                val dateStr = "%s-%02d".format(yearMonthStr, day)
                                                val moodResId = moodMap[dateStr]
                                                val isToday = day == today.dayOfMonth &&
                                                        yearMonth == YearMonth.from(today)
                                                val isPast = LocalDate.of(
                                                    yearMonth.year, yearMonth.month, day
                                                ).isBefore(today)

                                                when {
                                                    moodResId != null -> {
                                                        // Has mood log — show mood icon
                                                        Image(
                                                            provider = ImageProvider(moodResId),
                                                            contentDescription = null,
                                                            modifier = GlanceModifier.size(22.dp)
                                                        )
                                                    }
                                                    isToday -> {
                                                        // Today without log — dashed circle with day number
                                                        Box(
                                                            modifier = GlanceModifier
                                                                .size(22.dp)
                                                                .cornerRadius(11.dp)
                                                                .background(ColorProvider(subtleColor)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "$day",
                                                                style = TextStyle(
                                                                    color = ColorProvider(dashedCircleColor),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    textAlign = TextAlign.Center
                                                                )
                                                            )
                                                        }
                                                    }
                                                    isPast -> {
                                                        // Past day without log — faint circle
                                                        Box(
                                                            modifier = GlanceModifier
                                                                .size(22.dp)
                                                                .cornerRadius(11.dp)
                                                                .background(ColorProvider(subtleColor)),
                                                            contentAlignment = Alignment.Center
                                                        ) {}
                                                    }
                                                    else -> {
                                                        // Future day — just show number
                                                        Text(
                                                            text = "$day",
                                                            style = TextStyle(
                                                                color = ColorProvider(
                                                                    onSurfaceColor.copy(alpha = 0.35f)
                                                                ),
                                                                fontSize = 9.sp,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class MoodCalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MoodCalendarWidget()
}
