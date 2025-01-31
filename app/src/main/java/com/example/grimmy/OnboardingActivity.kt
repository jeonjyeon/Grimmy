package com.example.grimmy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.grimmy.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager 초기화 및 어댑터 설정
        adapter = OnboardingPagerAdapter(this)
        binding.onboardingViewPager.adapter = adapter

        // 페이지 슬라이드 허용
        binding.onboardingViewPager.isUserInputEnabled = true // 필요에 따라 조정

        // 페이지 변경 시 진행 상태 업데이트 위한 콜백 등록
        binding.onboardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 페이지 변경 이벤트 처리 (예: 진행률 업데이트)
                // position 변수를 사용하여 진행 상태를 업데이트할 수 있습니다.
            }
        })
    }

    // ViewPager의 현재 아이템을 변경하는 메서드
    fun goToNextPage() {
        // 현재 페이지가 마지막 페이지가 아닌 경우에만 페이지 전환
        if (binding.onboardingViewPager.currentItem < adapter.itemCount - 1) {
            binding.onboardingViewPager.currentItem += 1
        }
    }
}
