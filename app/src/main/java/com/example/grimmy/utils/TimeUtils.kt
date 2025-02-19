package com.example.grimmy.utils

// 📌 요일 문자열("월", "화", "수", "목", "금")을 0~4 인덱스로 변환하는 함수
fun parseDayToIndex(day: String): Int {
    return when(day.trim()) {
        "월요일" -> 0
        "화요일"-> 1
        "수요일"-> 2
        "목요일"-> 3
        "금요일" -> 4
        "토요일"-> 5
        "일요일" -> 6
        else -> 0 // 기본값은 월요일
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
