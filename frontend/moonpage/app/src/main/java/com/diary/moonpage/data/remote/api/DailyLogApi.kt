package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DailyLogApi {

    @Multipart
    @POST("api/dailylogs")
    suspend fun createDailyLog(
        @Part("BaseMoodId") baseMoodId: RequestBody,
        @Part("Date") date: RequestBody,
        @Part("Note") note: RequestBody?,
        @Part("SleepHours") sleepHours: RequestBody?,
        @Part("IsMenstruation") isMenstruation: RequestBody?,
        @Part("MenstruationPhase") menstruationPhase: RequestBody?,
        @Part activityIds: List<MultipartBody.Part>?, 
        @Part dailyPhotos: List<MultipartBody.Part>? 
    ): Response<Unit>

    @GET("api/dailylogs/date/{date}")
    suspend fun getDailyLogByDate(@Path("date") date: String): Response<DailyLogResponseDto>

    @DELETE("api/dailylogs/date/{date}")
    suspend fun deleteDailyLog(@Path("date") date: String): Response<Unit>

    @GET("api/dailylogs/month/{yearMonth}")
    suspend fun getDailyLogsByMonth(@Path("yearMonth") yearMonth: String): Response<List<DailyLogResponseDto>>
}
