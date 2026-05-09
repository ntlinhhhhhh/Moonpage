package com.diary.moonpage.domain.usecase.moment

import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.domain.repository.MomentRepository
import java.io.File
import javax.inject.Inject

class UploadMomentUseCase @Inject constructor(
    private val repository: MomentRepository
) {
    suspend operator fun invoke(
        dailyLogId: String,
        imageFile: File,
        caption: String,
        isPublic: Boolean,
        capturedAt: String,
        location: String?,
        weather: String?,
        rating: Float?
    ): Result<Moment> {
        return repository.uploadMoment(
            dailyLogId, imageFile, caption, isPublic, capturedAt, location, weather, rating
        )
    }
}
