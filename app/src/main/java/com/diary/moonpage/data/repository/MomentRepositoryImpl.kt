package com.diary.moonpage.data.repository

import android.content.Context
import com.diary.moonpage.core.util.MomentManager
import com.diary.moonpage.data.remote.api.MomentApi
import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.domain.repository.MomentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MomentRepositoryImpl @Inject constructor(
    private val api: MomentApi,
    private val momentManager: MomentManager,
    @ApplicationContext private val context: Context
) : MomentRepository {

    override val moments: Flow<List<Moment>> = momentManager.getMoments()
    override val localPaths: Flow<Map<String, String>> = momentManager.getLocalPaths()

    override suspend fun getMyMoments(): Result<List<Moment>> {
        return try {
            val response = api.getMyMoments()
            if (response.isSuccessful && response.body() != null) {
                val momentsList = response.body()!!.map { it.toDomain() }
                momentManager.saveMoments(momentsList)
                Result.success(momentsList)
            } else {
                val cached = moments.first()
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(Exception("Failed to fetch moments"))
                }
            }
        } catch (e: Exception) {
            val cached = moments.first()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getMoment(id: String): Result<Moment> {
        return try {
            val response = api.getMoment(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to fetch moment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMoment(id: String): Result<Unit> {
        return try {
            val currentMoments = moments.first()
            val momentToDelete = currentMoments.find { it.id == id }
            
            val response = api.deleteMoment(id)
            if (response.isSuccessful) {
                // Remove from cache
                val updatedMoments = currentMoments.filter { it.id != id }
                momentManager.saveMoments(updatedMoments)
                
                // Remove from localPaths and delete file
                momentToDelete?.let { 
                    val currentPaths = localPaths.first().toMutableMap()
                    val path = currentPaths.remove(it.imageUrl)
                    momentManager.saveLocalPaths(currentPaths)
                    path?.let { p ->
                        val file = File(p)
                        if (file.exists()) file.delete()
                    }
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete moment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadMoment(
        dailyLogId: String,
        imageFile: File,
        caption: String,
        isPublic: Boolean,
        capturedAt: String,
        location: String?,
        weather: String?,
        rating: Float?
    ): Result<Moment> {
        return try {
            val imagePart = MultipartBody.Part.createFormData(
                "imageFile",
                imageFile.name,
                imageFile.asRequestBody("image/webp".toMediaTypeOrNull())
            )
            
            val dailyLogIdBody = dailyLogId.toRequestBody("text/plain".toMediaTypeOrNull())
            val captionBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())
            val isPublicBody = isPublic.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val capturedAtBody = capturedAt.toRequestBody("text/plain".toMediaTypeOrNull())
            val locationBody = location?.toRequestBody("text/plain".toMediaTypeOrNull())
            val weatherBody = weather?.toRequestBody("text/plain".toMediaTypeOrNull())
            val ratingBody = rating?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadMoment(
                dailyLogIdBody, imagePart, captionBody, isPublicBody, capturedAtBody, locationBody, weatherBody, ratingBody
            )
            if (response.isSuccessful && response.body() != null) {
                val newMoment = response.body()!!.toDomain()
                
                // Save locally
                val fileName = "moment_${newMoment.id}.webp"
                val permanentFile = File(context.filesDir, "moments/$fileName")
                permanentFile.parentFile?.mkdirs()
                imageFile.copyTo(permanentFile, overwrite = true)
                
                // Update cache
                val currentMoments = moments.first()
                momentManager.saveMoments((listOf(newMoment) + currentMoments).distinctBy { it.id })
                momentManager.addLocalPath(newMoment.imageUrl, permanentFile.absolutePath)
                
                Result.success(newMoment)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache() {
        momentManager.clearCache()
    }
}
