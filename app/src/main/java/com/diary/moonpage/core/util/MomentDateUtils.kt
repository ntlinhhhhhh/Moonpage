package com.diary.moonpage.core.util

import com.diary.moonpage.domain.model.Moment
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Moment.resolveLogDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate? {
    dailyLogId?.let { id ->
        parseDatePrefix(id)?.let { return it }
    }

    return runCatching {
        Instant.parse(capturedAt).atZone(zoneId).toLocalDate()
    }.getOrNull()
        ?: runCatching {
            OffsetDateTime.parse(capturedAt).atZoneSameInstant(zoneId).toLocalDate()
        }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(capturedAt, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
        }.getOrNull()
        ?: parseDatePrefix(capturedAt)
}

private fun parseDatePrefix(value: String): LocalDate? {
    if (value.length < 10) return null
    return runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
}
