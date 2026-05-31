package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
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
import kotlinx.coroutines.flow.first
import java.util.Locale

private val ThemeDefaultIconColor = Color(0xFFDB9D1F)

class DailySummaryWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette
        val preferences = dataSource.getWidgetPreferences()

        val showStreak = preferences.showDailyStreak.first()
        val showNote = preferences.showDailyNote.first()
        val showStats = preferences.showDailyStats.first()
        
        val streakText = context.getString(R.string.streak_badge, snapshot.streakCount)

        val openAppAction = actionStartActivity(
            MoonpageWidgets.openAppIntent(context, MoonpageWidgets.todayLogRoute())
        )

        provideContent {
            val bg = if (isNight) palette.nightSurface else palette.daySurface
            val textColor = if (isNight) palette.nightOnSurface else palette.dayOnSurface
            val iconTint = ColorProvider(ThemeDefaultIconColor)
            val note = snapshot.note.ifBlank { "-" }
            val moodCircleColor = if (snapshot.moodLevel > 0) {
                palette.moodColors[(snapshot.moodLevel - 1).coerceIn(0, 4)]
            } else {
                ThemeDefaultIconColor.copy(alpha = 0.22f)
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp)
                    .background(ColorProvider(bg))
                    .clickable(openAppAction)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(52.dp)
                            .cornerRadius(26.dp)
                            .background(ColorProvider(moodCircleColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(snapshot.moodResId ?: R.drawable.ic_widget_stat),
                            contentDescription = null,
                            modifier = GlanceModifier.size(40.dp),
                            colorFilter = if (snapshot.moodResId == null) {
                                ColorFilter.tint(iconTint)
                            } else {
                                null
                            }
                        )
                    }

                    if (showNote) {
                        Text(
                            text = note,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    if (showStats) {
                        ActivityIconRow(
                            items = snapshot.activityItems,
                            iconTint = iconTint
                        )
                        Spacer(modifier = GlanceModifier.size(8.dp))
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DailyMetricItem(
                                modifier = GlanceModifier.defaultWeight(),
                                iconRes = R.drawable.ic_widget_sleep,
                                value = snapshot.sleep?.let { String.format(Locale.ENGLISH, "%.1fh", it) } ?: "-",
                                textColor = textColor,
                                iconTint = iconTint
                            )
                            DailyMetricItem(
                                modifier = GlanceModifier.defaultWeight(),
                                iconRes = R.drawable.ic_widget_steps,
                                value = snapshot.steps?.let { String.format(Locale.ENGLISH, "%,d", it) } ?: "-",
                                textColor = textColor,
                                iconTint = iconTint
                            )
                            DailyMetricItem(
                                modifier = GlanceModifier.defaultWeight(),
                                iconRes = R.drawable.ic_widget_kcal,
                                value = snapshot.calories?.let { String.format(Locale.ENGLISH, "%,d", it) } ?: "-",
                                textColor = textColor,
                                iconTint = iconTint
                            )
                            DailyMetricItem(
                                modifier = GlanceModifier.defaultWeight(),
                                iconRes = R.drawable.ic_widget_distance,
                                value = snapshot.distance?.let { String.format(Locale.ENGLISH, "%.1fkm", it / 1000.0) } ?: "-",
                                textColor = textColor,
                                iconTint = iconTint
                            )
                        }
                    }
                }

                if (showStreak) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = streakText,
                            modifier = GlanceModifier
                                .cornerRadius(50.dp)
                                .background(ColorProvider(if (isNight) palette.nightBadge else palette.dayBadge))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            style = TextStyle(
                                color = ColorProvider(if (isNight) palette.nightBadgeText else palette.dayBadgeText),
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

@androidx.compose.runtime.Composable
private fun ActivityIconRow(
    items: List<WidgetActivityItem>,
    iconTint: ColorProvider
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            Box(
                modifier = GlanceModifier.defaultWeight(),
                contentAlignment = Alignment.Center
            ) {
                val item = items.getOrNull(index)
                if (item != null) {
                    Box(
                        modifier = GlanceModifier
                            .size(28.dp)
                            .cornerRadius(14.dp)
                            .background(ColorProvider(ThemeDefaultIconColor.copy(alpha = 0.18f))),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(item.iconResId),
                            contentDescription = null,
                            modifier = GlanceModifier.size(18.dp),
                            colorFilter = ColorFilter.tint(iconTint)
                        )
                    }
                } else {
                    Spacer(modifier = GlanceModifier.size(28.dp))
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun DailyMetricItem(
    modifier: GlanceModifier,
    iconRes: Int,
    value: String,
    textColor: Color,
    iconTint: ColorProvider
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp),
            colorFilter = ColorFilter.tint(iconTint)
        )
        Text(
            text = value,
            style = TextStyle(color = ColorProvider(textColor), fontSize = 10.sp, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
    }
}

class DailySummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailySummaryWidget()
}
