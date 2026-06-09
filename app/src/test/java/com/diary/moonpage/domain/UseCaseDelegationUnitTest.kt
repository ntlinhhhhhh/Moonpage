package com.diary.moonpage.domain

import com.diary.moonpage.data.remote.dto.auth.ForgotPasswordRequestDTO
import com.diary.moonpage.data.remote.dto.auth.GoogleLoginRequestDTO
import com.diary.moonpage.data.remote.dto.auth.LoginRequestDTO
import com.diary.moonpage.data.remote.dto.auth.RegisterRequestDTO
import com.diary.moonpage.data.remote.dto.auth.ResetPasswordRequestDTO
import com.diary.moonpage.data.remote.dto.auth.VerifyOtpRequestDTO
import com.diary.moonpage.data.remote.dto.auth.VerifyOtpResponseDTO
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.repository.AuthRepository
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.domain.usecase.auth.ForgotPasswordUseCase
import com.diary.moonpage.domain.usecase.auth.GoogleLoginUseCase
import com.diary.moonpage.domain.usecase.auth.LoginUseCase
import com.diary.moonpage.domain.usecase.auth.RegisterUserCase
import com.diary.moonpage.domain.usecase.auth.ResetPasswordUseCase
import com.diary.moonpage.domain.usecase.auth.VerifyOtpUseCase
import com.diary.moonpage.domain.usecase.calendar.CreateDailyLogUseCase
import com.diary.moonpage.domain.usecase.calendar.DeleteDailyLogUseCase
import com.diary.moonpage.domain.usecase.calendar.GetDailyLogByDateUseCase
import com.diary.moonpage.domain.usecase.calendar.GetDailyLogsByMonthUseCase
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCaseDelegationUnitTest {
    @Test
    fun loginUseCaseDelegatesRequestToAuthRepository() = runBlocking {
        val user = user()
        val repository = FakeAuthRepository(loginResult = Result.success(user))
        val request = LoginRequestDTO(email = "user@test.com", password = "123456")

        val result = LoginUseCase(repository)(request)

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        assertEquals(request, repository.loginRequest)
    }

    @Test
    fun registerUseCaseDelegatesRequestToAuthRepository() = runBlocking {
        val user = user(name = "New User")
        val repository = FakeAuthRepository(registerResult = Result.success(user))
        val request = RegisterRequestDTO(email = "new@test.com", name = "New User", password = "123456")

        val result = RegisterUserCase(repository)(request)

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        assertEquals(request, repository.registerRequest)
    }

    @Test
    fun googleLoginUseCaseBuildsRequestAndRejectsBlankToken() = runBlocking {
        val repository = FakeAuthRepository(googleLoginResult = Result.success(user()))

        val success = GoogleLoginUseCase(repository)("google-token")
        val failure = GoogleLoginUseCase(repository)("   ")

        assertTrue(success.isSuccess)
        assertEquals(GoogleLoginRequestDTO("google-token"), repository.googleLoginRequest)
        assertTrue(failure.isFailure)
    }

    @Test
    fun forgotPasswordUseCaseTrimsEmailAndRejectsInvalidEmailBeforeRepository() = runBlocking {
        val repository = FakeAuthRepository()
        val invalidRepository = FakeAuthRepository()

        val success = ForgotPasswordUseCase(repository)(" user@test.com ")
        val failure = ForgotPasswordUseCase(invalidRepository)("not-an-email")

        assertTrue(success.isSuccess)
        assertEquals(ForgotPasswordRequestDTO("user@test.com"), repository.forgotPasswordRequest)
        assertTrue(failure.isFailure)
        assertNull(invalidRepository.forgotPasswordRequest)
    }

    @Test
    fun verifyOtpUseCaseValidatesOtpLengthAndDelegatesSixDigitCode() = runBlocking {
        val repository = FakeAuthRepository(verifyOtpResult = Result.success(VerifyOtpResponseDTO("reset-token")))

        val success = VerifyOtpUseCase(repository)("user@test.com", "123456")
        val blank = VerifyOtpUseCase(repository)("user@test.com", "")
        val short = VerifyOtpUseCase(repository)("user@test.com", "12345")

        assertTrue(success.isSuccess)
        assertEquals("reset-token", success.getOrNull()?.resetToken)
        assertEquals(VerifyOtpRequestDTO("user@test.com", "123456"), repository.verifyOtpRequest)
        assertTrue(blank.isFailure)
        assertTrue(short.isFailure)
    }

    @Test
    fun resetPasswordUseCaseRejectsShortPasswordAndDelegatesValidRequest() = runBlocking {
        val repository = FakeAuthRepository()
        val invalidRepository = FakeAuthRepository()

        val success = ResetPasswordUseCase(repository)("user@test.com", "reset-token", "123456")
        val failure = ResetPasswordUseCase(invalidRepository)("user@test.com", "reset-token", "12345")

        assertTrue(success.isSuccess)
        assertEquals(
            ResetPasswordRequestDTO("user@test.com", "reset-token", "123456"),
            repository.resetPasswordRequest
        )
        assertTrue(failure.isFailure)
        assertNull(invalidRepository.resetPasswordRequest)
    }

    @Test
    fun createDailyLogUseCasePassesAllFieldsToRepository() = runBlocking {
        val repository = FakeDailyLogRepository()
        val photo = File("photo.webp")

        val result = CreateDailyLogUseCase(repository)(
            baseMoodId = 5,
            date = "2026-06-01",
            note = "good day",
            sleepHours = 7.5,
            sleepStartTime = "23:00",
            isMenstruation = true,
            menstruationPhase = "period",
            activityIds = listOf("reading", "exercise"),
            dailyPhotos = listOf(photo),
            steps = 10000,
            musicTitle = "Song",
            artistName = "Artist",
            albumArtUrl = "https://example.com/album.png",
            calories = 450,
            distance = 5.6,
            wakeupTime = "06:30",
            weather = "Sunny",
            temperature = 31.0
        )

        assertTrue(result.isSuccess)
        assertEquals(
            CreateDailyLogRequest(
                baseMoodId = 5,
                date = "2026-06-01",
                note = "good day",
                sleepHours = 7.5,
                sleepStartTime = "23:00",
                isMenstruation = true,
                menstruationPhase = "period",
                activityIds = listOf("reading", "exercise"),
                dailyPhotos = listOf(photo),
                steps = 10000,
                musicTitle = "Song",
                artistName = "Artist",
                albumArtUrl = "https://example.com/album.png",
                calories = 450,
                distance = 5.6,
                wakeupTime = "06:30",
                weather = "Sunny",
                temperature = 31.0
            ),
            repository.createRequest
        )
    }

    @Test
    fun dailyLogLookupDeleteAndMonthUseCasesDelegateToRepository() = runBlocking {
        val log = dailyLog(date = "2026-06-01")
        val repository = FakeDailyLogRepository(
            dateLog = log,
            monthLogs = listOf(log, dailyLog(date = "2026-06-02"))
        )

        val byDate = GetDailyLogByDateUseCase(repository)("2026-06-01")
        val byMonth = GetDailyLogsByMonthUseCase(repository)("2026-06").first()
        val delete = DeleteDailyLogUseCase(repository)("2026-06-01")

        assertTrue(byDate.isSuccess)
        assertEquals(log, byDate.getOrNull())
        assertEquals("2026-06-01", repository.requestedDate)
        assertEquals(listOf("2026-06-01", "2026-06-02"), byMonth.map { it.date })
        assertEquals("2026-06", repository.requestedMonth)
        assertTrue(delete.isSuccess)
        assertEquals("2026-06-01", repository.deletedDate)
    }

    @Test
    fun googleLoginUseCaseDoesNotCallRepositoryForBlankToken() = runBlocking {
        val repository = FakeAuthRepository()

        val result = GoogleLoginUseCase(repository)(" ")

        assertTrue(result.isFailure)
        assertNull(repository.googleLoginRequest)
    }

    private class FakeAuthRepository(
        private val loginResult: Result<User> = Result.success(user()),
        private val registerResult: Result<User> = Result.success(user()),
        private val googleLoginResult: Result<User> = Result.success(user()),
        private val forgotPasswordResult: Result<Unit> = Result.success(Unit),
        private val verifyOtpResult: Result<VerifyOtpResponseDTO> = Result.success(VerifyOtpResponseDTO("reset-token")),
        private val resetPasswordResult: Result<Unit> = Result.success(Unit)
    ) : AuthRepository {
        var loginRequest: LoginRequestDTO? = null
        var registerRequest: RegisterRequestDTO? = null
        var googleLoginRequest: GoogleLoginRequestDTO? = null
        var forgotPasswordRequest: ForgotPasswordRequestDTO? = null
        var verifyOtpRequest: VerifyOtpRequestDTO? = null
        var resetPasswordRequest: ResetPasswordRequestDTO? = null

        override suspend fun login(request: LoginRequestDTO): Result<User> {
            loginRequest = request
            return loginResult
        }

        override suspend fun register(request: RegisterRequestDTO): Result<User> {
            registerRequest = request
            return registerResult
        }

        override suspend fun googleLogin(request: GoogleLoginRequestDTO): Result<User> {
            googleLoginRequest = request
            return googleLoginResult
        }

        override suspend fun forgotPassword(request: ForgotPasswordRequestDTO): Result<Unit> {
            forgotPasswordRequest = request
            return forgotPasswordResult
        }

        override suspend fun verifyOtp(request: VerifyOtpRequestDTO): Result<VerifyOtpResponseDTO> {
            verifyOtpRequest = request
            return verifyOtpResult
        }

        override suspend fun resetPassword(request: ResetPasswordRequestDTO): Result<Unit> {
            resetPasswordRequest = request
            return resetPasswordResult
        }
    }

    private class FakeDailyLogRepository(
        private val dateLog: DailyLog = dailyLog(),
        private val monthLogs: List<DailyLog> = emptyList()
    ) : DailyLogRepository {
        var createRequest: CreateDailyLogRequest? = null
        var requestedDate: String? = null
        var requestedMonth: String? = null
        var deletedDate: String? = null

        override fun getAllDailyLogsFlow(): Flow<List<DailyLog>> = flowOf(emptyList())

        override suspend fun createDailyLog(
            baseMoodId: Int,
            date: String,
            note: String?,
            sleepHours: Double?,
            sleepStartTime: String?,
            isMenstruation: Boolean,
            menstruationPhase: String?,
            activityIds: List<String>?,
            dailyPhotos: List<File>?,
            steps: Int?,
            musicTitle: String?,
            artistName: String?,
            albumArtUrl: String?,
            calories: Int?,
            distance: Double?,
            wakeupTime: String?,
            weather: String?,
            temperature: Double?
        ): Result<Unit> {
            createRequest = CreateDailyLogRequest(
                baseMoodId = baseMoodId,
                date = date,
                note = note,
                sleepHours = sleepHours,
                sleepStartTime = sleepStartTime,
                isMenstruation = isMenstruation,
                menstruationPhase = menstruationPhase,
                activityIds = activityIds,
                dailyPhotos = dailyPhotos,
                steps = steps,
                musicTitle = musicTitle,
                artistName = artistName,
                albumArtUrl = albumArtUrl,
                calories = calories,
                distance = distance,
                wakeupTime = wakeupTime,
                weather = weather,
                temperature = temperature
            )
            return Result.success(Unit)
        }

        override suspend fun getDailyLogByDate(date: String): Result<DailyLog> {
            requestedDate = date
            return Result.success(dateLog)
        }

        override suspend fun cacheDailyLog(log: DailyLog) = Unit

        override fun getDailyLogByDateFlow(date: String): Flow<DailyLog?> {
            requestedDate = date
            return flowOf(dateLog)
        }

        override suspend fun deleteDailyLog(date: String): Result<Unit> {
            deletedDate = date
            return Result.success(Unit)
        }

        override fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLog>> {
            requestedMonth = yearMonth
            return flowOf(monthLogs)
        }

        override suspend fun clearCache() = Unit
    }

    private data class CreateDailyLogRequest(
        val baseMoodId: Int,
        val date: String,
        val note: String?,
        val sleepHours: Double?,
        val sleepStartTime: String?,
        val isMenstruation: Boolean,
        val menstruationPhase: String?,
        val activityIds: List<String>?,
        val dailyPhotos: List<File>?,
        val steps: Int?,
        val musicTitle: String?,
        val artistName: String?,
        val albumArtUrl: String?,
        val calories: Int?,
        val distance: Double?,
        val wakeupTime: String?,
        val weather: String?,
        val temperature: Double?
    )

    private companion object {
        fun user(name: String = "Test User"): User {
            return User(
                token = "token",
                userId = "user-1",
                name = name,
                email = "user@test.com",
                avatarUrl = null,
                gender = null,
                birthday = null
            )
        }

        fun dailyLog(date: String = "2026-06-01"): DailyLog {
            return DailyLog(
                id = "log-$date",
                baseMoodId = 4,
                date = date,
                note = null,
                sleepHours = null,
                isMenstruation = false,
                menstruationPhase = null,
                dailyPhotos = emptyList(),
                activityIds = emptyList()
            )
        }
    }
}
