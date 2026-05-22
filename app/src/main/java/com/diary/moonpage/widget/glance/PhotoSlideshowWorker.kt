package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.SharedPreferences
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val SLIDESHOW_PREFS = "photo_slideshow_prefs"
private const val KEY_SLIDESHOW_INDEX = "slideshow_global_index"

/**
 * Reads/writes the slideshow index from SharedPreferences.
 * The widget reads from DataStore state per-instance, but this worker
 * bumps a global counter that the widget uses as a seed.
 */
internal fun getSlideshowIndex(context: Context): Int {
    return context.getSharedPreferences(SLIDESHOW_PREFS, Context.MODE_PRIVATE)
        .getInt(KEY_SLIDESHOW_INDEX, 0)
}

internal fun advanceSlideshowIndex(context: Context, photoCount: Int) {
    val prefs: SharedPreferences = context.getSharedPreferences(SLIDESHOW_PREFS, Context.MODE_PRIVATE)
    val current = prefs.getInt(KEY_SLIDESHOW_INDEX, 0)
    prefs.edit().putInt(KEY_SLIDESHOW_INDEX, (current + 1).mod(photoCount)).apply()
}

@HiltWorker
class PhotoSlideshowWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val dataSource = MoonpageWidgetDataSource(appContext)
            val snapshot = dataSource.loadTodaySnapshot()
            val photoCount = snapshot.photoUris.size

            if (photoCount > 1) {
                advanceSlideshowIndex(appContext, photoCount)
            }

            // Trigger Glance to re-render with the new index
            PhotoSlideshowWidget().updateAll(appContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
