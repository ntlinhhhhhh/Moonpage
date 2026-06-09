package com.diary.moonpage.core.util

private const val APP_IMAGE_BASE_URL = "https://hieu-wikipedia.io.vn/"
const val LOCAL_DAILY_LOG_PHOTO_PREFIX = "local_daily_photo:"
const val LEGACY_LOCAL_DAILY_LOG_PHOTO_URL_PREFIX = APP_IMAGE_BASE_URL + LOCAL_DAILY_LOG_PHOTO_PREFIX

fun normalizeAppImageUrl(url: String?): String? {
    val raw = url?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return when {
        raw.startsWith("http://", ignoreCase = true) ||
            raw.startsWith("https://", ignoreCase = true) ||
            raw.startsWith("content://", ignoreCase = true) ||
            raw.startsWith("file://", ignoreCase = true) ||
            raw.startsWith(LOCAL_DAILY_LOG_PHOTO_PREFIX) -> raw
        raw.startsWith("/data/") ||
            raw.startsWith("/storage/") ||
            raw.startsWith("/sdcard/") -> "file://$raw"
        else -> APP_IMAGE_BASE_URL + raw.trimStart('/')
    }
}
