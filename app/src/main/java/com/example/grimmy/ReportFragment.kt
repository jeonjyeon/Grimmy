package com.example.grimmy

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.grimmy.databinding.FragmentReportBinding

class ReportFragment : Fragment() {

    private lateinit var binding : FragmentReportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(inflater,container,false)

        childFragmentManager.beginTransaction().replace(R.id.report_frame,ReportPaintingFragment()).commit()

        binding.reportEmotionSelectedBtnTv.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.report_frame,ReportEmotionFragment()).commit()
            // 배경 드로어블 변경
            binding.reportEmotionSelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_right_selected_btn) // 선택된 버튼의 배경
            binding.reportEmotionSelectedBtnTv.setTextColor(resources.getColor(R.color.gray9, null))
            binding.reportPaintingSelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_left_non_selected_btn) // 비선택된 버튼의 배경
            binding.reportPaintingSelectedBtnTv.setTextColor(Color.parseColor("#FFCDA9"))

        }

        binding.reportPaintingSelectedBtnTv.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.report_frame,ReportPaintingFragment()).commit()
            // 배경 드로어블 변경
            binding.reportPaintingSelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_left_selected_btn) // 선택된 버튼의 배경
            binding.reportPaintingSelectedBtnTv.setTextColor(resources.getColor(R.color.gray9, null))
            binding.reportEmotionSelectedBtnTv.setBackgroundResource(R.drawable.bg_tab_right_non_selected_btn) // 비선택된 버튼의 배경
            binding.reportEmotionSelectedBtnTv.setTextColor(Color.parseColor("#FFCDA9"))
        }

        binding.reportNotifyBtnIv.setOnClickListener{
            // AlarmFragment로 전환
            val alarmFragment = AlarmFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.report_notify_frame, alarmFragment) // fragment_container는 프래그먼트를 표시할 컨테이너의 ID입니다.
                .addToBackStack(null) // 뒤로 가기 스택에 추가
                .commit()
        }
        return binding.root
    }

}