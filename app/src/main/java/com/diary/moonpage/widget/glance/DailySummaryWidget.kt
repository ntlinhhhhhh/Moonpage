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
import com.diary.moonpage.ui.MainActivity
import java.util.Locale
import kotlinx.coroutines.flow.firstOrNull

private val ThemeDefaultIconColor = Color(0xFFDB9D1F)

class DailySummaryWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette
        val preferences = dataSource.getWidgetPreferences()
        val showStreak = preferences.showDailyStreak.firstOrNull() ?: true
        val showNote = preferences.showDailyNote.firstOrNull() ?: true
        val showStats = preferences.showDailyStats.firstOrNull() ?: true

        val openAppAction = actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )

        provideContent {
            val bg = if (isNight) palette.nightSurface else palette.daySurface
            val textColor = if (isNight) palette.nightOnSurface else palette.dayOnSurface
            val subColor = if (isNight) {
                palette.nightOnSurface.copy(alpha = 0.62f)
            } else {
                palette.dayOnSurface.copy(alpha = 0.62f)
            }
            val iconTint = ColorProvider(ThemeDefaultIconColor)
            val note = snapshot.note.ifBlank { "-" }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp)
                    .background(ColorProvider(bg))
                    .clickable(openAppAction)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(snapshot.moodResId ?: R.drawable.ic_widget_stat),
                        contentDescription = null,
                        modifier = GlanceModifier.size(42.dp),
                        colorFilter = if (snapshot.moodResId == null) {
                            ColorFilter.tint(iconTint)
                        } else {
                            null
                        }
                    )

                    if (showNote) {
                        Text(
                            text = note,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    if (showStats) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DailySummaryFooterItem(
                                modifier = GlanceModifier.defaultWeight(),
                                label = "Activity",
                                value = snapshot.firstActivity ?: "-",
                                textColor = textColor,
                                subColor = subColor,
                                iconTint = iconTint
                            )
                            DailySummaryFooterItem(
                                modifier = GlanceModifier.defaultWeight(),
                                label = "Sleep",
                                value = snapshot.sleep?.let { String.format(Locale.ENGLISH, "%.1fh", it) } ?: "-",
                                textColor = textColor,
                                subColor = subColor,
                                iconTint = iconTint
                            )
                            DailySummaryFooterItem(
                                modifier = GlanceModifier.defaultWeight(),
                                label = "Steps",
                                value = snapshot.steps?.let { String.format(Locale.ENGLISH, "%,d", it) } ?: "-",
                                textColor = textColor,
                                subColor = subColor,
                                iconTint = iconTint
                            )
                            DailySummaryFooterItem(
                                modifier = GlanceModifier.defaultWeight(),
                                label = "Kcal",
                                value = snapshot.calories?.let { String.format(Locale.ENGLISH, "%,d", it) } ?: "-",
                                textColor = textColor,
                                subColor = subColor,
                                iconTint = iconTint
                            )
                            DailySummaryFooterItem(
                                modifier = GlanceModifier.defaultWeight(),
                                label = "Dist",
                                value = snapshot.distance?.let { String.format(Locale.ENGLISH, "%.1fkm", it / 1000.0) } ?: "-",
                                textColor = textColor,
                                subColor = subColor,
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
}

@androidx.compose.runtime.Composable
private fun DailySummaryFooterItem(
    modifier: GlanceModifier,
    label: String,
    value: String,
    textColor: Color,
    subColor: Color,
    iconTint: ColorProvider
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_stat),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp),
            colorFilter = ColorFilter.tint(iconTint)
        )
        Text(
            text = label,
            style = TextStyle(color = ColorProvider(subColor), fontSize = 7.sp),
            maxLines = 1
        )
        Text(
            text = value,
            style = TextStyle(color = ColorProvider(textColor), fontSize = 9.sp, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
    }
}

class DailySummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailySummaryWidget()
}
