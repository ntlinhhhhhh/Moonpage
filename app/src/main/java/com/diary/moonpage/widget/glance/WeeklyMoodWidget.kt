package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
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
 * Widget 3: Weekly Mood Calendar (4×1) – Style DailyBean
 * - Nền Surface theo theme, bo góc 16dp
 * - Header: "MMM yyyy" căn trái
 * - 7 cột: nhãn ngày (S/M/T/W/T/F/S) + icon mood (hoặc vòng tròn mờ)
 * - Ngày hiện tại: highlight
 * - Streak Badge TopEnd
 * - Click → mở app
 */
class WeeklyMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val weekDays = dataSource.loadWeekSnapshot()
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette

        val today = LocalDate.now()
        val monthLabel = today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))

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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    // Header: Month Year
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
                    }

                    Spacer(modifier = GlanceModifier.size(4.dp))

                    // 7-day row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weekDays.forEach { day ->
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                            ) {
                                // Day label (S, M, T, ...)
                                Text(
                                    text = day.dayLabel.take(1),
                                    style = TextStyle(
                                        color = ColorProvider(
                                            if (day.isToday) {
                                                if (isNight) palette.nightOnSurface else palette.dayOnSurface
                                            } else {
                                                if (isNight) palette.nightOnSurface.copy(alpha = 0.5f)
                                                else palette.dayOnSurface.copy(alpha = 0.5f)
                                            }
                                        ),
                                        fontSize = 8.sp,
                                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                Spacer(modifier = GlanceModifier.size(2.dp))
                                // Mood icon or empty circle
                                if (day.moodResId != null) {
                                    Image(
                                        provider = ImageProvider(day.moodResId),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(24.dp)
                                    )
                                } else {
                                    // Empty circle (dashed for today, solid muted for others)
                                    Box(
                                        modifier = GlanceModifier
                                            .size(24.dp)
                                            .cornerRadius(12.dp)
                                            .background(
                                                ColorProvider(
                                                    if (day.isToday)
                                                        (if (isNight) palette.nightOnSurface.copy(alpha = 0.15f)
                                                         else palette.dayOnSurface.copy(alpha = 0.1f))
                                                    else
                                                        (if (isNight) palette.nightOnSurface.copy(alpha = 0.08f)
                                                         else palette.dayOnSurface.copy(alpha = 0.06f))
                                                )
                                            )
                                    ) {}
                                }
                            }
                        }
                    }
                }

                // ── Streak Badge – TOP END ──
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(top = 0.dp, end = 0.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "🔥 ${snapshot.streakCount}",
                        modifier = GlanceModifier
                            .cornerRadius(50.dp)
                            .background(ColorProvider(if (isNight) palette.nightBadge else palette.dayBadge))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = TextStyle(
                            color = ColorProvider(if (isNight) palette.nightBadgeText else palette.dayBadgeText),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

class WeeklyMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyMoodWidget()
}
