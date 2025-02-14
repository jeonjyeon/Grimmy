package com.example.grimmy.utils

// 📌 요일 문자열("월", "화", "수", "목", "금")을 0~4 인덱스로 변환하는 함수
fun parseDayToIndex(dayStr: String): Int {
    return when (dayStr.trim()) {
        "월" -> 0
        "화" -> 1
        "수" -> 2
        "목" -> 3
        "금" -> 4
        else -> -1 // 잘못된 값일 경우 -1 반환
    }
}

// 📌 "HH:mm" 형식을 분 단위(Int)로 변환하는 함수
fun parseTimeToMinutes(timeStr: String): Int {
    val parts = timeStr.split(":")
    if (parts.size != 2) return -1
    val hour = parts[0].toIntOrNull() ?: return -1
    val minute = parts[1].toIntOrNull() ?: return -1
    return hour * 60 + minute
}
