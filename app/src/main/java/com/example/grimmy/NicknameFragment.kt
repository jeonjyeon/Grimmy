package com.example.grimmy

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.example.grimmy.databinding.FragmentNicknameBinding
import com.example.grimmy.viewmodel.UserViewModel
import androidx.fragment.app.activityViewModels



class NicknameFragment : Fragment() {
    private lateinit var binding: FragmentNicknameBinding
    private val userViewModel: UserViewModel by activityViewModels() // ViewModel 사용

    private val nicknameRegex = "^[가-힣a-zA-Z0-9]+\$".toRegex() // 닉네임으로 허용되는 문자
    private val maxLength = 5 // 닉네임 최대 길이


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNicknameBinding.inflate(inflater, container, false)

        // 초기 글자 수 카운트 0/5 설정
        updateCharCount(0)
        binding.nicknameNextBtnTv.isEnabled = false
        showGuideMessage("한글, 영문, 숫자만 입력해 주세요.")

        // EditText에 입력된 텍스트를 감지
        binding.nicknameNicknameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 입력 중에 끝에 공백이 있으면 제거
                val nickname = s.toString().trimEnd()
                if (nickname != s.toString()) {
                    binding.nicknameNicknameEt.setText(nickname)
                    binding.nicknameNicknameEt.setSelection(nickname.length) // 커서 위치 조정
                }
                // 글자 수 카운트 업데이트
                updateCharCount(nickname.length)

                // 입력된 닉네임이 없는 경우
                if (nickname.isEmpty()) {
                    showGuideMessage("한글, 영문, 숫자만 입력해 주세요.")
                    return
                }

                // 닉네임 유효성 검사
                validateNickname(nickname)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // TextView 클릭 이벤트 처리
        binding.nicknameNextBtnTv.setOnClickListener {
            hideKeyboard()
            binding.nicknameNicknameEt.clearFocus()
            val nickname = binding.nicknameNicknameEt.text.toString().trim()

            // 유효한 닉네임인지 체크 후 다음 페이지로 이동
            if (isValidNickname(nickname)) {
                // ViewModel에 닉네임 저장
                userViewModel.setNickname(nickname)

                // 서버에 닉네임 저장

                // 다음 프래그먼트로 넘어감
                binding.nicknameNextBtnTv.postDelayed({
                    (activity as OnboardingActivity).goToNextPage()
                }, 300) // 300ms 지연
            } else {
                showGuideMessage("유효한 닉네임을 입력해주세요.")
            }
        }
        return binding.root
    }

    // 글자 수 카운트 업데이트
    private fun updateCharCount(currentLength: Int) {
        binding.nicknameNicknameCountTv.text = "$currentLength/$maxLength"
    }

    // 안내 메시지 표시 함수
    private fun showGuideMessage(message: String) {
        if (binding.nicknameGuideSentenceTv.text.toString() != message) { // 동일 메시지가 아닐 경우에만 업데이트
            binding.nicknameGuideSentenceTv.text = message
        }
    }

    private fun isValidNickname(nickname: String): Boolean {
        return nicknameRegex.matches(nickname)
    }

    private fun validateNickname(nickname: String) {
        val isValid = isValidNickname(nickname)
        binding.nicknameNextBtnTv.isEnabled = isValid
        binding.nicknameNextBtnTv.setBackgroundResource(if (isValid) R.drawable.bg_color_on else R.drawable.bg_color_off)
        binding.nicknameNextBtnTv.setTextColor(ContextCompat.getColor(requireContext(), if (isValid) R.color.white else R.color.gray8))
        showGuideMessage(if (isValid) "사용 가능한 닉네임입니다." else "초성, 공백, 특수문자는 포함할 수 없습니다.")
    }

    private fun hideKeyboard() {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = activity?.currentFocus ?: View(context)
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
