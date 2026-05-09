package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.activity.ActivityDto
import retrofit2.Response
import retrofit2.http.GET

interface ActivityApi {
    @GET("api/activities")
    suspend fun getActivities(): Response<List<ActivityDto>>
}
