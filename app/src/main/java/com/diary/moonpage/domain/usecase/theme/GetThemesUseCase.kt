package com.diary.moonpage.domain.usecase.theme

import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.repository.ThemeRepository
import javax.inject.Inject

class GetThemesUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend operator fun invoke(token: String): Result<List<Theme>> {
        return repository.getAllThemes(token)
    }
}
