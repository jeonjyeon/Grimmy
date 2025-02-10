package com.example.grimmy

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.grimmy.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private var test_state : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater,container,false)
        // ✅ SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.homeProfileUsernameTv.text = nickname ?: "사용자"

        childFragmentManager.beginTransaction().replace(R.id.home_frame,HomeMonthlyFragment()).commit()

        binding.homeWeeklySelectedBtnTv.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.report_frame,ReportEmotionFragment()).commit()
            // 배경 드로어블 변경
            binding.homeWeeklySelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_right_selected_btn) // 선택된 버튼의 배경
            binding.homeWeeklySelectedBtnTv.setTextColor(resources.getColor(R.color.gray9, null))
            binding.homeMonthlySelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_left_non_selected_btn) // 비선택된 버튼의 배경
            binding.homeMonthlySelectedBtnTv.setTextColor(Color.parseColor("#80FFCDA9"))

        }

        binding.homeMonthlySelectedBtnTv.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.report_frame,ReportPaintingFragment()).commit()
            // 배경 드로어블 변경
            binding.homeMonthlySelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_left_selected_btn) // 선택된 버튼의 배경
            binding.homeMonthlySelectedBtnTv.setTextColor(resources.getColor(R.color.gray9, null))
            binding.homeWeeklySelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_right_non_selected_btn) // 비선택된 버튼의 배경
            binding.homeWeeklySelectedBtnTv.setTextColor(Color.parseColor("#80FFCDA9"))
        }

        binding.homeNotifyBtnIv.setOnClickListener{
            // AlarmFragment로 전환
            val alarmFragment = AlarmFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.home_notify_frame, alarmFragment) // fragment_container는 프래그먼트를 표시할 컨테이너의 ID입니다.
                .addToBackStack(null) // 뒤로 가기 스택에 추가
                .commit()
        }

        return binding.root
    }
    // ✅ 저장된 닉네임 가져오기
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }

    // ScrollView의 id가 mainScrollView라고 가정합니다.
    fun scrollToTop() {
        binding.homeFrameSv.smoothScrollTo(0, 0)
    }

}