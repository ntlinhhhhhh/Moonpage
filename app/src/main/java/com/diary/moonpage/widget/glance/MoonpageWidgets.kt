package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.diary.moonpage.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

object MoonpageWidgets {
    const val EXTRA_TARGET_ROUTE = "com.diary.moonpage.widget.TARGET_ROUTE"
    const val ROUTE_CALENDAR = "calendar_screen"
    const val ROUTE_CAMERA = "camera_screen"
    const val ROUTE_STATS_MOOD = "stats_mood_detail_screen"

    private val widgetUpdateScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var updateJob: Job? = null
    private var refreshRequested = false
    private val updateLock = Any()

    fun todayLogRoute(): String = "daily_log_screen/${LocalDate.now()}"

    fun openAppIntent(context: Context, targetRoute: String): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_ROUTE, targetRoute)
        }
    }

    fun refreshAll(context: Context) {
        val appContext = context.applicationContext

        synchronized(updateLock) {
            refreshRequested = true
            if (updateJob?.isActive == true) return

            updateJob = widgetUpdateScope.launch {
                runRefreshLoop(appContext)
            }
        }
    }

    private suspend fun runRefreshLoop(context: Context) {
        while (true) {
            // Debounce rapid consecutive calls to prevent overlapping WorkManager sessions.
            delay(800)

            synchronized(updateLock) {
                refreshRequested = false
            }

            android.util.Log.d("MoonpageWidgets", "Starting sequential widget refresh...")

            safeUpdate(context, PhotoMomentWidget())
            delay(200)
            safeUpdate(context, QuickMoodWidget())
            delay(200)
            safeUpdate(context, WeeklyMoodWidget())
            delay(200)
            safeUpdate(context, MonthlyMoodWidget())
            delay(200)
            safeUpdate(context, DailySummaryWidget())

            android.util.Log.d("MoonpageWidgets", "Sequential widget refresh completed.")

            val shouldRunAgain = synchronized(updateLock) {
                if (refreshRequested) {
                    true
                } else {
                    updateJob = null
                    false
                }
            }
            if (!shouldRunAgain) return
        }
    }

    private suspend fun safeUpdate(context: Context, widget: GlanceAppWidget) {
        try {
            updateGlance(context, widget)
        } catch (e: CancellationException) {
            // Normal cancellation from a newer job - rethrow to stop this specific job
            throw e
        } catch (e: Exception) {
            android.util.Log.e("MoonpageWidgets", "Failed to update ${widget::class.java.simpleName}", e)
        }
    }

    private suspend fun updateGlance(context: Context, widget: GlanceAppWidget) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(widget::class.java)
        
        glanceIds.forEach { id ->
            widget.update(context, id)
        }
    }
}
