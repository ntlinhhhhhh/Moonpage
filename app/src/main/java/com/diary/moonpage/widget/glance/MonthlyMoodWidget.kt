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

// DailyBean-style off-white background color
private val DailyBeanBg = Color(0xFFF5F3EE)
private val DailyBeanBgDark = Color(0xFF2A2A2A)
private val DailyBeanText = Color(0xFF333333)
private val DailyBeanTextDark = Color(0xFFEEEEEE)
private val DailyBeanSubText = Color(0xFFAAAAAA)
private val DailyBeanSubTextDark = Color(0xFF888888)

private val MoodCircleColors = listOf(
    Color(0xFFF5DE6E), // very_happy
    Color(0xFFA8D96E), // happy
    Color(0xFF5BAD6E), // neutral
    Color(0xFF2D6E45), // sad
    Color(0xFF4A4A4A)  // very_sad
)

private val MutedCircleColor = Color(0xFFE0DDD8)
private val MutedCircleColorDark = Color(0xFF3A3A3A)

/**
 * Widget 4: Monthly Mood Calendar (4×3) – Style DailyBean
 */
class MonthlyMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val monthDays = dataSource.loadMonthSnapshot()
        val isNight = dataSource.isNightMode()

        val bg = if (isNight) snapshot.palette.nightSurface else snapshot.palette.daySurface
        val textColor = if (isNight) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
        val subColor = if (isNight) snapshot.palette.nightOnSurface.copy(alpha=0.6f) else snapshot.palette.dayOnSurface.copy(alpha=0.6f)
        val mutedCircleColor = if (isNight) MutedCircleColorDark else MutedCircleColor

        val today = LocalDate.now()
        val monthLabel = today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))

        // Chunk into weeks (groups of 7)
        val weeks = monthDays.chunked(7)
        val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

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
                    .background(ColorProvider(bg))
                    .clickable(openAppAction)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    // ── Header: ↻ | Month Year | ⚙ ──
                    Box(
                        modifier = GlanceModifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = GlanceModifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Text(text = "↻", style = TextStyle(color = ColorProvider(subColor), fontSize = 13.sp))
                        }
                        Text(
                            text = monthLabel,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(modifier = GlanceModifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(text = "⚙", style = TextStyle(color = ColorProvider(subColor), fontSize = 13.sp))
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(4.dp))

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
                                        color = ColorProvider(subColor),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(3.dp))

                    // ── Calendar grid ──
                    weeks.forEach { week ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                            Spacer(modifier = GlanceModifier.size(24.dp))
                                        }
                                        day.moodResId != null -> {
                                            // Has mood – colored circle
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(24.dp)
                                                    .cornerRadius(12.dp)
                                                    .background(ColorProvider(day.moodColor)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    provider = ImageProvider(day.moodResId),
                                                    contentDescription = null,
                                                    modifier = GlanceModifier.size(19.dp)
                                                )
                                            }
                                        }
                                        day.isToday -> {
                                            // Today without mood – dashed circle
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(24.dp)
                                                    .background(ImageProvider(com.diary.moonpage.R.drawable.widget_day_today_border)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${day.dayNumber}",
                                                    style = TextStyle(
                                                        color = ColorProvider(subColor),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                        day.dayNumber > today.dayOfMonth -> {
                                            // Future day – just text
                                            Text(
                                                text = "${day.dayNumber}",
                                                style = TextStyle(
                                                    color = ColorProvider(subColor.copy(alpha = 0.5f)),
                                                    fontSize = 8.sp
                                                )
                                            )
                                        }
                                        else -> {
                                            // Past day no mood – muted circle
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(24.dp)
                                                    .cornerRadius(12.dp)
                                                    .background(ColorProvider(mutedCircleColor))
                                            ) {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Streak Badge – TOP END ──
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(end = 4.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "🔥 ${snapshot.streakCount}",
                        modifier = GlanceModifier
                            .cornerRadius(50.dp)
                            .background(ColorProvider(Color(0xCC000000)))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

class MonthlyMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthlyMoodWidget()
}
