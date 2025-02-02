package com.example.grimmy

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.grimmy.databinding.FragmentBrithYearBinding
import com.example.grimmy.viewmodel.UserViewModel

class BirthYearFragment : Fragment() {
    private lateinit var binding: FragmentBrithYearBinding
    private val userViewModel: UserViewModel by activityViewModels() // ViewModel 사용

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrithYearBinding.inflate(inflater, container, false)

        // 사용자 정보를 관찰
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            // user 객체가 null이 아닐 때만 접근
            user?.let {
                binding.yearNicknameTv.text = it.nickname // 닉네임 표시
//                binding.textViewBirthYear.text = it.birthYear // 출생년도 표시
//                binding.textViewStudentStatus.text = it.studentStatus // 학생 상태 표시
//                binding.textViewExamType.text = it.examType // 시험 타입 표시
            }
        }

        // NumberPicker 설정
        setupNumberPicker()

        // 초기 상태에서 TextView 비활성화
        binding.yearNextBtnTv.isEnabled = false
        binding.yearNextBtnTv.alpha = 0.5f // 비활성화된 상태의 시각적 표현

        // NumberPicker 값 변경 리스너 설정
        binding.yearPickerNp.setOnValueChangedListener { _, _, _ ->
            binding.yearNextBtnTv.isEnabled = true
            binding.yearNextBtnTv.alpha = 1.0f // 활성화된 상태의 시각적 표현
            binding.yearNextBtnTv.setBackgroundResource(R.drawable.bg_ok_btn)
            binding.yearNextBtnTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        }

        // TextView 클릭 이벤트 처리
        binding.yearNextBtnTv.setOnClickListener {
            // ViewModel에 출생 년도 저장
            userViewModel.setBirthYear(binding.yearPickerNp.value.toString())

            // 서버에 출생 년도 저장

            // 잠시 대기 후 다음 페이지로 넘어가기
            binding.yearNextBtnTv.postDelayed({
                (activity as OnboardingActivity).goToNextPage()
            }, 300) // 300ms 지연
        }
        return binding.root
    }

    private fun setupNumberPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도
//        val studentBirthYear = currentYear - 18 // 현재 연도를 가져올 수 있도록 수정 가능
        binding.yearPickerNp.minValue = currentYear - 50 // 최소값
        binding.yearPickerNp.maxValue = currentYear // 최대값: 현재 연도
        binding.yearPickerNp.value = currentYear //기본값: 현재 연도
//        yearPicker.maxValue = currentYear // 최대값: 현재 연도
//        yearPicker.value = studentBirthYear // 기본값: 고3 수험생 출생년도
    }

}