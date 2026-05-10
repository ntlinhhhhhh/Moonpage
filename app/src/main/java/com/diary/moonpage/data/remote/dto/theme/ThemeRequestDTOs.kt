package com.diary.moonpage.data.remote.dto.theme

import com.google.gson.annotations.SerializedName

data class BuyThemeRequest(
    @SerializedName("themeId") val themeId: String,
    @SerializedName("price") val price: Int
)

data class SetActiveThemeRequest(
    @SerializedName("themeId") val themeId: String
)
