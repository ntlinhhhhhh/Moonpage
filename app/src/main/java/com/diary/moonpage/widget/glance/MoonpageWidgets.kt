package com.diary.moonpage.widget.glance

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object MoonpageWidgets {
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