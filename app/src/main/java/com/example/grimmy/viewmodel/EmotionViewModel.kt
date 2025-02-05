package com.example.grimmy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grimmy.Emotion
import com.example.grimmy.R

class EmotionViewModel : ViewModel() {
    private val _emotionData = MutableLiveData<List<Emotion>>()
    val emotionData: LiveData<List<Emotion>> get() = _emotionData

    fun fetchEmotionData() {
        // ⚠️ 서버에서 데이터를 받아온다고 가정 (현재는 테스트용)
        val emotions = listOf(
            Emotion("기쁨", 40f, R.drawable.img_emotion_happy),
            Emotion("슬픔", 10f, R.drawable.img_emotion_sad),
            Emotion("분노", 50f, R.drawable.img_emotion_angry),
            Emotion("집중", 0f, R.drawable.img_emotion_lightening),
            Emotion("사랑", 0f, R.drawable.img_emotion_love),
            Emotion("피곤", 0f, R.drawable.img_emotion_sleepy)
        )

        // 전체 개수 계산
        val totalCount = emotions.sumOf { it.value.toDouble() }

        // value가 0이 아닌 감정만 필터링
        val nonZeroEmotions = emotions.filter { it.value > 0 }

        // 감정 데이터가 없거나 모든 감정의 value가 0인지 체크
        if (nonZeroEmotions.isEmpty()) {
            // 모든 감정의 value가 0이면 회색 원 표시
            _emotionData.value = listOf(Emotion("데이터 없음", 100f, R.drawable.img_default_profile)) // 회색 원 아이콘
        } else {
            // 각 감정의 퍼센트 계산
            val percentEmotions = nonZeroEmotions.map { entry ->
                Emotion(entry.name,(entry.value / totalCount.toFloat()) * 100, entry.iconRes) // 퍼센트로 변환
            }

            // 감정 데이터를 ㄱㄴㄷ 순으로 정렬
            val sortedEmotions = percentEmotions.sortedBy { it.name }
            _emotionData.value = sortedEmotions
        }
    }
}
