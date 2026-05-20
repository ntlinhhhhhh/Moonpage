package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

class DailySummaryWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNightMode = dataSource.isNightMode()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(28.dp)
                    .background(
                        ColorProvider(
                            if (isNightMode) snapshot.palette.nightSurface else snapshot.palette.daySurface
                        )
                    )
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                        )
                    )
                    .padding(16.dp)
            ) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = "\uD83D\uDD25 ${snapshot.streakCount}",
                        modifier = GlanceModifier
                            .cornerRadius(18.dp)
                            .background(
                                ColorProvider(
                                    if (isNightMode) snapshot.palette.nightBadge else snapshot.palette.dayBadge
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        style = TextStyle(
                            color = ColorProvider(
                                if (isNightMode) snapshot.palette.nightBadgeText else snapshot.palette.dayBadgeText
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Box(
                        modifier = GlanceModifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        snapshot.moodResId?.let { moodRes ->
                            Image(
                                provider = ImageProvider(moodRes),
                                contentDescription = null,
                                modifier = GlanceModifier.size(32.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = GlanceModifier.fillMaxWidth().padding(top = 14.dp, bottom = 12.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = snapshot.note.ifBlank { context.getString(R.string.widget_summary_empty_note) },
                        maxLines = 3,
                        style = TextStyle(
                            color = ColorProvider(
                                if (isNightMode) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Text(
                    text = snapshot.footerItems.ifEmpty { listOf(context.getString(R.string.widget_daily_summary_name)) }
                        .take(4)
                        .joinToString("  |  "),
                    modifier = GlanceModifier.fillMaxWidth(),
                    style = TextStyle(
                        color = ColorProvider(
                            if (isNightMode) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
                        ),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

class DailySummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailySummaryWidget()
}
