package com.diary.moonpage.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.diary.moonpage.R
import com.diary.moonpage.ui.MainActivity

class MoonpageWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(
                appWidgetId,
                createRemoteViews(context)
            )
        }
    }

    private fun createRemoteViews(context: Context): RemoteViews {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return RemoteViews(context.packageName, R.layout.widget_moonpage).apply {
            setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }
    }
}
