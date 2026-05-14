package com.diary.moonpage.core.util

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.PermissionController
import dagger.hilt.android.qualifiers.ApplicationContext
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
        HealthPermission.getReadPermission(DistanceRecord::class)
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

    suspend fun readHealthData(date: LocalDate): HealthData {
        val client = healthConnectClient ?: return HealthData()

        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val stepsRequest = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
        )
        val steps = client.readRecords(stepsRequest).records.sumOf { it.count }

        val caloriesRequest = ReadRecordsRequest(
            recordType = TotalCaloriesBurnedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
        )
        val calories = client.readRecords(caloriesRequest).records.sumOf { it.energy.inKilocalories }

        val distanceRequest = ReadRecordsRequest(
            recordType = DistanceRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
        )
        val distance = client.readRecords(distanceRequest).records.sumOf { it.distance.inMeters }

        return HealthData(
            steps = steps.toInt(),
            calories = calories.toInt(),
            distance = (distance / 1000.0) // Convert to km
        )
    }
}

data class HealthData(
    val steps: Int = 0,
    val calories: Int = 0,
    val distance: Double = 0.0
)
