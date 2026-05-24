package com.diary.moonpage.data.remote.dto.theme

import com.google.gson.annotations.SerializedName

data class BuyThemeRequest(
    @SerializedName("themeId") val themeId: String,
    @SerializedName("price") val price: Int
)

data class SetActiveThemeRequest(
    @SerializedName("themeId") val themeId: String
)

data class CreateThemeRequest(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("backgroundUrl") val backgroundUrl: String? = null,
    @SerializedName("backgroundDarkColor") val backgroundDarkColor: String? = null,
    @SerializedName("backgroundLightColor") val backgroundLightColor: String? = null,
    @SerializedName("isOfficial") val isOfficial: Boolean = false,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("moods") val moods: List<CreateThemeMoodRequest>
)

data class CreateThemeMoodRequest(
    @SerializedName("baseMoodId") val baseMoodId: Int,
    @SerializedName(value = "iconColor", alternate = ["iconUrl"]) val iconUrl: String,
    @SerializedName("customName") val customName: String
)

data class CreateThemeResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)
