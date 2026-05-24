package com.diary.moonpage.widget.glance

import android.content.Context
import androidx.glance.appwidget.updateAll

object MoonpageWidgets {
    suspend fun refreshAll(context: Context) {
        PhotoMomentWidget().updateAll(context)
        QuickMoodWidget().updateAll(context)
        WeeklyMoodWidget().updateAll(context)
        MonthlyMoodWidget().updateAll(context)
        DailySummaryWidget().updateAll(context)
    }
}
