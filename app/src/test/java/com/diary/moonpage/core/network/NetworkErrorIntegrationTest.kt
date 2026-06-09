package com.diary.moonpage.core.network

import com.diary.moonpage.core.util.TokenManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class NetworkErrorIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var tokenManager: TokenManager
    private lateinit var authInterceptor: AuthInterceptor
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        // Mock android.util.Log to prevent errors in AuthInterceptor
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0

        mockWebServer = MockWebServer()
        mockWebServer.start()

        tokenManager = mockk(relaxed = true)
        authInterceptor = AuthInterceptor(tokenManager)

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // ----- NHÓM 1: AuthInterceptor (Token Injection) -----

    /**
     * Test: Khi có token hợp lệ, Authorization header phải được gắn vào request gửi đến backend.
     * Nhóm: Network / AuthInterceptor
     */
    @Test
    fun `when token is present, Authorization header is added for backend requests`() = runBlocking {
        // Arrange
        val testToken = "test_token_123"
        coEvery { tokenManager.getToken() } returns flowOf(testToken)

        val request = Request.Builder()
            .url("https://hieu-wikipedia.io.vn/api/test")
            .build()

        var interceptedRequest: Request? = null

        // Dùng interceptor thứ hai làm "điểm chặn" để kiểm tra header
        val captureInterceptor = okhttp3.Interceptor { chain ->
            interceptedRequest = chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val localClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(captureInterceptor)
            .build()

        // Act
        localClient.newCall(request).execute()

        // Assert
        assertEquals("Bearer test_token_123", interceptedRequest?.header("Authorization"))
    }

    /**
     * Test: Khi không có token, request được gửi đi mà không có Authorization header
     * (không ném exception, chỉ log warning).
     * Nhóm: Network / AuthInterceptor
     */
    @Test
    fun `when no token, request proceeds without Authorization header`() = runBlocking {
        // Arrange
        coEvery { tokenManager.getToken() } returns flowOf(null)

        val request = Request.Builder()
            .url("https://hieu-wikipedia.io.vn/api/test")
            .build()

        var interceptedRequest: Request? = null
        val captureInterceptor = okhttp3.Interceptor { chain ->
            interceptedRequest = chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val localClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(captureInterceptor)
            .build()

        // Act
        localClient.newCall(request).execute()

        // Assert: không có Authorization header
        assertEquals(null, interceptedRequest?.header("Authorization"))
    }

    /**
     * Test: Interceptor không được thêm Authorization vào các request đến server bên ngoài (non-backend).
     * Nhóm: Network / AuthInterceptor
     */
    @Test
    fun `when request is to non-backend host, no Authorization header is added`() = runBlocking {
        // Arrange
        val testToken = "test_token_123"
        coEvery { tokenManager.getToken() } returns flowOf(testToken)

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()

        var interceptedRequest: Request? = null
        val captureInterceptor = okhttp3.Interceptor { chain ->
            interceptedRequest = chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val localClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(captureInterceptor)
            .build()

        // Act
        localClient.newCall(request).execute()

        // Assert: token KHÔNG được gắn vào request ngoài backend
        assertEquals(null, interceptedRequest?.header("Authorization"))
    }

    // ----- NHÓM 2: HTTP Error Responses (Server Errors) -----

    /**
     * Test: Khi server trả về 500, client nhận đúng mã lỗi 500.
     * Nhóm: Network / Server Error Handling
     */
    @Test
    fun `when server returns 500, response code is 500`() {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        // Act
        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertEquals(500, response.code)
    }

    /**
     * Test: Khi server trả về 401 Unauthorized, client nhận đúng mã lỗi 401.
     * Nhóm: Network / Server Error Handling
     */
    @Test
    fun `when server returns 401, response code is 401`() {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        // Act
        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertEquals(401, response.code)
    }

    /**
     * Test: Khi server trả về 404 Not Found, client nhận đúng mã lỗi 404.
     * Nhóm: Network / Server Error Handling
     */
    @Test
    fun `when server returns 404, response code is 404`() {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        // Act
        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertEquals(404, response.code)
    }

    /**
     * Test: Khi kết nối bị timeout (response trả về chậm hơn readTimeout),
     * OkHttp phải ném ra SocketTimeoutException.
     * Nhóm: Network / Connection Timeout
     */
    @Test(expected = SocketTimeoutException::class)
    fun `when connection times out, SocketTimeoutException is thrown`() {
        // Arrange: delay response 3s, nhưng readTimeout chỉ là 2s
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("OK")
                .setBodyDelay(3, TimeUnit.SECONDS)
        )

        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        // Act: phải ném SocketTimeoutException
        okHttpClient.newCall(request).execute().use { it.body?.string() }
    }
}
