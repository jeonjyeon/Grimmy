package com.example.grimmy

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4 // 온보딩 단계 수

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NicknameFragment()
            1 -> BirthYearFragment()
            2 -> StudentStatusFragment()
            3 -> ExamTypeFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}