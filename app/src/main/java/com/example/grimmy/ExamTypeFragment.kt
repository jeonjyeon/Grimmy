package com.example.grimmy

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.grimmy.databinding.FragmentExamTypeBinding
import com.example.grimmy.viewmodel.UserViewModel

class ExamTypeFragment : Fragment() {
    private lateinit var binding: FragmentExamTypeBinding
    private val userViewModel: UserViewModel by activityViewModels() // ViewModel 사용

    private val typeOptions = mutableSetOf<String>() // 선택된 유형을 저장할 Set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExamTypeBinding.inflate(inflater, container, false)

        // 사용자 정보를 관찰
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            // user 객체가 null이 아닐 때만 접근
            user?.let {
                binding.typeNicknameTv.text = it.nickname // 닉네임 표시
            }
        }

        // 각 버튼에 클릭 리스너 설정
        setButtonClickListeners()
        // 초기 상태: 아무 것도 선택되지 않음
        updateNextButtonState()


        // "다음" 버튼 클릭 이벤트 처리
        binding.typeNextBtnTv.setOnClickListener {
            userViewModel.setExamType(typeOptions.toList()) // 선택된 유형을 ViewModel에 저장

            if (typeOptions.isNotEmpty()) { // 최소 1개 이상 선택해야 이동
                if (binding.typeNextBtnTv.text == "시작하기") {
                    // "시작하기" 버튼이 눌리면 MainActivity로 이동
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish() // OnboardingActivity 종료
                }
            }
        }
        return binding.root
    }

    private fun setButtonClickListeners() {
        val buttons  = listOf(
            binding.typeBasicDesignBtnTv,
            binding.typeWatercolorPaintingBtnTv,
            binding.typeChangeOfThoughtBtnTv,
            binding.typeWesternPaintingBtnIv,
            binding.typeOrientalPaintingBtnTv,
            binding.typeAdultBtnTv,
            binding.typeIdeasAndExpressionsBtnTv,
            binding.typeBasicLiteracyBtnTv,
            binding.typeDrawingBtnTv,
            binding.typeAnimationBtnTv,
            binding.typePortfolioBtnTv,
            binding.typeInterviewBtnTv
        )

        // 버튼 클릭 리스너 설정 (중복 제거)
        buttons.forEach { it.setOnClickListener { toggleSelection(it as TextView) }
        }
    }

    private fun toggleSelection(selected: TextView) {
        val text = selected.text.toString() // 선택된 버튼의 텍스트 가져오기

        if (typeOptions.contains(text)) {
            // 이미 선택된 경우 -> 해제
            typeOptions.remove(text)
            selected.setBackgroundResource(R.drawable.bg_rectangle) // 원래 배경
            selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.font3))
        } else {
            // 선택 개수가 3개 미만일 때만 추가 선택 가능
            if (typeOptions.size < 3) {
                typeOptions.add(text)
                selected.setBackgroundResource(R.drawable.bg_rectangle_select) // 선택된 배경
                selected.setTextColor(Color.WHITE)
            }
        }

        updateNextButtonState() // "다음" 버튼 상태 업데이트
    }

    private fun updateNextButtonState() {
        val hasSelection = typeOptions.isNotEmpty() // 최소 1개 이상 선택되었는지 확인

        with(binding.typeNextBtnTv) {
            isEnabled = hasSelection // 최소 1개 이상 선택 시 활성화
            setBackgroundResource(
                if (hasSelection) R.drawable.bg_color_on else R.drawable.bg_color_off
            )
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (hasSelection) R.color.white else R.color.gray8
                )
            )
            text = if (hasSelection) "시작하기" else "다음" // 최소 1개 이상 선택 시 "시작하기"로 변경
        }
    }
}