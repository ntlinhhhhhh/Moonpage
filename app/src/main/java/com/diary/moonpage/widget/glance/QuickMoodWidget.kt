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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.diary.moonpage.R
import com.diary.moonpage.ui.MainActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class QuickMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()

        val today = LocalDate.now()
        val dateLabel = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH))

        val surfaceColor = if (isNight) snapshot.palette.nightSurface else snapshot.palette.daySurface
        val onSurfaceColor = if (isNight) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
        val badgeBg = if (isNight) snapshot.palette.nightBadge else snapshot.palette.dayBadge
        val badgeText = if (isNight) snapshot.palette.nightBadgeText else snapshot.palette.dayBadgeText

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "daily_log")
        }

        // Mood icons from level 5 (very happy) down to 1 (very sad)
        val moodIcons = listOf(
            R.drawable.very_happy,
            R.drawable.happy,
            R.drawable.neutral,
            R.drawable.sad,
            R.drawable.very_sad
        )

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(20.dp)
                    .background(ColorProvider(surfaceColor))
                    .clickable(actionStartActivity(openIntent))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    // ── Header: title + streak badge ──
                    Box(
                        modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Centered title
                        Text(
                            text = context.getString(R.string.daily_log_how_was_your_day),
                            style = TextStyle(
                                color = ColorProvider(onSurfaceColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        // Streak badge aligned to end
                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "\uD83D\uDD25 ${snapshot.streakCount}",
                                modifier = GlanceModifier
                                    .cornerRadius(50.dp)
                                    .background(ColorProvider(badgeBg))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                style = TextStyle(
                                    color = ColorProvider(badgeText),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // ── Body: 5 mood icons evenly distributed ──
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        moodIcons.forEachIndexed { index, resId ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(resId),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // ── Footer: date label ──
                    Box(
                        modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dateLabel,
                            style = TextStyle(
                                color = ColorProvider(onSurfaceColor.copy(alpha = 0.5f)),
                                fontSize = 9.sp,
                                fontStyle = FontStyle.Normal,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

class QuickMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickMoodWidget()
}
