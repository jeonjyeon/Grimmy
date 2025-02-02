package com.example.grimmy

data class ClassSchedule(
    val className: String,  // 수업명
    val classPlace: String,  // 장소
    val day: String,         // 요일
    val startTime: String,   // 시작 시간 (예: "11:00")
    val endTime: String      // 종료 시간 (예: "12:30")
)