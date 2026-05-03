package com.diary.moonpage.domain.model

data class Theme(
    val id: String,
    val name: String,
    val collection: String,
    val price: Int,
    val isFree: Boolean = price == 0,
    val thumbnailUrl: String?,
    val backgroundUrl: String?,
    val isOwned: Boolean = false,
    val isActive: Boolean = false,
    val description: String? = null,
    val type: ThemeType = ThemeType.THEME,
    val icons: List<String> = emptyList(), // "VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"
    val primaryColor: String? = null,
    val decoration: String = "NONE" // "NONE", "KITTY", "SPROUT", "BLUSHING"
)