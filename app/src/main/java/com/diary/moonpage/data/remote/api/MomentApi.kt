package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.moment.MomentResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MomentApi {
    @Multipart
    @POST("api/moments")
    suspend fun uploadMoment(
        @Part("DailyLogId") dailyLogId: RequestBody,
        @Part imageFile: MultipartBody.Part,
        @Part("Caption") caption: RequestBody,
        @Part("IsPublic") isPublic: RequestBody,
        @Part("CapturedAt") capturedAt: RequestBody,
        @Part("Location") location: RequestBody?,
        @Part("Weather") weather: RequestBody?,
        @Part("Rating") rating: RequestBody?
    ): Response<MomentResponse>

    @GET("api/moments/me")
    suspend fun getMyMoments(): Response<List<MomentResponse>>

    @GET("api/moments/{id}")
    suspend fun getMoment(@Path("id") id: String): Response<MomentResponse>

    @DELETE("api/moments/{id}")
    suspend fun deleteMoment(@Path("id") id: String): Response<Unit>
}
