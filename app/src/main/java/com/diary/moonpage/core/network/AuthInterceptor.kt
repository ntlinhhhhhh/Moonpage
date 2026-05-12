package com.diary.moonpage.core.network

import com.diary.moonpage.core.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.getToken().first()
        }
        
        val requestBuilder = chain.request().newBuilder()
        val host = chain.request().url.host
        
        if (token != null && host == "hieu-wikipedia.io.vn") {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
