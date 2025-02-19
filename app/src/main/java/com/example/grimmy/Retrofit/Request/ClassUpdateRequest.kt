package com.example.grimmy.Retrofit.Request

data class ClassUpdateRequest(
    val scheduleId: Int,
    val userId: Int,
    val title: String,
    val location: String,
    val day: String,       // 요일 문자열 ("월", "화", …)
    val startTime: String, // "HH:mm" 형식
    val endTime: String    // "HH:mm" 형식
)
