package com.example.grimmy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // viewPager 초기화 및 어댑터 설정
        viewPager = findViewById(R.id.onboarding_viewPager)
        adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter

        // 슬라이드 비활성화
        viewPager.isUserInputEnabled = false

        // 페이지 변경 시 진행 상태 업데이트 위한 콜백 등록
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 페이지 변경 이벤트 처리 (예: 진행률 업데이트)
                // position 변수는 현재 선택된 페이지의 인덱스임.
            }
        })
    }
}