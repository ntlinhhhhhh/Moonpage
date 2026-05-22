package com.diary.moonpage.widget.glance

import android.content.Context
import androidx.glance.appwidget.updateAll

object MoonpageWidgets {
    suspend fun refreshAll(context: Context) {
        PhotoMomentWidget().updateAll(context)
        DailySummaryWidget().updateAll(context)
        PhotoSlideshowWidget().updateAll(context)
        MoodCalendarWidget().updateAll(context)
        QuickMoodWidget().updateAll(context)
    }
}

