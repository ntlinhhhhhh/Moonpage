package com.diary.moonpage.domain.repository

import com.diary.moonpage.domain.model.Activity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    val activities: Flow<List<Activity>>
    suspend fun syncActivities(): Result<Unit>
}
