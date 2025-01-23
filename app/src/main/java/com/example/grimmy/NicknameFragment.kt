package com.example.grimmy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat


class NicknameFragment : Fragment() {
    private lateinit var nicknameInput: EditText
    private lateinit var nextTextView: TextView
    private lateinit var charCountTextView: TextView // 글자 수 카운트 TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nickname, container, false)

        // UI 요소 초기화
        nicknameInput = view.findViewById(R.id.nickname_nickname_et)
        nextTextView = view.findViewById(R.id.nickname_next_btn_tv)
        charCountTextView = view.findViewById(R.id.nickname_nickname_count_tv)

        // 초기 글자 수 카운트 0/5 설정
        updateCharCount(0)

        // EditText에 입력된 텍스트를 감지
        nicknameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0 // 입력된 글자 수
                updateCharCount(currentLength) // 글자 수 업데이트

                // 닉네임 입력 여부에 따른 배경색 변경
                if (s.isNullOrEmpty()) { // 닉네임 입력 안 됐을 때
                    nextTextView.setBackgroundResource(R.drawable.bg_color_off)
                } else { // 닉네임 입력 됐을 때
                    nextTextView.setBackgroundResource(R.drawable.bg_color_on)
                    nextTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // TextView 클릭 이벤트 처리
        nextTextView.setOnClickListener {
            val nickname = nicknameInput.text.toString()
            if (nickname.isNotEmpty() && !nickname.contains(" ")) { // 공백 체크 추가
                // 닉네임이 입력되었을 때 다음 페이지로 이동
                (activity as OnboardingActivity).viewPager.currentItem += 1
            }
            else {
                // 닉네임 입력 요청
                Toast.makeText(context, "닉네임을 입력해주세요. (공백 포함 불가)", Toast.LENGTH_SHORT).show()
            }
//            when {
//                nickname.isEmpty() -> {
//                    // 닉네임이 입력되지 않았을 때
//                    Toast.makeText(context, "닉네임을 입력해주세요. (공백 포함 불가)", Toast.LENGTH_SHORT).show()
//                }
//                nickname.contains(" ") -> {
//                    // 닉네임에 공백이 포함되었을 때
//                    Toast.makeText(context, "닉네임에 공백은 포함될 수 없습니다.", Toast.LENGTH_SHORT).show()
//                }
//                else -> {
//                    // 닉네임이 입력되었을 때 다음 페이지로 이동
//                    (activity as OnboardingActivity).viewPager.currentItem += 1
//                }
//            }
        }
        return view
    }

    private fun updateCharCount(currentLength: Int) {
        val maxLength = 5
        charCountTextView.text = "$currentLength/$maxLength"
    }
}
