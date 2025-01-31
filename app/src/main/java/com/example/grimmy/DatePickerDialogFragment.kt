package com.example.grimmy

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
        val dialog = Dialog(requireContext())
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
        binding.datePickerYearNp.apply {
            minValue = 2000
            maxValue = 2100
            value = Calendar.getInstance().get(Calendar.YEAR)
        }
        binding.datePickerMonthNp.apply {
            minValue = 1
            maxValue = 12
            value = Calendar.getInstance().get(Calendar.MONTH) + 1
            displayedValues = arrayOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
        }
    }
}