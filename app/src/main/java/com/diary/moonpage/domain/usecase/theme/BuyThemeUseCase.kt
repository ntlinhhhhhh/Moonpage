package com.diary.moonpage.domain.usecase.theme

import com.diary.moonpage.domain.repository.ThemeRepository
import javax.inject.Inject

class BuyThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend operator fun invoke(token: String, themeId: String): Result<Unit> {
        return repository.buyTheme(token, themeId)
    }
}
