package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class ClassAddResponse(
    @SerializedName("scheduleDetailId") val scheduleDetailId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("location") val location: String,
    @SerializedName("day") val day: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)
