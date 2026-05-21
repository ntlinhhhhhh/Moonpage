package com.diary.moonpage.core.util

fun normalizeAppImageUrl(url: String?): String? {
    return url?.trim()?.takeIf { it.isNotEmpty() }
}

