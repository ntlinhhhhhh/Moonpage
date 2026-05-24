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
import com.diary.moonpage.R
import com.diary.moonpage.ui.MainActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Widget 2: Quick Mood Selector (4×1) – Style DailyBean
 * - Nền Surface theo theme, bo góc 16dp
 * - "How was your day?" ở giữa trên cùng
 * - 5 icon cảm xúc hàng ngang cách đều
 * - Dòng dưới: "Thứ, Ngày Tháng" hiện tại
 * - Streak Badge TopEnd
 * - Click bất kỳ → mở app
 */
class QuickMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette

        val today = LocalDate.now()
        val dateStr = today.format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
        )

        // Mood icons and colors (level 5=very_happy → 1=very_sad)
        val moodLevels = listOf(5, 4, 3, 2, 1)
        val moodDrawables = moodLevels.map { dataSource.mapMoodDrawable(it) }

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
                    // Title
                    Text(
                        text = context.getString(R.string.widget_how_was_your_day),
                        style = TextStyle(
                            color = ColorProvider(if (isNight) palette.nightOnSurface else palette.dayOnSurface),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.size(4.dp))

                    // 5 Mood Icons Row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        moodDrawables.forEachIndexed { idx, drawableRes ->
                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(drawableRes),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(3.dp))

                    // Date Label
                    Text(
                        text = dateStr,
                        style = TextStyle(
                            color = ColorProvider(
                                if (isNight) palette.nightOnSurface.copy(alpha = 0.55f)
                                else palette.dayOnSurface.copy(alpha = 0.55f)
                            ),
                            fontSize = 9.sp
                        )
                    )
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

class QuickMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickMoodWidget()
}
