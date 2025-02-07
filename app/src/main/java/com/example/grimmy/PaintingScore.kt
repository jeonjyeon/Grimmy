package com.example.grimmy

data class PaintingScore(
    val date: String,  // 날짜 (yyyy-MM-dd)
    val scores: List<Float> = emptyList() // 하루 최대 3개의 시험 기록 (없을 수도 있음)
)
