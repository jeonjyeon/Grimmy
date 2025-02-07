package com.example.grimmy

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import com.example.grimmy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavView()

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = com.example.grimmy.R.id.fragment_home
        }
    }

    private fun setBottomNavView() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.grimmy.R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(com.example.grimmy.R.id.main_frame,HomeFragment()).commit()
                    true
                }
                com.example.grimmy.R.id.fragment_goal -> {
                    supportFragmentManager.beginTransaction().replace(com.example.grimmy.R.id.main_frame,GoalFragment()).commit()
                    true
                }
                com.example.grimmy.R.id.fragment_report -> {
                    supportFragmentManager.beginTransaction().replace(com.example.grimmy.R.id.main_frame,ReportFragment()).commit()
                    true
                }
                com.example.grimmy.R.id.fragment_schedule -> {
                    supportFragmentManager.beginTransaction().replace(com.example.grimmy.R.id.main_frame,ScheduleFragment()).commit()
                    true
                }
                com.example.grimmy.R.id.fragment_mypage -> {
                    supportFragmentManager.beginTransaction().replace(com.example.grimmy.R.id.main_frame,MypageFragment()).commit()
                    true
                }

                else -> false
            }
        }
    }

    // 바텀 네비게이션 바 숨기기
    fun hideBottomNav() {
        binding.bottomNav.visibility = View.GONE
    }

    // 바텀 네비게이션 바 보이기
    fun showBottomNav() {
        binding.bottomNav.visibility = View.VISIBLE
    }
}