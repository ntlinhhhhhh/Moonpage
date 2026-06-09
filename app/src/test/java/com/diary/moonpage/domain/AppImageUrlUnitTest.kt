package com.diary.moonpage.domain

import com.diary.moonpage.core.util.LEGACY_LOCAL_DAILY_LOG_PHOTO_URL_PREFIX
import com.diary.moonpage.core.util.LOCAL_DAILY_LOG_PHOTO_PREFIX
import com.diary.moonpage.core.util.normalizeAppImageUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppImageUrlUnitTest {
    @Test
    fun normalizeAppImageUrlReturnsNullForBlankValues() {
        assertNull(normalizeAppImageUrl(null))
        assertNull(normalizeAppImageUrl(""))
        assertNull(normalizeAppImageUrl("   "))
    }

    @Test
    fun normalizeAppImageUrlKeepsSupportedAbsoluteSchemes() {
        assertEquals("https://example.com/a.png", normalizeAppImageUrl(" https://example.com/a.png "))
        assertEquals("http://example.com/a.png", normalizeAppImageUrl("http://example.com/a.png"))
        assertEquals("content://media/external/images/1", normalizeAppImageUrl("content://media/external/images/1"))
        assertEquals("file:///storage/emulated/0/a.png", normalizeAppImageUrl("file:///storage/emulated/0/a.png"))
        assertEquals(
            "${LOCAL_DAILY_LOG_PHOTO_PREFIX}2026-06-01/photo.jpg",
            normalizeAppImageUrl("${LOCAL_DAILY_LOG_PHOTO_PREFIX}2026-06-01/photo.jpg")
        )
        assertEquals(
            "${LEGACY_LOCAL_DAILY_LOG_PHOTO_URL_PREFIX}2026-06-01/photo.jpg",
            normalizeAppImageUrl("${LEGACY_LOCAL_DAILY_LOG_PHOTO_URL_PREFIX}2026-06-01/photo.jpg")
        )
    }

    @Test
    fun normalizeAppImageUrlConvertsDevicePathsToFileUris() {
        assertEquals("file:///data/user/0/com.diary.moonpage/files/a.webp", normalizeAppImageUrl("/data/user/0/com.diary.moonpage/files/a.webp"))
        assertEquals("file:///storage/emulated/0/DCIM/a.jpg", normalizeAppImageUrl("/storage/emulated/0/DCIM/a.jpg"))
        assertEquals("file:///sdcard/Pictures/a.jpg", normalizeAppImageUrl("/sdcard/Pictures/a.jpg"))
    }

    @Test
    fun normalizeAppImageUrlPrefixesRelativeBackendPaths() {
        assertEquals("https://hieu-wikipedia.io.vn/images/avatar.png", normalizeAppImageUrl("images/avatar.png"))
        assertEquals("https://hieu-wikipedia.io.vn/images/avatar.png", normalizeAppImageUrl("/images/avatar.png"))
        assertEquals("https://hieu-wikipedia.io.vn/uploads/avatar.png", normalizeAppImageUrl(" uploads/avatar.png "))
    }
}
