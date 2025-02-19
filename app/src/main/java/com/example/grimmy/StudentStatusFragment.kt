package com.example.grimmy

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.grimmy.Retrofit.Request.StatusRequest
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentStudentStatusBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentStatusFragment : Fragment() {
    private lateinit var binding: FragmentStudentStatusBinding

    private lateinit var statusOptions: List<TextView>
    private var selectedOption: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentStatusBinding.inflate(inflater, container, false)
        // 사용자 정보를 관찰

        // ✅ SharedPreferences에서 닉네임 가져오기
        val nickname = getNickname()
        binding.statusNicknameTv.text = nickname ?: "사용자" // 닉네임이 없으면 기본값 표시


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
                sendStatusToServer(studentStatus) // ✅ 서버로 전송
            }
        }

        return binding.root
    }

    // ✅ 저장된 닉네임 가져오기
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }

    // ✅ 서버에 신분(status) 저장 요청
    private fun sendStatusToServer(status: String) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1) // ✅ 저장된 userId 불러오기

        if (userId == -1) {
            Log.e("StudentStatusFragment", "❌ 저장된 userId가 없습니다! 요청을 보낼 수 없습니다.")
            return
        }
        Log.i("StudentStatusFragment", "✅ 저장된 userId 사용: $userId")

        val statusEnum = getStatusEnum(status)
        if (statusEnum != null) {
            RetrofitClient.service.updateStatus(userId, StatusRequest(statusEnum.name)) // ✅ ENUM.name 전송
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Log.i("StudentStatusFragment", "✅ 신분 업데이트 성공! (${statusEnum.name})")
                            moveToNextPage()
                        } else {
                            Log.e("StudentStatusFragment", "❌ 신분 업데이트 실패: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("StudentStatusFragment", "❌ 네트워크 오류: ${t.message}")
                    }
                })
        } else {
            Log.e("StudentStatusFragment", "❌ 잘못된 신분 선택: $status")
        }
    }

    // ✅ 선택된 신분을 ENUM 형식의 문자열로 변환하는 함수
    private fun getStatusEnum(statusText: String): EnumStatus? {
        return EnumStatus.entries.find { it.statusName == statusText }
    }

    private fun moveToNextPage() {
        binding.statusNextBtnTv.postDelayed({
            (activity as OnboardingActivity).goToNextPage()
        }, 350)
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
            setBackgroundResource(R.drawable.bg_circle_box_color)
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
                if (selectedOption != null) R.drawable.bg_ok_btn else R.drawable.bg_cancel_btn
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