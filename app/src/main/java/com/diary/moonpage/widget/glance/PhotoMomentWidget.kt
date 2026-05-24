package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.ui.MainActivity
import kotlinx.coroutines.flow.firstOrNull

private val photoIndexKey = intPreferencesKey("photo_moment_index")
private val photoDirectionKey = ActionParameters.Key<Int>("photo_direction")

/**
 * Widget 1: Photo Moment (2×2) – Style Locket
 * - Ảnh fill 100% với ContentScale.Crop
 * - Bo góc 32dp cực tròn
 * - Tự động chuyển ảnh hôm nay
 * - Streak Badge TopEnd
 * - Click → mở app
 */
class PhotoMomentWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        var currentIndex = state[photoIndexKey] ?: 0
        val photoCount = snapshot.photoUris.size

        if (currentIndex >= photoCount) {
            currentIndex = 0
            updateAppWidgetState(context, id) { prefs -> prefs[photoIndexKey] = 0 }
        }

        val resolvedIndex = if (photoCount == 0) 0 else currentIndex.mod(photoCount)
        val bitmap = dataSource.loadBitmap(snapshot.photoUris.getOrNull(resolvedIndex))
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette

        val openAppAction = actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(32.dp)  // Locket-style extreme rounded corners
                    .background(
                        ColorProvider(if (isNight) palette.nightSurface else palette.daySurface)
                    )
                    .clickable(openAppAction),
                contentAlignment = Alignment.TopStart
            ) {
                // Photo fill 100%
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                } else {
                    // Placeholder khi chưa có ảnh
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        Text(
                            text = "📷",
                            style = TextStyle(fontSize = 28.sp)
                        )
                        Text(
                            text = context.getString(R.string.widget_photo_empty),
                            style = TextStyle(
                                color = ColorProvider(
                                    if (isNight) palette.nightOnSurface else palette.dayOnSurface
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Tap zones for previous/next photo
                if (photoCount > 1) {
                    Row(modifier = GlanceModifier.fillMaxSize()) {
                        Spacer(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .clickable(actionRunCallback<PhotoIndexAction>(
                                    actionParametersOf(photoDirectionKey to -1)
                                ))
                        )
                        Spacer(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .clickable(actionRunCallback<PhotoIndexAction>(
                                    actionParametersOf(photoDirectionKey to 1)
                                ))
                        )
                    }
                }

                // ── Streak Badge – TOP END ──
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(top = 12.dp, end = 12.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "🔥 ${snapshot.streakCount}",
                        modifier = GlanceModifier
                            .cornerRadius(50.dp)
                            .background(ColorProvider(if (isNight) palette.nightBadge else palette.dayBadge))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        style = TextStyle(
                            color = ColorProvider(if (isNight) palette.nightBadgeText else palette.dayBadgeText),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

class PhotoIndexAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val direction = parameters[photoDirectionKey] ?: 0
        val photoCount = MoonpageWidgetDataSource(context).loadTodaySnapshot().photoUris.size
        if (photoCount <= 1) return
        updateAppWidgetState(context, glanceId) { prefs: MutablePreferences ->
            val currentIndex = prefs[photoIndexKey] ?: 0
            prefs[photoIndexKey] = (currentIndex + direction).mod(photoCount)
        }
        PhotoMomentWidget().updateAll(context)
    }
}

class PhotoMomentWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhotoMomentWidget()
}
