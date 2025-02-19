package com.example.grimmy

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.example.grimmy.Retrofit.Request.BirthRequest
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentBrithYearBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BirthYearFragment : Fragment() {
    private lateinit var binding: FragmentBrithYearBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrithYearBinding.inflate(inflater, container, false)

        // ✅ SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.yearNicknameTv.text = nickname ?: "사용자"

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
            hideKeyboard()
            val selectedYear = binding.yearPickerNp.value

            // ✅ 출생 연도를 서버에 저장 (SharedPreferences에는 저장 안 함)
            sendBirthYearToServer(selectedYear)
        }
        return binding.root
    }

    // ✅ 저장된 닉네임 가져오기
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }

    // ✅ 출생 연도를 서버에 저장하는 함수
    private fun sendBirthYearToServer(birthYear: Int) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1) // ✅ 저장된 userId 불러오기

        if (userId == -1) {
            Log.e("BirthYearFragment", "❌ 저장된 userId가 없습니다! 요청을 보낼 수 없습니다.")
            return
        }
        Log.i("BirthYearFragment", "✅ 저장된 userId 사용: $userId")

        RetrofitClient.service.updateBirthYear(userId, BirthRequest(birthYear))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.i("BirthYearFragment", "✅ 출생 연도 업데이트 성공!")

                        // ✅ 서버 저장 후 다음 프래그먼트로 이동
                        moveToNextPage()
                    } else {
                        Log.e("BirthYearFragment", "❌ 출생 연도 업데이트 실패: ${response.code()}")
                        Log.e("BirthYearFragment", "❌ 응답 내용: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("BirthYearFragment", "❌ 네트워크 오류: ${t.message}")
                }
            })
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



    private fun moveToNextPage() {
        binding.yearNextBtnTv.postDelayed({
            (activity as OnboardingActivity).goToNextPage()
        }, 300)
    }

    private fun hideKeyboard() {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = activity?.currentFocus ?: View(context)
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

}