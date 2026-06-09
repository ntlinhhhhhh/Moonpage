package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
import com.diary.moonpage.ui.MainActivity
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
}
