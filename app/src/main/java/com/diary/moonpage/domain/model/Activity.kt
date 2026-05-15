package com.diary.moonpage.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Activity(
    val id: String,
    val name: String,
    val iconUrl: String,
    val category: String
)
