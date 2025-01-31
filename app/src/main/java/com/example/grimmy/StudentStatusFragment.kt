package com.example.grimmy

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.grimmy.databinding.FragmentStudentStatusBinding

class StudentStatusFragment : Fragment() {
    private lateinit var binding: FragmentStudentStatusBinding

    private lateinit var statusOptions: List<TextView>
    private var selectedOption: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentStatusBinding.inflate(inflater, container, false)

        // TextView 목록 초기화
        statusOptions = listOf(
            binding.istatusEleBtnTv,
            binding.statusMiddleBtnTv,
            binding.statusHighBtnTv,
            binding.statusQualExamBtnTv,
            binding.statusNRepeatBtnTv,
            binding.statusAdultBtnTv
        )

        // 각 버튼에 클릭 리스너 설정
        statusOptions.forEach { button ->
            button.setOnClickListener { toggleSelection(it as TextView) }
        }


        // 초기 상태: 아무 것도 선택되지 않음
        updateNextButtonState()

        // 각 버튼에 클릭 리스너 설정
        for (button in statusOptions) {
            button.setOnClickListener { toggleSelection(it as TextView) }
        }

        // 초기 상태: 아무 것도 선택되지 않음
        updateNextButtonState()

        // 다음 버튼 클릭 이벤트 처리
        binding.statusNextBtnTv.setOnClickListener {
            (activity as OnboardingActivity).goToNextPage()
        }

        return binding.root
    }

    private fun toggleSelection(selected: TextView) {
        if (selectedOption == selected) {
            clearSelection()
        } else {
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
            setBackgroundResource(R.drawable.bg_circle_selected)
            setTextColor(Color.WHITE)
        }
    }

    private fun updateNextButtonState() {
        with(binding.statusNextBtnTv) {
            isEnabled = selectedOption != null
            setBackgroundResource(
                if (selectedOption != null) R.drawable.bg_color_on else R.drawable.bg_color_off
            )
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (selectedOption != null) R.color.white else R.color.gray8
                )
            )
        }
    }
}