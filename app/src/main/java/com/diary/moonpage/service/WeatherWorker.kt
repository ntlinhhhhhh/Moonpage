package com.diary.moonpage.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diary.moonpage.domain.usecase.notification.CheckAndTriggerNotificationsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val checkAndTriggerNotificationsUseCase: CheckAndTriggerNotificationsUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            checkAndTriggerNotificationsUseCase()
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WeatherWorker", "Error executing weather check", e)
            Result.retry()
        }
    }
}
