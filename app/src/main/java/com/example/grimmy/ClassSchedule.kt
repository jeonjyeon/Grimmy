package com.example.grimmy

import com.google.gson.annotations.SerializedName

data class ClassSchedule(
    @SerializedName("title") val className: String,  // 수업명
    @SerializedName("location") val classPlace: String,  // 장소
    @SerializedName("day") val day: String,         // 요일
    @SerializedName("startTime") val startTime: String,   // 시작 시간 (예: "11:00")
    @SerializedName("endTime") val endTime: String      // 종료 시간 (예: "12:30")
)