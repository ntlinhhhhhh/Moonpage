package com.diary.moonpage.widget.glance

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.diary.moonpage.ui.MainActivity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

object MoonpageWidgets {
    const val EXTRA_TARGET_ROUTE = "com.diary.moonpage.widget.TARGET_ROUTE"
    const val ROUTE_CALENDAR = "calendar_screen"
    const val ROUTE_CAMERA = "camera_screen"
    const val ROUTE_STATS_MOOD = "stats_mood_detail_screen"

    fun todayLogRoute(): String = "daily_log_screen/${LocalDate.now()}"

    fun openAppIntent(context: Context, targetRoute: String): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_ROUTE, targetRoute)
        }
    }

    suspend fun refreshAll(context: Context) {
        val appContext = context.applicationContext

        // Small delay to ensure DataStore has finished committing
        delay(100)

        // 1. Force Glance internal state update
        coroutineScope {
            launch { updateGlance(appContext, PhotoMomentWidget(), PhotoMomentWidgetReceiver::class.java) }
            launch { updateGlance(appContext, QuickMoodWidget(), QuickMoodWidgetReceiver::class.java) }
            launch { updateGlance(appContext, WeeklyMoodWidget(), WeeklyMoodWidgetReceiver::class.java) }
            launch { updateGlance(appContext, MonthlyMoodWidget(), MonthlyMoodWidgetReceiver::class.java) }
            launch { updateGlance(appContext, DailySummaryWidget(), DailySummaryWidgetReceiver::class.java) }
        }
    }

    private suspend fun updateGlance(context: Context, widget: GlanceAppWidget, receiverClass: Class<*>) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(widget::class.java)
        
        glanceIds.forEach { id ->
            widget.update(context, id)
        }

        // 2. Force system-level update for these IDs
        val componentName = ComponentName(context, receiverClass)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                component = componentName
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
