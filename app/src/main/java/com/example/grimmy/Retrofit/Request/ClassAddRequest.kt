package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class ClassAddRequest(
    @SerializedName("scheduleId") val scheduleId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("location") val location: String,
    @SerializedName("day") val day: String,       // 요일 문자열 ("월", "화", …)
    @SerializedName("startTime") val startTime: String, // "HH:mm" 형식
    @SerializedName("endTime") val endTime: String    // "HH:mm" 형식
)
