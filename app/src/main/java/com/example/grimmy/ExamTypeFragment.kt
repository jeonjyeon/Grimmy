package com.example.grimmy

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.grimmy.Retrofit.Request.CategoryRequest
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentExamTypeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExamTypeFragment : Fragment() {
    private lateinit var binding: FragmentExamTypeBinding

    private val typeOptions = mutableSetOf<String>() // 선택된 유형을 저장할 Set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExamTypeBinding.inflate(inflater, container, false)

        // ✅ SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.typeNicknameTv.text = nickname ?: "사용자"

        // 각 버튼에 클릭 리스너 설정
        setButtonClickListeners()
        // 초기 상태: 아무 것도 선택되지 않음
        updateNextButtonState()


        // "다음" 버튼 클릭 이벤트 처리
        binding.typeNextBtnTv.setOnClickListener {
            sendExamTypeToServer(typeOptions.toList()) // ✅ 서버에 전송

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
    // ✅ 닉네임을 SharedPreferences에서 가져오는 함수
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)


    }

    // ✅ 선택된 시험 유형을 ENUM 변환 후 서버에 전송하는 함수
    private fun sendExamTypeToServer(selectedTypes: List<String>) {
        val examTypeEnums = selectedTypes.mapNotNull { getExamTypeEnum(it) } // 선택된 한글 값을 ENUM으로 변환

        if (examTypeEnums.isNotEmpty()) {
            RetrofitClient.service.updateCategory(CategoryRequest(examTypeEnums.map { it.name })) // ✅ ENUM.name 리스트 전송
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Log.i("ExamTypeFragment", "✅ 시험 유형 업데이트 성공! $examTypeEnums")
                        } else {
                            Log.e("ExamTypeFragment", "❌ 시험 유형 업데이트 실패: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("ExamTypeFragment", "❌ 네트워크 오류: ${t.message}")
                    }
                })
        } else {
            Log.e("ExamTypeFragment", "❌ 잘못된 시험 유형 선택: $selectedTypes")
        }
    }

    // ✅ UI에서 선택한 값 → ENUM 변환
    private fun getExamTypeEnum(typeText: String): EnumExamType? {
        return EnumExamType.entries.find { it.examType == typeText }
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
                if (hasSelection) R.drawable.bg_ok_btn else R.drawable.bg_cancel_btn
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