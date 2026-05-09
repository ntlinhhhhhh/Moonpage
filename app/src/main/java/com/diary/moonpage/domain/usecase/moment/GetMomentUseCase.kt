package com.diary.moonpage.domain.usecase.moment

import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.domain.repository.MomentRepository
import javax.inject.Inject

class GetMomentUseCase @Inject constructor(
    private val repository: MomentRepository
) {
    suspend operator fun invoke(id: String): Result<Moment> {
        return repository.getMoment(id)
    }
}
