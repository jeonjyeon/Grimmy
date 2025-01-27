package com.example.grimmy

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.grimmy.databinding.DialogDatePickerBinding
import java.util.Calendar

class DatePickerDialogFragment : DialogFragment() {

    private lateinit var binding : DialogDatePickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogDatePickerBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        setupNumberPickers()

        binding.datePickerOkBtnTv.setOnClickListener {
            val year = binding.datePickerYearNp.value
            val month = binding.datePickerMonthNp.value
            handleDateSelected(year,month)
            dismiss()
        }
        binding.datePickerCancelBtnTv.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun setupNumberPickers() {
        binding.datePickerYearNp.apply {
            minValue = 2000
            maxValue = 2100
            value = Calendar.getInstance().get(Calendar.YEAR)
        }
        binding.datePickerMonthNp.apply {
            minValue = 1
            maxValue = 12
            value = Calendar.getInstance().get(Calendar.MONTH) + 1
            displayedValues = arrayOf("1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월")
        }
    }

    private fun handleDateSelected(year:Int,month:Int) {
        // api연동 시 선택된 달의 달력 띄우기
    }

}