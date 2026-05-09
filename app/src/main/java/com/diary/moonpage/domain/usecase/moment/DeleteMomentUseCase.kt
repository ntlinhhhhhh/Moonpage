package com.diary.moonpage.domain.usecase.moment

import com.diary.moonpage.domain.repository.MomentRepository
import javax.inject.Inject

class DeleteMomentUseCase @Inject constructor(
    private val repository: MomentRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteMoment(id)
    }
}
