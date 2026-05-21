package com.diary.moonpage.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.diary.moonpage.R
import com.diary.moonpage.domain.usecase.notification.CheckAndTriggerNotificationsUseCase
import com.diary.moonpage.ui.MainActivity
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
            checkAndTriggerNotificationsUseCase.checkWeatherOnly()
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WeatherWorker", "Error executing weather check", e)
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val channelId = "moonpage_weather_work_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Moonpage Weather Checks",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background checks for rain alerts"
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Moonpage")
            .setContentText("Checking weather alerts")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        return ForegroundInfo(2002, notification)
    }
}
