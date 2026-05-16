package com.diary.moonpage.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) {
    suspend fun getCurrentLocation(): Location? {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFineLocationPermission && !hasCoarseLocationPermission) {
            return null
        }
        
        return try {
            // 1. Try last known location first - it's immediate
            val lastLocation = locationClient.lastLocation.await()
            if (lastLocation != null && (System.currentTimeMillis() - lastLocation.time) < 30 * 60 * 1000) {
                // If last location is less than 30 mins old, use it immediately
                return lastLocation
            }

            // 2. If no fresh last location, request a new one but with a strict 3-second timeout
            withTimeoutOrNull(3000) {
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                ).await()
            } ?: lastLocation // Fallback to whatever last location we had if fresh request timed out
        } catch (e: Exception) {
            null
        }
    }
}
