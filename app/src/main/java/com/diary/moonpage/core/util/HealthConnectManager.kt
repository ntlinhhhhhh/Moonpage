package com.diary.moonpage.core.util

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.PermissionController
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy {
        if (getSdkStatus() == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else null
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    fun getSdkStatus(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    fun isSdkAvailable(): Boolean {
        return getSdkStatus() == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            client.permissionController.getGrantedPermissions()
                .containsAll(permissions)
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermissionsContract() = PermissionController.createRequestPermissionResultContract()

    suspend fun readHealthData(date: LocalDate): HealthData = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext HealthData()

        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)

        try {
            val response = client.aggregate(
                androidx.health.connect.client.request.AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL
                    ),
                    timeRangeFilter = timeRangeFilter
                )
            )
            
            // Read sleep sessions separately as they are read via ReadRecordsRequest
            val sleepRequest = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            val sleepRecords = client.readRecords(sleepRequest).records
            val totalSleepMinutes = sleepRecords.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
            val sleepHours = totalSleepMinutes / 60.0

            // Get earliest sleep start and latest wake up time
            val sleepStartStr = sleepRecords.minByOrNull { it.startTime }?.startTime?.atZone(ZoneId.systemDefault())?.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            val sleepWakeStr = sleepRecords.maxByOrNull { it.endTime }?.endTime?.atZone(ZoneId.systemDefault())?.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))

            HealthData(
                steps = response[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0,
                calories = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toInt() ?: 0,
                distance = (response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0) / 1000.0,
                sleepHours = sleepHours,
                sleepStartTime = sleepStartStr,
                sleepWakeTime = sleepWakeStr
            )
        } catch (e: Exception) {
            android.util.Log.e("HealthConnect", "Data fetch failed", e)
            HealthData()
        }
    }
}

data class HealthData(
    val steps: Int = 0,
    val calories: Int = 0,
    val distance: Double = 0.0,
    val sleepHours: Double = 0.0,
    val sleepStartTime: String? = null,
    val sleepWakeTime: String? = null
)
