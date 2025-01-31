package com.example.grimmy

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.example.grimmy.databinding.FragmentScheduleAddClassBinding

class ScheduleAddClassFragment : Fragment() {
    private lateinit var binding: FragmentScheduleAddClassBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleAddClassBinding.inflate(inflater,container,false)

        // "완료" 버튼 클릭 시 동작
        binding.scheduleClassAddOkTv.setOnClickListener {
            // 이전 프래그먼트로 돌아가면서 네비게이션 바 보이기
            requireActivity().supportFragmentManager.popBackStack()
            (activity as MainActivity).showBottomNav()
        }

        return binding.root
    }
    override fun onResume() {
        super.onResume()
        // 프래그먼트가 활성화되면 네비게이션 바 숨기기
        (activity as MainActivity).hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        // 프래그먼트가 비활성화되면 네비게이션 바 보이기
        (activity as MainActivity).showBottomNav()
    }
}