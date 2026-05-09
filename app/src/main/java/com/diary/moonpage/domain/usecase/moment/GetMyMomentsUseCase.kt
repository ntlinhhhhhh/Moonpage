package com.diary.moonpage.domain.usecase.moment

import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.domain.repository.MomentRepository
import javax.inject.Inject

class GetMyMomentsUseCase @Inject constructor(
    private val repository: MomentRepository
) {
    suspend operator fun invoke(): Result<List<Moment>> {
        return repository.getMyMoments()
    }
}
