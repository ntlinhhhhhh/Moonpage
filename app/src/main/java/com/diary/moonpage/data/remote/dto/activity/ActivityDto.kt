package com.diary.moonpage.data.remote.dto.activity

import com.diary.moonpage.domain.model.Activity
import com.google.gson.annotations.SerializedName

data class ActivityDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("category") val category: String
) {
    fun toDomain(): Activity {
        return Activity(
            id = id,
            name = name,
            iconUrl = iconUrl,
            category = category
        )
    }
}
