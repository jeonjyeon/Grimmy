package com.example.grimmy

import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat

class BirthYearFragment : Fragment() {
    private lateinit var yearPicker: NumberPicker
    private lateinit var nextTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_brith_year, container, false)

        // UI 요소 초기화
        yearPicker = view.findViewById(R.id.year_picker_np)
        nextTextView = view.findViewById(R.id.year_next_btn_tv)

        // NumberPicker 설정
        setupNumberPicker()
//        setInitialButtonState()

        // 초기 상태에서 TextView 비활성화
        nextTextView.isEnabled = false
        nextTextView.alpha = 0.5f // 비활성화된 상태의 시각적 표현

        // NumberPicker 값 변경 리스너 설정
        yearPicker.setOnValueChangedListener { _, _, _ ->
            nextTextView.isEnabled = true
            nextTextView.alpha = 1.0f // 활성화된 상태의 시각적 표현
            nextTextView.setBackgroundResource(R.drawable.bg_color_on)
            nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        }

        // TextView 클릭 이벤트 처리
        nextTextView.setOnClickListener {
            (activity as OnboardingActivity).viewPager.currentItem += 1
        }
        return view
    }

//    // 다음 버튼 초기 설정
//    private fun setInitialButtonState() {
//        nextTextView.setBackgroundResource(R.drawable.bg_color_off)
//        nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray8))
//    }

    private fun setupNumberPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도
        val studentBirthYear = currentYear - 18 // 현재 연도를 가져올 수 있도록 수정 가능
        yearPicker.minValue = currentYear - 50 // 최소값
        yearPicker.maxValue = currentYear // 최대값: 현재 연도
        yearPicker.value = currentYear //기본값: 현재 연도
//        yearPicker.maxValue = currentYear // 최대값: 현재 연도
//        yearPicker.value = studentBirthYear // 기본값: 고3 수험생 출생년도
    }

}