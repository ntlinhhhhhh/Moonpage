package com.diary.moonpage.domain

import com.diary.moonpage.data.local.entity.ThemeMoodEntity
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.repository.CreateThemePayload
import com.diary.moonpage.domain.repository.MomentRepository
import com.diary.moonpage.domain.repository.StatisticsRepository
import com.diary.moonpage.domain.repository.ThemeRepository
import com.diary.moonpage.domain.usecase.moment.DeleteMomentUseCase
import com.diary.moonpage.domain.usecase.moment.GetMomentUseCase
import com.diary.moonpage.domain.usecase.moment.GetMyMomentsUseCase
import com.diary.moonpage.domain.usecase.moment.UploadMomentUseCase
import com.diary.moonpage.domain.usecase.stats.GetStatisticsSummaryUseCase
import com.diary.moonpage.domain.usecase.theme.BuyThemeUseCase
import com.diary.moonpage.domain.usecase.theme.GetOwnedThemesUseCase
import com.diary.moonpage.domain.usecase.theme.GetThemesUseCase
import com.diary.moonpage.domain.usecase.theme.SetActiveThemeUseCase
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class MomentThemeStatsUseCaseUnitTest {
    @Test
    fun tc04MomentUseCasesDelegateUploadLookupListAndDelete() = runBlocking {
        val uploaded = moment(id = "uploaded", isPublic = false)
        val repository = FakeMomentRepository(
            uploadResult = Result.success(uploaded),
            listResult = Result.success(listOf(moment("m1"), moment("m2"))),
            getResult = Result.success(moment("m1"))
        )
        val image = File("moment.webp")

        val upload = UploadMomentUseCase(repository)(
            dailyLogId = "2026-06-01",
            imageFile = image,
            caption = "",
            isPublic = false,
            capturedAt = "2026-06-01T10:00:00Z",
            location = "Da Nang",
            weather = "Sunny",
            rating = 4.0f
        )
        val list = GetMyMomentsUseCase(repository)()
        val detail = GetMomentUseCase(repository)("m1")
        val delete = DeleteMomentUseCase(repository)("m1")

        assertTrue(upload.isSuccess)
        assertEquals(uploaded, upload.getOrNull())
        assertEquals(
            UploadMomentRequest(
                dailyLogId = "2026-06-01",
                imageFile = image,
                caption = "",
                isPublic = false,
                capturedAt = "2026-06-01T10:00:00Z",
                location = "Da Nang",
                weather = "Sunny",
                rating = 4.0f
            ),
            repository.uploadRequest
        )
        assertEquals(listOf("m1", "m2"), list.getOrNull()?.map { it.id })
        assertEquals("m1", repository.getMomentId)
        assertEquals("m1", detail.getOrNull()?.id)
        assertTrue(delete.isSuccess)
        assertEquals("m1", repository.deletedMomentId)
    }

    @Test
    fun tc05StatisticsSummaryUseCaseDelegatesMonthlyAndYearlyQueries() = runBlocking {
        val response = StatisticsResponse(
            totalLogs = 20,
            totalPhotos = 5,
            currentStreak = 3,
            longestStreak = 10,
            moodDistribution = emptyList(),
            moodFlow = emptyList(),
            bestActivities = emptyList()
        )
        val repository = FakeStatisticsRepository(Response.success(response))

        val monthly = GetStatisticsSummaryUseCase(repository)(year = 2026, month = 6, isMonthly = true)
        val yearly = GetStatisticsSummaryUseCase(repository)(year = 2026, month = null, isMonthly = false)

        assertTrue(monthly.isSuccessful)
        assertEquals(20, monthly.body()?.totalLogs)
        assertTrue(yearly.isSuccessful)
        assertEquals(
            listOf(
                StatisticsQuery(year = 2026, month = 6, isMonthly = true),
                StatisticsQuery(year = 2026, month = null, isMonthly = false)
            ),
            repository.queries
        )
    }

    @Test
    fun tc06ThemeStoreUseCasesDelegateCatalogOwnedBuyAndActivate() = runBlocking {
        val catalog = listOf(theme("theme_default", price = 0), theme("theme_paid", price = 250))
        val owned = listOf(theme("theme_default", price = 0, isOwned = true))
        val repository = FakeThemeRepository(catalog = catalog, owned = owned)

        val allThemes = GetThemesUseCase(repository)()
        val ownedThemes = GetOwnedThemesUseCase(repository)()
        val buyPaid = BuyThemeUseCase(repository)("theme_paid", 250)
        val buyFree = BuyThemeUseCase(repository)("theme_default", 0)
        val activate = SetActiveThemeUseCase(repository)("theme_paid")

        assertTrue(allThemes.isSuccess)
        assertEquals(listOf("theme_default", "theme_paid"), allThemes.getOrNull()?.map { it.id })
        assertTrue(ownedThemes.isSuccess)
        assertEquals(listOf("theme_default"), ownedThemes.getOrNull()?.map { it.id })
        assertTrue(buyPaid.isSuccess)
        assertTrue(buyFree.isSuccess)
        assertEquals(listOf("theme_paid" to 250, "theme_default" to 0), repository.buyRequests)
        assertTrue(activate.isSuccess)
        assertEquals("theme_paid", repository.activeThemeId)
    }

    private class FakeMomentRepository(
        private val uploadResult: Result<Moment> = Result.success(moment("uploaded")),
        private val listResult: Result<List<Moment>> = Result.success(emptyList()),
        private val getResult: Result<Moment> = Result.success(moment("detail"))
    ) : MomentRepository {
        override val moments: Flow<List<Moment>> = flowOf(emptyList())
        override val localPaths: Flow<Map<String, String>> = flowOf(emptyMap())
        var uploadRequest: UploadMomentRequest? = null
        var getMomentId: String? = null
        var deletedMomentId: String? = null

        override suspend fun getMyMoments(): Result<List<Moment>> = listResult

        override suspend fun getMoment(id: String): Result<Moment> {
            getMomentId = id
            return getResult
        }

        override suspend fun deleteMoment(id: String): Result<Unit> {
            deletedMomentId = id
            return Result.success(Unit)
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
            uploadRequest = UploadMomentRequest(
                dailyLogId = dailyLogId,
                imageFile = imageFile,
                caption = caption,
                isPublic = isPublic,
                capturedAt = capturedAt,
                location = location,
                weather = weather,
                rating = rating
            )
            return uploadResult
        }

        override suspend fun clearCache() = Unit
    }

    private class FakeStatisticsRepository(
        private val response: Response<StatisticsResponse>
    ) : StatisticsRepository {
        override val refreshTrigger = MutableSharedFlow<Unit>()
        val queries = mutableListOf<StatisticsQuery>()

        override suspend fun getStatisticsSummary(year: Int, month: Int?, isMonthly: Boolean): Response<StatisticsResponse> {
            queries.add(StatisticsQuery(year, month, isMonthly))
            return response
        }

        override suspend fun getGlobalSummary(): Response<StatisticsResponse> = response

        override suspend fun clearCache() = Unit

        override fun triggerRefresh() {
            refreshTrigger.tryEmit(Unit)
        }
    }

    private class FakeThemeRepository(
        private val catalog: List<Theme>,
        private val owned: List<Theme>
    ) : ThemeRepository {
        override val activeTheme: Flow<Theme?> = flowOf(catalog.firstOrNull())
        override val ownedThemes: Flow<List<Theme>> = flowOf(owned)
        override val allThemes: Flow<List<Theme>> = flowOf(catalog)
        override val myThemes: Flow<List<Theme>> = flowOf(emptyList())
        val buyRequests = mutableListOf<Pair<String, Int?>>()
        var activeThemeId: String? = null

        override suspend fun getAllThemes(): Result<List<Theme>> = Result.success(catalog)

        override suspend fun getOwnedThemes(): Result<List<Theme>> = Result.success(owned)

        override suspend fun getMyThemes(): Result<List<Theme>> = Result.success(emptyList())

        override suspend fun createThemes(themes: List<CreateThemePayload>): Result<Unit> = Result.success(Unit)

        override suspend fun renameTheme(themeId: String, name: String): Result<Unit> = Result.success(Unit)

        override suspend fun buyTheme(themeId: String, price: Int?): Result<Unit> {
            buyRequests.add(themeId to price)
            return Result.success(Unit)
        }

        override suspend fun setActiveTheme(themeId: String): Result<Unit> {
            activeThemeId = themeId
            return Result.success(Unit)
        }

        override suspend fun getMoodsForTheme(themeId: String): List<ThemeMoodEntity> = emptyList()

        override suspend fun getActiveThemeId(): String? = activeThemeId

        override suspend fun clearCache() = Unit
    }

    private data class UploadMomentRequest(
        val dailyLogId: String,
        val imageFile: File,
        val caption: String,
        val isPublic: Boolean,
        val capturedAt: String,
        val location: String?,
        val weather: String?,
        val rating: Float?
    )

    private data class StatisticsQuery(
        val year: Int,
        val month: Int?,
        val isMonthly: Boolean
    )

    private companion object {
        fun moment(id: String, isPublic: Boolean = true): Moment {
            return Moment(
                id = id,
                imageUrl = "https://example.com/$id.jpg",
                caption = "caption",
                capturedAt = "2026-06-01T10:00:00Z",
                isPublic = isPublic
            )
        }

        fun theme(id: String, price: Int, isOwned: Boolean = false): Theme {
            return Theme(
                id = id,
                name = id,
                collection = "Collection",
                price = price,
                thumbnailUrl = null,
                backgroundUrl = null,
                isOwned = isOwned
            )
        }
    }
}
