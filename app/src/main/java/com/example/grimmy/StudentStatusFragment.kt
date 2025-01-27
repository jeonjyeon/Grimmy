package com.example.grimmy

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.grimmy.R

class StudentStatusFragment : Fragment() {

    private lateinit var statusOptions: List<TextView>
    private var selectedOption: TextView? = null
    private lateinit var nextTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_status, container, false)

        // TextView 목록 초기화
        val eleButton: TextView = view.findViewById(R.id.istatus_ele_btn_tv)
        val middleButton: TextView = view.findViewById(R.id.status_middle_btn_tv)
        val highButton: TextView = view.findViewById(R.id.status_high_btn_tv)
        val qualExamButton: TextView = view.findViewById(R.id.status_qual_exam_btn_tv)
        val nRepeatButton: TextView = view.findViewById(R.id.status_n_repeat_btn_tv)
        val adultButton: TextView = view.findViewById(R.id.status_adult_btn_tv)

        statusOptions = listOf(eleButton, middleButton, highButton, qualExamButton, nRepeatButton, adultButton)

        // 다음 버튼 초기화
        nextTextView = view.findViewById(R.id.status_next_btn_tv)

        // 각 버튼에 클릭 리스너 설정
        for (button in statusOptions) {
            button.setOnClickListener { toggleSelection(it as TextView) }
        }

        // 초기 상태: 아무 것도 선택되지 않음
        updateNextButtonState()

        // TextView 클릭 이벤트 처리
        nextTextView.setOnClickListener {
            (activity as OnboardingActivity).viewPager.currentItem += 1
        }

        return view
    }

    private fun toggleSelection(selected: TextView) {
        // 동일 버튼 클릭 시 선택 해제
        if (selectedOption == selected) {
            clearSelection()
        } else {
            // 새로운 옵션 선택
            clearSelection()
            setSelected(selected)
        }
        updateNextButtonState()
    }

    private fun clearSelection() {
        selectedOption?.apply {
            setBackgroundResource(R.drawable.bg_circle)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.font3))
        }
        selectedOption = null
    }

    private fun setSelected(selected: TextView) {
        selectedOption = selected
        selected.apply {
            setBackgroundResource(R.drawable.bg_circle_selected) // 선택된 상태의 배경 drawable
            setTextColor(Color.WHITE)
        }
    }

    private fun updateNextButtonState() {
        if (::nextTextView.isInitialized) {
            if (selectedOption != null) {
                nextTextView.isEnabled = true
                nextTextView.setBackgroundResource(R.drawable.bg_color_on) // 활성화된 상태의 배경 drawable
                nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                nextTextView.isEnabled = false
                nextTextView.setBackgroundResource(R.drawable.bg_color_off) // 비활성화된 상태의 배경 drawable
                nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray8))
            }
        }
    }
}