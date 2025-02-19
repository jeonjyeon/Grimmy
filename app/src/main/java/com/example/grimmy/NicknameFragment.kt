package com.example.grimmy

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.example.grimmy.databinding.FragmentNicknameBinding
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.Retrofit.Request.NicknameRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NicknameFragment : Fragment() {
    private lateinit var binding: FragmentNicknameBinding

    private val nicknameRegex = "^[가-힣a-zA-Z0-9]+\$".toRegex() // 닉네임으로 허용되는 문자
    private val maxLength = 5 // 닉네임 최대 길이

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNicknameBinding.inflate(inflater, container, false)

        // 초기 글자 수 카운트 0/5 설정
        updateCharCount(0)
        binding.nicknameNextBtnTv.isEnabled = false
        showGuideMessage("한글, 영문, 숫자만 입력해 주세요.")

        // EditText에 입력된 텍스트를 감지
        binding.nicknameNicknameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nickname = s.toString().trimEnd()
                if (nickname != s.toString()) {
                    binding.nicknameNicknameEt.setText(nickname)
                    binding.nicknameNicknameEt.setSelection(nickname.length)
                }
                updateCharCount(nickname.length)

                if (nickname.isEmpty()) {
                    showGuideMessage("한글, 영문, 숫자만 입력해 주세요.")
                    binding.nicknameNextBtnTv.isEnabled = false
                    return
                }

                validateNickname(nickname)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // "다음" 버튼 클릭 이벤트
        binding.nicknameNextBtnTv.setOnClickListener {
            hideKeyboard()
            binding.nicknameNicknameEt.clearFocus()
            val nickname = binding.nicknameNicknameEt.text.toString().trim()

            if (isValidNickname(nickname)) {
                // ✅ 닉네임 서버에 저장
                sendNicknameToServer(nickname)
            } else {
                showGuideMessage("유효한 닉네임을 입력해주세요.")
            }
        }

        return binding.root
    }

    // ✅ 닉네임을 SharedPreferences에 저장하는 함수
    private fun saveNickname(nickname: String) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("nickname", nickname).apply()
    }

    // ✅ 서버에 닉네임 저장 요청
    private fun sendNicknameToServer(nickname: String) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1) // ✅ 저장된 userId 불러오기

        if (userId == -1) {
            Log.e("NicknameFragment", "❌ 저장된 userId가 없습니다! 요청을 보낼 수 없습니다.")
            return
        }
        Log.i("NicknameFragment", "✅ 저장된 userId 사용: $userId")

        RetrofitClient.service.updateNickname(userId, NicknameRequest(nickname))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.i("NicknameFragment", "✅ 닉네임 업데이트 성공!")

                        // ✅ 닉네임을 SharedPreferences에 저장
                        saveNickname(nickname)

                        moveToNextPage()
                    } else {
                        Log.e("NicknameFragment", "❌ 닉네임 업데이트 실패: ${response.code()}")
                        Log.e("NicknameFragment", "❌ 응답 내용: ${response.errorBody()?.string()}")
                        showGuideMessage("닉네임 설정에 실패했습니다. 다시 시도해주세요.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("NicknameFragment", "❌ 네트워크 오류: ${t.message}")
                    showGuideMessage("네트워크 오류가 발생했습니다.")
                }
            })
    }

    // ✅ 다음 프래그먼트로 이동
    private fun moveToNextPage() {
        binding.nicknameNextBtnTv.postDelayed({
            (activity as OnboardingActivity).goToNextPage()
        }, 300)
    }

    // ✅ 글자 수 카운트 업데이트
    private fun updateCharCount(currentLength: Int) {
        binding.nicknameNicknameCountTv.text = "$currentLength/$maxLength"
    }

    // ✅ 안내 메시지 표시
    private fun showGuideMessage(message: String) {
        if (binding.nicknameGuideSentenceTv.text.toString() != message) {
            binding.nicknameGuideSentenceTv.text = message
        }
    }

    private fun isValidNickname(nickname: String): Boolean {
        return nicknameRegex.matches(nickname)
    }

    private fun validateNickname(nickname: String) {
        val isValid = isValidNickname(nickname)
        binding.nicknameNextBtnTv.isEnabled = isValid
        binding.nicknameNextBtnTv.setBackgroundResource(
            if (isValid) R.drawable.bg_ok_btn else R.drawable.bg_cancel_btn
        )
        binding.nicknameNextBtnTv.setTextColor(
            ContextCompat.getColor(requireContext(), if (isValid) R.color.white else R.color.gray8)
        )
        showGuideMessage(if (isValid) "사용 가능한 닉네임입니다." else "초성, 공백, 특수문자는 포함할 수 없습니다.")
    }

    private fun hideKeyboard() {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = activity?.currentFocus ?: View(context)
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
