package com.example.grimmy

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.grimmy.databinding.DialogScorePickerBinding

class ScorePickerDialogFragment : DialogFragment() {

    interface OnScoreSelectedListener {
        fun onScoreSelected(score: Int)
    }

    var listener: OnScoreSelectedListener? = null

    private lateinit var binding: DialogScorePickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 뷰 바인딩으로 다이얼로그 레이아웃 inflate
        binding = DialogScorePickerBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext(), R.style.TransparentDialog)
        dialog.setContentView(binding.root)

        setupNumberPicker()

        // 확인 버튼 클릭 시 선택된 점수를 listener에 전달하고 다이얼로그 종료
        binding.takenTimePickerOkBtnTv.setOnClickListener {
            val score = binding.scorePickerNp.value
            listener?.onScoreSelected(score)
            dismiss()
        }
        // 취소 버튼 클릭 시 다이얼로그 종료
        binding.takenTimePickerCancelBtnTv.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun setupNumberPicker() {
        binding.scorePickerNp.apply {
            minValue = 0
            maxValue = 100
            // "0 점", "1 점", ... "100 점" 형식의 배열 생성하여 표시
            val scoreValues = Array(101) { "$it 점" }
            displayedValues = scoreValues
            // 초기값 설정: arguments를 통해 전달받거나 기본값 0 사용
            value = arguments?.getInt("initialScore") ?: 0
        }
    }
}
