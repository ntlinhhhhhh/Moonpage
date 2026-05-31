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
import com.diary.moonpage.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first
import java.util.Locale

class WeeklyMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val weekDays = dataSource.loadWeekSnapshot()
        val isNight = dataSource.isNightMode()
        val preferences = dataSource.getWidgetPreferences()

        val bg = if (isNight) snapshot.palette.nightSurface else snapshot.palette.daySurface
        val textColor = if (isNight) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
        val subColor = if (isNight) {
            snapshot.palette.nightOnSurface.copy(alpha = 0.6f)
        } else {
            snapshot.palette.dayOnSurface.copy(alpha = 0.6f)
        }
        val placeholderColor = if (isNight) snapshot.palette.nightSurfaceVariant else snapshot.palette.daySurfaceVariant
        val monthLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))

        val openAppAction = actionStartActivity(
            MoonpageWidgets.openAppIntent(context, MoonpageWidgets.ROUTE_CALENDAR)
        )

        // Read preferences once — avoids collectAsState keeping a live Flow session
        // that gets cancelled every time widget.update() is called.
        val showStreak = preferences.showWeeklyMoodStreak.first()
        
        val streakText = context.getString(R.string.streak_badge, snapshot.streakCount)

        val dayNames = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp)
                    .background(ColorProvider(bg))
                    .clickable(openAppAction)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Box(
                        modifier = GlanceModifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthLabel,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.size(4.dp))

                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        dayNames.forEach { label ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        color = ColorProvider(subColor),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(4.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weekDays.forEach { day ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    day.moodResId != null -> {
                                        Box(
                                            modifier = GlanceModifier
                                                .size(28.dp)
                                                .cornerRadius(14.dp)
                                                .background(ColorProvider(day.moodColor)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                provider = ImageProvider(day.moodResId),
                                                contentDescription = null,
                                                modifier = GlanceModifier.size(22.dp)
                                            )
                                        }
                                    }
                                    day.isToday -> {
                                        Box(
                                            modifier = GlanceModifier
                                                .size(28.dp)
                                                .background(ImageProvider(R.drawable.widget_day_today_border)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${day.dayNumber}",
                                                style = TextStyle(
                                                    color = ColorProvider(textColor),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = GlanceModifier
                                                .size(28.dp)
                                                .cornerRadius(14.dp)
                                                .background(ColorProvider(placeholderColor)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${day.dayNumber}",
                                                style = TextStyle(
                                                    color = ColorProvider(subColor),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showStreak) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(end = 4.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = streakText,
                            modifier = GlanceModifier
                                .cornerRadius(50.dp)
                                .background(ColorProvider(Color(0xCC000000)))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

class WeeklyMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyMoodWidget()
}
