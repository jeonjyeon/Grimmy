package com.example.grimmy

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.grimmy.databinding.DialogTakenTimePickerBinding

class TakenTimeDialogFragment : DialogFragment() {

    interface OnTimeSelectedListener {
        fun onTimeSelected(hours: Int, minutes: Int)
    }

    var listener: OnTimeSelectedListener? = null

    private lateinit var binding: DialogTakenTimePickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 뷰 바인딩으로 다이얼로그 레이아웃 inflate
        binding = DialogTakenTimePickerBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext(), R.style.TransparentDialog)
        dialog.setContentView(binding.root)

        setupNumberPickers()

        // 확인 버튼 클릭 시 선택된 시간과 분을 listener에 전달하고 다이얼로그 종료
        binding.takenTimePickerOkBtnTv.setOnClickListener {
            val hours = binding.takenTimePickerHourNp.value
            val minutes = binding.takenTimePickerMinutesNp.value
            listener?.onTimeSelected(hours, minutes)
            dismiss()
        }
        // 취소 버튼 클릭 시 다이얼로그 종료
        binding.takenTimePickerCancelBtnTv.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun setupNumberPickers() {
        // 시간 NumberPicker 설정: 0 ~ 23
        binding.takenTimePickerHourNp.apply {
            minValue = 0
            maxValue = 23
            // "0 시간", "1 시간", ... "23 시간" 형식의 배열 생성하여 표시
            val hourValues = Array(24) { "$it 시간" }
            displayedValues = hourValues
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textColor = Color.WHITE
            }
        }

        // 분 NumberPicker 설정: 0 ~ 59
        binding.takenTimePickerMinutesNp.apply {
            minValue = 0
            maxValue = 59
            // "0 분", "1 분", ... "59 분" 형식의 배열 생성하여 표시
            val minuteValues = Array(60) { "$it 분" }
            displayedValues = minuteValues
            // 초기값은 현재 분으로 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textColor = Color.WHITE
            }
        }
    }
}