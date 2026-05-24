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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.diary.moonpage.ui.MainActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Widget 4: Monthly Mood Calendar (4×3) – Style DailyBean
 * - Nền Surface theo theme, bo góc 16dp
 * - Header: "MMM yyyy" căn trái + Streak Badge TopEnd
 * - Row header: S M T W T F S
 * - Lưới lịch nguyên tháng (với padding cells đầu tháng)
 * - Click → mở app
 */
class MonthlyMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val monthDays = dataSource.loadMonthSnapshot()
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette

        val today = LocalDate.now()
        val monthLabel = today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))

        // Chunk into weeks (groups of 7)
        val weeks = monthDays.chunked(7)
        val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")

        val openAppAction = actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp)
                    .background(ColorProvider(if (isNight) palette.nightSurface else palette.daySurface))
                    .clickable(openAppAction)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    // ── Header: Month + Streak Badge ──
                    Box(
                        modifier = GlanceModifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = monthLabel,
                            style = TextStyle(
                                color = ColorProvider(if (isNight) palette.nightOnSurface else palette.dayOnSurface),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "🔥 ${snapshot.streakCount}",
                                modifier = GlanceModifier
                                    .cornerRadius(50.dp)
                                    .background(ColorProvider(if (isNight) palette.nightBadge else palette.dayBadge))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                style = TextStyle(
                                    color = ColorProvider(if (isNight) palette.nightBadgeText else palette.dayBadgeText),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(3.dp))

                    // ── Day-of-week header row ──
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        dayHeaders.forEach { label ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        color = ColorProvider(
                                            if (isNight) palette.nightOnSurface.copy(alpha = 0.45f)
                                            else palette.dayOnSurface.copy(alpha = 0.45f)
                                        ),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(2.dp))

                    // ── Calendar grid ──
                    weeks.forEach { week ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pad the last row to 7 cells if needed
                            val paddedWeek = week + List(7 - week.size) {
                                MonthDayMood(0, null, Color.Transparent, false, true)
                            }
                            paddedWeek.forEach { day ->
                                Box(
                                    modifier = GlanceModifier.defaultWeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        day.isEmpty -> {
                                            // Empty padding cell
                                            Spacer(modifier = GlanceModifier.size(18.dp))
                                        }
                                        day.moodResId != null -> {
                                            // Has mood – show icon
                                            Image(
                                                provider = ImageProvider(day.moodResId),
                                                contentDescription = null,
                                                modifier = GlanceModifier.size(18.dp)
                                            )
                                        }
                                        day.isToday -> {
                                            // Today without mood – dashed circle with day number
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(18.dp)
                                                    .cornerRadius(9.dp)
                                                    .background(
                                                        ColorProvider(
                                                            if (isNight) palette.nightOnSurface.copy(alpha = 0.12f)
                                                            else palette.dayOnSurface.copy(alpha = 0.10f)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${day.dayNumber}",
                                                    style = TextStyle(
                                                        color = ColorProvider(
                                                            if (isNight) palette.nightOnSurface
                                                            else palette.dayOnSurface
                                                        ),
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                        else -> {
                                            // Future/no-log day – subtle circle
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(18.dp)
                                                    .cornerRadius(9.dp)
                                                    .background(
                                                        ColorProvider(
                                                            if (isNight) palette.nightOnSurface.copy(alpha = 0.06f)
                                                            else palette.dayOnSurface.copy(alpha = 0.05f)
                                                        )
                                                    )
                                            ) {}
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

class MonthlyMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthlyMoodWidget()
}
