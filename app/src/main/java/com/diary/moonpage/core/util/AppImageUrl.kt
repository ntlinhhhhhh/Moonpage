package com.diary.moonpage.core.util

private const val APP_IMAGE_BASE_URL = "https://hieu-wikipedia.io.vn/"

fun normalizeAppImageUrl(url: String?): String? {
    val raw = url?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return when {
        raw.startsWith("http://", ignoreCase = true) ||
            raw.startsWith("https://", ignoreCase = true) ||
            raw.startsWith("content://", ignoreCase = true) ||
            raw.startsWith("file://", ignoreCase = true) -> raw
        raw.startsWith("/data/") ||
            raw.startsWith("/storage/") ||
            raw.startsWith("/sdcard/") -> "file://$raw"
        else -> APP_IMAGE_BASE_URL + raw.trimStart('/')
    }
}
