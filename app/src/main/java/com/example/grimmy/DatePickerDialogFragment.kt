package com.example.grimmy

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.grimmy.databinding.DialogDatePickerBinding
import java.util.Calendar

class DatePickerDialogFragment : DialogFragment() {

    interface OnDateSelectedListener {
        fun onDateSelected(year: Int, month: Int)
    }

    var listener: OnDateSelectedListener? = null

    private lateinit var binding: DialogDatePickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDatePickerBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext(), R.style.TransparentDialog) //다이얼로그 코너 투명 적용
        dialog.setContentView(binding.root)

        setupNumberPickers()

        binding.datePickerOkBtnTv.setOnClickListener {
            val year = binding.datePickerYearNp.value
            val month = binding.datePickerMonthNp.value
            listener?.onDateSelected(year, month)
            dismiss()
        }
        binding.datePickerCancelBtnTv.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun setupNumberPickers() {
        val years = Array(101) { (2000 + it).toString() + "년" } // 2000년부터 2100년까지의 배열 생성
        binding.datePickerYearNp.apply {
            minValue = 0
            maxValue = years.size - 1
            displayedValues = years  // 미리 정의한 년도 배열을 설정
            value = Calendar.getInstance().get(Calendar.YEAR) - 2000  // 현재 년도의 인덱스를 계산하여 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textColor = Color.WHITE
            }
        }
        binding.datePickerMonthNp.apply {
            minValue = 1
            maxValue = 12
            value = Calendar.getInstance().get(Calendar.MONTH) + 1
            displayedValues = arrayOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textColor = Color.WHITE
            }
        }
    }
}