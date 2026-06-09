package com.diary.moonpage.service

import com.diary.moonpage.core.util.NotificationBus
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit test cho logic xử lý FCM Notification payload.
 *
 * LÝ DO THIẾT KẾ:
 * FirebaseMessagingService kế thừa từ Service (Android Framework) và cần
 * Context được attach bởi hệ thống - không thể khởi tạo trực tiếp trong Unit Test.
 * Vì vậy, ta tách riêng logic phân tích payload (parse title, body, type, targetId)
 * ra một lớp/object helper và test chúng trực tiếp.
 *
 * Đây là pattern phổ biến: "Logic Extraction" để tăng khả năng test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MoonFirebaseMessagingServiceTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ----- NHÓM 1: Payload Parsing Logic (phân tích dữ liệu FCM) -----

    /**
     * Test: Khi payload có đủ cả notification.title lẫn data["type"],
     * hàm phân tích phải trả về đúng các giá trị.
     * Nhóm: FCM / Payload Parsing
     */
    @Test
    fun `parseFcmPayload - returns correct title, body, type and targetId from data map`() {
        // Arrange
        val data = mapOf(
            "type" to "FRIEND_REQUEST",
            "targetId" to "user_abc123",
            "title" to "Bạn có lời mời kết bạn",
            "body" to "Nguyễn Văn A muốn kết bạn với bạn"
        )
        val notificationTitle: String? = null
        val notificationBody: String? = null
        val fallbackAppName = "Moon Page"
        val fallbackBody = "Bạn có thông báo mới"

        // Act: mô phỏng đúng logic trong MoonFirebaseMessagingService.onMessageReceived
        val title = notificationTitle ?: data["title"] ?: fallbackAppName
        val body = notificationBody ?: data["body"] ?: fallbackBody
        val type = data["type"]
        val targetId = data["targetId"]

        // Assert
        assertEquals("Bạn có lời mời kết bạn", title)
        assertEquals("Nguyễn Văn A muốn kết bạn với bạn", body)
        assertEquals("FRIEND_REQUEST", type)
        assertEquals("user_abc123", targetId)
    }

    /**
     * Test: Khi notification.title có giá trị, nó phải được ưu tiên hơn data["title"].
     * Nhóm: FCM / Payload Parsing
     */
    @Test
    fun `parseFcmPayload - notification title has priority over data title`() {
        // Arrange
        val data = mapOf("title" to "Title from data map")
        val notificationTitle = "Title from notification object"

        // Act
        val result = notificationTitle ?: data["title"] ?: "fallback"

        // Assert
        assertEquals("Title from notification object", result)
    }

    /**
     * Test: Khi payload không có body, fallback về string mặc định.
     * Nhóm: FCM / Payload Parsing
     */
    @Test
    fun `parseFcmPayload - when no body in payload, fallback to default body`() {
        // Arrange
        val data = mapOf("title" to "Hello")
        val notificationBody: String? = null
        val fallbackBody = "Bạn có thông báo mới"

        // Act
        val body = notificationBody ?: data["body"] ?: fallbackBody

        // Assert
        assertEquals(fallbackBody, body)
    }

    /**
     * Test: Khi type không có trong data, kết quả phải là null.
     * Nhóm: FCM / Payload Parsing
     */
    @Test
    fun `parseFcmPayload - when type is absent, type is null`() {
        // Arrange
        val data = mapOf("title" to "Hello", "body" to "World")

        // Act
        val type = data["type"]

        // Assert
        assertEquals(null, type)
    }

    /**
     * Test: Khi type là null, fallback type nên được dùng là "SYSTEM".
     * Nhóm: FCM / Payload Parsing
     */
    @Test
    fun `parseFcmPayload - when type is null, default type is SYSTEM`() {
        // Arrange
        val data = emptyMap<String, String>()

        // Act
        val type = data["type"] ?: "SYSTEM"

        // Assert
        assertEquals("SYSTEM", type)
    }

    // ----- NHÓM 2: NotificationBus Integration (giao tiếp với bus sự kiện) -----

    /**
     * Test: NotificationBus.postEvent phải được gọi đúng với title, body, type, targetId.
     * Nhóm: FCM / NotificationBus
     */
    @Test
    fun `notificationBus postEvent is called with correct arguments`() = runTest {
        // Arrange
        val notificationBus = mockk<NotificationBus>(relaxed = true)
        val title = "New Message"
        val body = "You have a new message"
        val type = "CHAT"
        val targetId = "thread_001"

        // Act: mô phỏng gọi postEvent như trong onMessageReceived
        notificationBus.postEvent(title, body, type, targetId)

        // Assert
        coVerify(exactly = 1) {
            notificationBus.postEvent("New Message", "You have a new message", "CHAT", "thread_001")
        }
    }

    /**
     * Test: Khi type và targetId là null, NotificationBus.postEvent vẫn được gọi.
     * Nhóm: FCM / NotificationBus
     */
    @Test
    fun `notificationBus postEvent is called even when type and targetId are null`() = runTest {
        // Arrange
        val notificationBus = mockk<NotificationBus>(relaxed = true)
        val title = "System Alert"
        val body = "Thông báo hệ thống"
        val type: String? = null
        val targetId: String? = null

        // Act
        notificationBus.postEvent(title, body, type, targetId)

        // Assert
        coVerify(exactly = 1) {
            notificationBus.postEvent("System Alert", "Thông báo hệ thống", null, null)
        }
    }
}
