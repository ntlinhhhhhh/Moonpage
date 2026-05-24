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

// Mood circle background colors (level 5→1: very_happy→very_sad)
private val MoodCircleColors = listOf(
    Color(0xFFF5DE6E), // very_happy – yellow
    Color(0xFFA8D96E), // happy – light green
    Color(0xFF5BAD6E), // neutral – medium green
    Color(0xFF2D6E45), // sad – dark green
    Color(0xFF4A4A4A)  // very_sad – dark gray
)

/**
 * Widget 2: Quick Mood Selector (4×1) – Style DailyBean
 * Layout: 3 rows:
 *  Row 1: [↻ icon] [How was your day?] [⚙ icon]
 *  Row 2: 5 emoji circles to (36dp) spaced evenly với colored background
 *  Row 3: "Thursday, November 27" date text, centered
 *  Streak Badge: TopEnd overlay
 */
class QuickMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()

        val bg = if (isNight) snapshot.palette.nightSurface else snapshot.palette.daySurface
        val textColor = if (isNight) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
        val subColor = if (isNight) snapshot.palette.nightOnSurface.copy(alpha=0.6f) else snapshot.palette.dayOnSurface.copy(alpha=0.6f)

        val today = LocalDate.now()
        val dateStr = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH))

        // Level order: very_happy(5), happy(4), neutral(3), sad(2), very_sad(1)
        val moodLevels = listOf(5, 4, 3, 2, 1)

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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    // ── Row 1: ↻ | "How was your day?" | ⚙ ──
                    Box(
                        modifier = GlanceModifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Left: ↻
                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "↻",
                                style = TextStyle(
                                    color = ColorProvider(subColor),
                                    fontSize = 14.sp
                                )
                            )
                        }
                        // Center: title
                        Text(
                            text = context.getString(R.string.widget_how_was_your_day),
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        // Right: ⚙
                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "⚙",
                                style = TextStyle(
                                    color = ColorProvider(subColor),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(6.dp))

                    // ── Row 2: 5 Emoji Circles ──
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        moodLevels.forEachIndexed { idx, level ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Colored circle background
                                Box(
                                    modifier = GlanceModifier
                                        .size(36.dp)
                                        .cornerRadius(18.dp)
                                        .background(ColorProvider(snapshot.palette.moodColors[(level - 1).coerceIn(0, 4)])),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(dataSource.mapMoodDrawable(level)),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(5.dp))

                    // ── Row 3: Date label ──
                    Text(
                        text = dateStr,
                        style = TextStyle(
                            color = ColorProvider(subColor),
                            fontSize = 9.sp
                        )
                    )
                }

                // ── Streak Badge – TOP END ──
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "🔥 ${snapshot.streakCount}",
                        modifier = GlanceModifier
                            .cornerRadius(50.dp)
                            .background(ColorProvider(Color(0xCC000000)))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
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

class QuickMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickMoodWidget()
}
