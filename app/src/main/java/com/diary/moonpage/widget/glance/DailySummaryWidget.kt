package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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

/**
 * Widget 5: Daily Log Summary (4×2)
 * - Nền SurfaceVariant theo theme
 * - Bo góc 16dp
 * - 3 khu vực: Header (title + streak badge), Note (2 dòng ellipsis), Footer (mood/sleep/steps/kcal/distance/activities)
 * - Click toàn widget → mở app
 */
class DailySummaryWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette

        val openAppAction = actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp)
                    .background(
                        ColorProvider(if (isNight) palette.nightSurfaceVariant else palette.daySurfaceVariant)
                    )
                    .clickable(openAppAction)
                    .padding(14.dp)
            ) {
                // ── Header Row: Title + Streak Badge (TopEnd) ──
                Box(
                    modifier = GlanceModifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = context.getString(R.string.widget_today_summary),
                        style = TextStyle(
                            color = ColorProvider(if (isNight) palette.nightOnSurface else palette.dayOnSurface),
                            fontSize = 13.sp,
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
                                .background(
                                    ColorProvider(if (isNight) palette.nightBadge else palette.dayBadge)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = TextStyle(
                                color = ColorProvider(if (isNight) palette.nightBadgeText else palette.dayBadgeText),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.size(8.dp))

                // ── Note Text (2 lines max, ellipsis) ──
                Box(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = snapshot.note.ifBlank { context.getString(R.string.widget_no_note) },
                        maxLines = 2,
                        style = TextStyle(
                            color = ColorProvider(
                                if (isNight) palette.nightOnSurface.copy(alpha = 0.85f)
                                else palette.dayOnSurface.copy(alpha = 0.85f)
                            ),
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.size(8.dp))

                // ── Footer: mood + sleep + steps + kcal + distance + activities ──
                if (snapshot.footerItems.isNotEmpty()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        snapshot.footerItems.take(5).forEachIndexed { idx, item ->
                            if (idx > 0) {
                                Text(
                                    text = " · ",
                                    style = TextStyle(
                                        color = ColorProvider(
                                            if (isNight) palette.nightOnSurface.copy(alpha = 0.4f)
                                            else palette.dayOnSurface.copy(alpha = 0.4f)
                                        ),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            val display = if (item.label.isBlank()) item.emoji
                                         else "${item.emoji} ${item.label}"
                            Text(
                                text = display,
                                style = TextStyle(
                                    color = ColorProvider(
                                        if (isNight) palette.nightOnSurface.copy(alpha = 0.7f)
                                        else palette.dayOnSurface.copy(alpha = 0.7f)
                                    ),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class DailySummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailySummaryWidget()
}
