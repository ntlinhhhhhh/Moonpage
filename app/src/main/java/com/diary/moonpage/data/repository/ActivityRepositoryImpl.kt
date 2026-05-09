package com.diary.moonpage.data.repository

import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.data.remote.api.ActivityApi
import com.diary.moonpage.domain.model.Activity
import com.diary.moonpage.domain.repository.ActivityRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityApi: ActivityApi,
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val gson: Gson
) : ActivityRepository {

    override val activities: Flow<List<Activity>> = activityPreferencesManager.activities

    override suspend fun syncActivities(): Result<Unit> {
        return try {
            val response = activityApi.getActivities()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val activities = dtos.map { it.toDomain() }
                // Convert list to JSON string and save
                val json = gson.toJson(activities)
                activityPreferencesManager.saveActivities(json)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch activities"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
