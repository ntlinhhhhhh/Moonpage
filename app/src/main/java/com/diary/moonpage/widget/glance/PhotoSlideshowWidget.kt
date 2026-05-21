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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.diary.moonpage.R
import com.diary.moonpage.ui.MainActivity
import java.util.concurrent.TimeUnit

class PhotoSlideshowWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val isNight = dataSource.isNightMode()

        val photoCount = snapshot.photoUris.size
        // Read the global slideshow index from SharedPreferences
        val currentIndex = getSlideshowIndex(context)
        val resolvedIndex = if (photoCount == 0) 0 else currentIndex.mod(photoCount)
        val bitmap = dataSource.loadBitmap(snapshot.photoUris.getOrNull(resolvedIndex))

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "daily_log")
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(
                        ColorProvider(
                            if (isNight) snapshot.palette.nightSurface else snapshot.palette.daySurface
                        )
                    )
                    .clickable(actionStartActivity(openIntent)),
                contentAlignment = Alignment.TopStart
            ) {
                // Background photo
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                } else {
                    // Empty state: centered placeholder icon + message
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        Image(
                            provider = ImageProvider(R.mipmap.ic_launcher_round),
                            contentDescription = null,
                            modifier = GlanceModifier.wrapContentSize()
                        )
                        Text(
                            text = context.getString(R.string.widget_photo_empty),
                            style = TextStyle(
                                color = ColorProvider(
                                    if (isNight) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = GlanceModifier.padding(top = 6.dp)
                        )
                    }
                }

                // Streak Badge – top-right corner
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(top = 10.dp, end = 10.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "\uD83D\uDD25 ${snapshot.streakCount}",
                        modifier = GlanceModifier
                            .cornerRadius(50.dp)
                            .background(
                                ColorProvider(
                                    if (isNight) snapshot.palette.nightBadge else snapshot.palette.dayBadge
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = TextStyle(
                            color = ColorProvider(
                                if (isNight) snapshot.palette.nightBadgeText else snapshot.palette.dayBadgeText
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

class PhotoSlideshowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhotoSlideshowWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Schedule 30-minute periodic slideshow update
        val request = PeriodicWorkRequestBuilder<PhotoSlideshowWorker>(
            30, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SLIDESHOW_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SLIDESHOW_WORK_NAME)
        super.onDisabled(context)
    }
}

internal const val SLIDESHOW_WORK_NAME = "photo_slideshow_auto_advance"
