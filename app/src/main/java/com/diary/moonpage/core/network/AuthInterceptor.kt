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

        android.util.Log.d("AuthInterceptor", "Token present: ${token != null}")

        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val host = request.url.host

        val allowedHosts = listOf("hieu-wikipedia.io.vn", "spotify.com", "googleusercontent.com")

        val shouldAddToken = allowedHosts.any { host.contains(it) }

        if (token != null && shouldAddToken) {
            if (request.header("Authorization") == null) {
                android.util.Log.d("AuthInterceptor", "Adding Auth header to: ${request.url}")
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        } else if (token == null) {
            android.util.Log.w("AuthInterceptor", "No token for request: ${request.url}")
        }

        return chain.proceed(requestBuilder.build())
    }
}