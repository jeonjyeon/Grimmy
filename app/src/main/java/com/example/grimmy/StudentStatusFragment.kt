package com.example.grimmy

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.grimmy.databinding.FragmentStudentStatusBinding
import com.example.grimmy.viewmodel.UserViewModel

class StudentStatusFragment : Fragment() {
    private lateinit var binding: FragmentStudentStatusBinding
    private val userViewModel: UserViewModel by activityViewModels() // ViewModel 사용

    private lateinit var statusOptions: List<TextView>
    private var selectedOption: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentStatusBinding.inflate(inflater, container, false)
        // 사용자 정보를 관찰

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            // user 객체가 null이 아닐 때만 접근
            user?.let {
                binding.statusNicknameTv.text = it.nickname // 닉네임 표시
//                binding.textViewBirthYear.text = it.birthYear // 출생년도 표시
//                binding.textViewStudentStatus.text = it.studentStatus // 학생 상태 표시
//                binding.textViewExamType.text = it.examType // 시험 타입 표시
            }
        }

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
            // 선택된 버튼의 텍스트를 가져와서 학생 상태로 설정
            selectedOption?.let { selected ->
                val studentStatus = selected.text.toString() // 선택된 TextView의 텍스트를 가져옴
                userViewModel.setStudentStatus(studentStatus) // ViewModel에 학생 상태 저장
            }
            // 잠시 대기 후 다음 페이지로 넘어가기
            binding.statusNextBtnTv.postDelayed({
                (activity as OnboardingActivity).goToNextPage()
            }, 350) // 300ms 지연
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
            setBackgroundResource(R.drawable.bg_circle_select)
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