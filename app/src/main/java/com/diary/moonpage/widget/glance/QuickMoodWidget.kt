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
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first

class QuickMoodWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()
        val preferences = dataSource.getWidgetPreferences()
        val showLabels = preferences.showQuickMoodLabels.first()
        val _trigger = preferences.lastUpdateTrigger.first() // Force dependency

        val bg = if (isNight) snapshot.palette.nightSurface else snapshot.palette.daySurface
        val textColor = if (isNight) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
        val subColor = if (isNight) {
            snapshot.palette.nightOnSurface.copy(alpha = 0.6f)
        } else {
            snapshot.palette.dayOnSurface.copy(alpha = 0.6f)
        }
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH))
        val moodItems = listOf(
            5 to "Great",
            4 to "Good",
            3 to "Okay",
            2 to "Low",
            1 to "Bad"
        )
        val moodCircleSize = if (showLabels) 32.dp else 36.dp
        val moodCircleRadius = if (showLabels) 16.dp else 18.dp
        val moodImageSize = if (showLabels) 26.dp else 30.dp

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
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Box(
                        modifier = GlanceModifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "\u21BB",
                                style = TextStyle(
                                    color = ColorProvider(subColor),
                                    fontSize = 14.sp
                                )
                            )
                        }
                        Text(
                            text = context.getString(R.string.widget_how_was_your_day),
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "\u2699",
                                style = TextStyle(
                                    color = ColorProvider(subColor),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.size(6.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        moodItems.forEach { (level, label) ->
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                                verticalAlignment = Alignment.Vertical.CenterVertically
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(moodCircleSize)
                                        .cornerRadius(moodCircleRadius)
                                        .background(
                                            ColorProvider(
                                                snapshot.palette.moodColors[(level - 1).coerceIn(0, 4)]
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(dataSource.mapMoodDrawable(level)),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(moodImageSize)
                                    )
                                }

                                if (showLabels) {
                                    Spacer(modifier = GlanceModifier.size(2.dp))
                                    Text(
                                        text = label,
                                        style = TextStyle(
                                            color = ColorProvider(subColor),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    if (!showLabels) {
                        Spacer(modifier = GlanceModifier.size(5.dp))
                        Text(
                            text = dateStr,
                            style = TextStyle(
                                color = ColorProvider(subColor),
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "\uD83D\uDD25 ${snapshot.streakCount}",
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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        CoroutineScope(Dispatchers.Main).launch {
            QuickMoodWidget().updateAll(context)
        }
    }
}