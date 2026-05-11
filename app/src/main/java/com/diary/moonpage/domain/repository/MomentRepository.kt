package com.diary.moonpage.domain.repository

import com.diary.moonpage.domain.model.Moment
import kotlinx.coroutines.flow.Flow
import java.io.File

interface MomentRepository {
    val moments: Flow<List<Moment>>
    val localPaths: Flow<Map<String, String>>

    suspend fun getMyMoments(): Result<List<Moment>>
    suspend fun getMoment(id: String): Result<Moment>
    suspend fun deleteMoment(id: String): Result<Unit>
    suspend fun uploadMoment(
        dailyLogId: String,
        imageFile: File,
        caption: String,
        isPublic: Boolean,
        capturedAt: String,
        location: String?,
        weather: String?,
        rating: Float?
    ): Result<Moment>

    suspend fun clearCache()
}
