package com.example.grimmy

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4 // 온보딩 단계 수

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NicknameFragment()
            1 -> YearFragment()
            2 -> IdentityFragment()
            3 -> TypeFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}