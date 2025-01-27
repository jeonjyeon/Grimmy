package com.example.grimmy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat


class NicknameFragment : Fragment() {
    private lateinit var nicknameInput: EditText
    private lateinit var nextTextView: TextView
    private lateinit var charCountTextView: TextView // 글자 수 카운트 TextView
    private lateinit var guideTextView: TextView // 공백 안내 메시지 TextView
    private val nicknameRegex = "^[가-힣a-zA-Z0-9]+\$".toRegex() // 닉네임으로 허용되는 문자
    private val maxLength = 5 // 닉네임 최대 길이

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nickname, container, false)

        // UI 요소 초기화
        nicknameInput = view.findViewById(R.id.nickname_nickname_et)
        nextTextView = view.findViewById(R.id.nickname_next_btn_tv)
        charCountTextView = view.findViewById(R.id.nickname_nickname_count_tv)
        guideTextView = view.findViewById(R.id.nickname_guide_sentence_tv)

        // 초기 글자 수 카운트 0/5 설정
        updateCharCount(0)
        nextTextView.isEnabled = false
//        setInitialButtonState()
        showGuideMessage("한글, 영문, 숫자만 입력해 주세요.")

        // EditText에 입력된 텍스트를 감지
        nicknameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nickname = s.toString() // 닉네임

                // 글자 수 카운트 업데이트
                updateCharCount(nickname.length)

                // 입력된 닉네임이 없는 경우
                if (nickname.isEmpty()) {
                    showGuideMessage("한글, 영문, 숫자만 입력해 주세요.")
//                    setInitialButtonState()
                    return
                }

                // 닉네임 유효성 검사
                validateNickname(nickname)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // TextView 클릭 이벤트 처리
        nextTextView.setOnClickListener {
            val nickname = nicknameInput.text.toString()

            // 유효한 닉네임인지 체크 후 다음 페이지로 이동
            if (isValidNickname(nickname)) {
                (activity as OnboardingActivity).viewPager.currentItem += 1
            } else {
                showGuideMessage("유효한 닉네임을 입력해주세요.")
            }
        }
        return view
    }

    // 다음 버튼 초기 설정
//    private fun setInitialButtonState() {
//        nextTextView.setBackgroundResource(R.drawable.bg_color_off)
//        nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray8))
//    }

    // 글자 수 카운트 업데이트
    private fun updateCharCount(currentLength: Int) {
        charCountTextView.text = "$currentLength/$maxLength"
    }

    // 안내 메시지 표시 함수
    private fun showGuideMessage(message: String) {
        if (guideTextView.text.toString() != message) { // 동일 메시지가 아닐 경우에만 업데이트
            guideTextView.text = message
        }
    }

    private fun isValidNickname(nickname: String): Boolean {
        return nicknameRegex.matches(nickname)
    }

    private fun validateNickname(nickname: String) {
        if (!isValidNickname(nickname)) {
            nextTextView.isEnabled = false
            nextTextView.setBackgroundResource(R.drawable.bg_color_off)
            nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray8))
            showGuideMessage("초성, 공백, 특수문자는 포함할 수 없습니다.")
        } else {
            nextTextView.isEnabled = true
            nextTextView.setBackgroundResource(R.drawable.bg_color_on)
            nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            showGuideMessage("사용 가능한 닉네임입니다.")
        }
    }
}
