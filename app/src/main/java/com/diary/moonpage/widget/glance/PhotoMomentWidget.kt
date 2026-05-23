package com.diary.moonpage.widget.glance

import android.content.Context
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.diary.moonpage.R
import kotlinx.coroutines.flow.firstOrNull

private val photoIndexKey = intPreferencesKey("photo_moment_index")
private val photoDirectionKey = ActionParameters.Key<Int>("photo_direction")

class PhotoMomentWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val currentIndex = state[photoIndexKey] ?: 0
        val photoCount = snapshot.photoUris.size
        val resolvedIndex = if (photoCount == 0) 0 else currentIndex.mod(photoCount)
        val bitmap = dataSource.loadBitmap(snapshot.photoUris.getOrNull(resolvedIndex))
        
        val widgetPrefs = dataSource.getWidgetPreferences()
        val showStreak = widgetPrefs.showPhotoStreak.firstOrNull() ?: true
        val displayMode = widgetPrefs.photoDisplayMode.firstOrNull() ?: "CROP"

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(28.dp)
                    .background(
                        ColorProvider(
                            if (dataSource.isNightMode()) snapshot.palette.nightSurface else snapshot.palette.daySurface
                        )
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        contentScale = if (displayMode == "CROP") ContentScale.Crop else ContentScale.Fit,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
                if (bitmap == null) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        Text(
                            text = context.getString(R.string.widget_photo_empty),
                            style = TextStyle(
                                color = ColorProvider(
                                    if (dataSource.isNightMode()) snapshot.palette.nightOnSurface else snapshot.palette.dayOnSurface
                                ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                if (showStreak) {
                    Text(
                        text = "\uD83D\uDD25 ${snapshot.streakCount}",
                        modifier = GlanceModifier
                            .cornerRadius(18.dp)
                            .background(
                                ColorProvider(
                                    if (dataSource.isNightMode()) snapshot.palette.nightBadge else snapshot.palette.dayBadge
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        style = TextStyle(
                            color = ColorProvider(
                                if (dataSource.isNightMode()) snapshot.palette.nightBadgeText else snapshot.palette.dayBadgeText
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (photoCount > 1) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                    ) {
                        Spacer(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .clickable(
                                    actionRunCallback<PhotoIndexAction>(
                                        actionParametersOf(photoDirectionKey to -1)
                                    )
                                )
                        )
                        Spacer(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .clickable(
                                    actionRunCallback<PhotoIndexAction>(
                                        actionParametersOf(photoDirectionKey to 1)
                                    )
                                )
                        )
                    }
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
            val nextIndex = (currentIndex + direction).mod(photoCount)
            prefs[photoIndexKey] = nextIndex
        }
        PhotoMomentWidget().updateAll(context)
    }
}

class PhotoMomentWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhotoMomentWidget()
}
