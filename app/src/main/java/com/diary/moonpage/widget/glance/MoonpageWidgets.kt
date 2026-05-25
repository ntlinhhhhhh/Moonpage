package com.diary.moonpage.widget.glance

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll

object MoonpageWidgets {
    suspend fun refreshAll(context: Context) {
        val appContext = context.applicationContext

        PhotoMomentWidget().updateAll(appContext)
        QuickMoodWidget().updateAll(appContext)
        WeeklyMoodWidget().updateAll(appContext)
        MonthlyMoodWidget().updateAll(appContext)
        DailySummaryWidget().updateAll(appContext)

        requestReceiverUpdate(appContext, PhotoMomentWidgetReceiver::class.java)
        requestReceiverUpdate(appContext, QuickMoodWidgetReceiver::class.java)
        requestReceiverUpdate(appContext, WeeklyMoodWidgetReceiver::class.java)
        requestReceiverUpdate(appContext, MonthlyMoodWidgetReceiver::class.java)
        requestReceiverUpdate(appContext, DailySummaryWidgetReceiver::class.java)
    }

    private fun requestReceiverUpdate(
        context: Context,
        receiverClass: Class<*>
    ) {
        val componentName = ComponentName(context, receiverClass)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = componentName
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }
}
